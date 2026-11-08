package com.example.ivydentalapp.ui.dashboard

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ivydentalapp.data.local.entity.AppointmentEntity
import com.example.ivydentalapp.data.local.entity.InvoiceEntity
import com.example.ivydentalapp.data.repository.AppointmentRepository
import com.example.ivydentalapp.data.repository.InvoiceRepository
import com.example.ivydentalapp.data.repository.NotificationRepository
import com.example.ivydentalapp.data.repository.PaymentRepository
import com.example.ivydentalapp.data.service.PaymentSimulationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PaymentViewModel(
    private val invoiceRepository: InvoiceRepository,
    private val paymentRepository: PaymentRepository,
    private val appointmentRepository: AppointmentRepository,
    private val paymentSimulationService: PaymentSimulationService,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    // Selected Invoice IDs (Persists across tab switches)
    private val _selectedInvoiceIds = mutableStateListOf<Int>()
    val selectedInvoiceIds: List<Int> get() = _selectedInvoiceIds

    // Dialog States
    private val _showPaymentDialog = MutableStateFlow(false)
    val showPaymentDialog = _showPaymentDialog.asStateFlow()

    private val _showSuccessDialog = MutableStateFlow(false)
    val showSuccessDialog = _showSuccessDialog.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing = _isProcessing.asStateFlow()

    // Form Data
    private val _gcashAccountName = MutableStateFlow("")
    val gcashAccountName = _gcashAccountName.asStateFlow()

    private val _gcashRefNumber = MutableStateFlow("")
    val gcashRefNumber = _gcashRefNumber.asStateFlow()

    // Helper Data
    private val _invoiceBalances = MutableStateFlow<Map<Int, Double>>(emptyMap())
    val invoiceBalances = _invoiceBalances.asStateFlow()

    private val _appointmentDetails = MutableStateFlow<Map<Int, AppointmentEntity>>(emptyMap())
    val appointmentDetails = _appointmentDetails.asStateFlow()

    fun toggleInvoiceSelection(invoiceId: Int) {
        if (_selectedInvoiceIds.contains(invoiceId)) {
            _selectedInvoiceIds.remove(invoiceId)
        } else {
            _selectedInvoiceIds.add(invoiceId)
        }
    }

    fun clearSelection() {
        _selectedInvoiceIds.clear()
    }

    fun setPaymentDialogVisible(visible: Boolean) {
        _showPaymentDialog.value = visible
        if (!visible) {
            // Optional: clear form logic if needed, or keep for convenience
        }
    }

    fun setSuccessDialogVisible(visible: Boolean) {
        _showSuccessDialog.value = visible
    }

    fun updateGcashDetails(name: String, ref: String) {
        _gcashAccountName.value = name
        _gcashRefNumber.value = ref
    }

    fun loadInvoiceDetails(invoices: List<InvoiceEntity>) {
        viewModelScope.launch {
            val balances = mutableMapOf<Int, Double>()
            val details = mutableMapOf<Int, AppointmentEntity>()

            invoices.forEach { invoice ->
                try {
                    val dp = paymentRepository.getTotalDownpaymentForInvoice(invoice.invoiceId)
                    balances[invoice.invoiceId] = (invoice.totalAmount ?: 0.0) - dp

                    if (invoice.appointmentId != null) {
                        val appt = appointmentRepository.getAppointmentById(invoice.appointmentId)
                        if (appt != null) {
                            details[invoice.invoiceId] = appt
                        }
                    }
                } catch (e: Exception) {
                    balances[invoice.invoiceId] = invoice.totalAmount ?: 0.0
                }
            }
            _invoiceBalances.value = balances
            _appointmentDetails.value = details
        }
    }

    fun processPayment(
        patientId: Int,
        invoices: List<InvoiceEntity>
    ) {
        viewModelScope.launch {
            _isProcessing.value = true
            _showPaymentDialog.value = false

            try {
                val selectedInvoices = invoices.filter { _selectedInvoiceIds.contains(it.invoiceId) }
                var totalPaid = 0.0

                selectedInvoices.forEach { invoice ->
                    // Logic similar to Receptionist PaymentViewModel for consistency
                    val invoiceAmount = invoice.totalAmount ?: 0.0
                    
                     // Fetch Current Paid amount before this transaction
                    val currentPayments = paymentRepository.getPaymentsForInvoices(listOf(invoice.invoiceId))
                    val alreadyPaid = currentPayments.sumOf { 
                        if (it.status == "Completed" || it.status == "Paid") it.amountPaid else 0.0 
                    }
                    
                    var remainingBalance = invoiceAmount - alreadyPaid
                    if (remainingBalance < 0) remainingBalance = 0.0
                    
                    // Fix for "0 PHP" issue: Ensure we pay the full amount if remaining is 0 but it's marked as unpaid, 
                    // or if it's just a new transaction.
                    // Ideally remainingBalance should be correct. If it is 0, it means it's fully paid?
                    // But if the user selected it, it's likely Unpaid.
                    // We'll enforce: if remainingBalance is roughly 0, use invoiceAmount (assuming no 'real' payments yet).
                    var amountToPay = remainingBalance
                    if (amountToPay <= 0.01 && invoiceAmount > 0) {
                        amountToPay = invoiceAmount
                    }
                    
                    totalPaid += amountToPay
                    
                    val paymentId = paymentSimulationService.createGCashPayment(
                        invoiceId = invoice.invoiceId,
                        amount = amountToPay,
                        referenceNumber = _gcashRefNumber.value,
                        accountName = _gcashAccountName.value,
                        patientId = patientId,
                        appointmentId = invoice.appointmentId
                    )
                    // Wait for simulation to finish to ensures DB status is "Paid"
                    paymentSimulationService.simulatePaymentCompletion(paymentId.toInt(), delaySeconds = 2)

                    // Update Invoice Status based on (Already + This Payment)
                    val totalPaidAfter = alreadyPaid + amountToPay
                    val finalRemaining = invoiceAmount - totalPaidAfter
                    
                    val newStatus = when {
                        finalRemaining <= 0.01 -> "Paid"
                        totalPaidAfter > 0 -> "Partial"
                        else -> "Unpaid"
                    }
                    invoiceRepository.updateInvoiceStatus(invoice.invoiceId, newStatus)
                    
                    // Update Appointment Status to Completed if Paid
                    if (newStatus == "Paid" && invoice.appointmentId != null && invoice.appointmentId > 0) {
                        appointmentRepository.updateAppointmentStatus(invoice.appointmentId, "Completed")
                    }
                }

                // Notify Patient
                notificationRepository.createNotification(
                    userId = patientId,
                    userType = "Patient",
                    title = "Payment Successful",
                    message = "You have successfully paid ₱${String.format("%,.2f", totalPaid)} via GCash."
                )
                
                // Notify Staff/Admin
                notificationRepository.createNotification(
                    userId = 0, // Staff ID placeholder
                    userType = "Staff",
                    title = "Payment Received",
                    message = "Payment of ₱${String.format("%,.2f", totalPaid)} received via GCash from Patient ID $patientId."
                )

                _selectedInvoiceIds.clear()
                _gcashAccountName.value = ""
                _gcashRefNumber.value = ""
                _isProcessing.value = false
                _showSuccessDialog.value = true

            } catch (e: Exception) {
                _isProcessing.value = false
                e.printStackTrace()
            }
        }
    }
}

class PaymentViewModelFactory(
    private val invoiceRepository: InvoiceRepository,
    private val paymentRepository: PaymentRepository,
    private val appointmentRepository: AppointmentRepository,
    private val paymentSimulationService: PaymentSimulationService,
    private val notificationRepository: NotificationRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PaymentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PaymentViewModel(
                invoiceRepository,
                paymentRepository,
                appointmentRepository,
                paymentSimulationService,
                notificationRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
