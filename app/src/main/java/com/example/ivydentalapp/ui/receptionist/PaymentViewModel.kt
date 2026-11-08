package com.example.ivydentalapp.ui.receptionist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ivydentalapp.data.local.entity.InvoiceEntity
import com.example.ivydentalapp.data.local.entity.PaymentEntity
import com.example.ivydentalapp.data.repository.AppointmentRepository
import com.example.ivydentalapp.data.repository.InvoiceRepository
import com.example.ivydentalapp.data.repository.NotificationRepository
import com.example.ivydentalapp.data.repository.PaymentRepository
import com.example.ivydentalapp.data.service.PaymentSimulationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PaymentUiState(
    val isLoading: Boolean = false,
    val invoices: List<InvoiceEntity> = emptyList(),
    val payments: List<PaymentEntity> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null
)

class PaymentViewModel(
    private val invoiceRepository: InvoiceRepository,
    private val paymentRepository: PaymentRepository,
    private val appointmentRepository: AppointmentRepository,
    private val notificationRepository: NotificationRepository,
    private val paymentService: PaymentSimulationService
) : ViewModel() {
    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    init {
        loadUnpaidInvoices()
        observePayments()
    }

    fun loadUnpaidInvoices() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                invoiceRepository.getAllInvoices().collect { invoices ->
                    val unpaidInvoices = invoices.filter { 
                        it.paymentStatus != "Paid" 
                    }
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            invoices = unpaidInvoices
                        ) 
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isLoading = false, error = e.message) 
                }
            }
        }
    }

    fun observePayments() {
        viewModelScope.launch {
            paymentRepository.getAllPayments().collect { payments ->
                _uiState.update { it.copy(payments = payments) }
            }
        }
    }

    fun applyDiscount(invoiceIds: List<Int>, discountType: String, referenceId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                if (referenceId.isBlank()) {
                    _uiState.update { it.copy(isLoading = false, error = "Reference ID is required for discount validation.") }
                    return@launch
                }

                val selectedInvoices = _uiState.value.invoices.filter { invoiceIds.contains(it.invoiceId) }
                
                selectedInvoices.forEach { invoice ->
                    // Just Notify for now as we can't easily change schema
                    notificationRepository.createNotification(
                        userId = invoice.patientId ?: 0,
                        userType = "Patient",
                        title = "Discount Applied",
                        message = "A ${discountType} discount (Ref: ${referenceId}) has been applied/validated for Invoice #${invoice.invoiceId}."
                    )
                     notificationRepository.createNotification(
                        userId = 0,
                        userType = "Staff",
                        title = "Discount Validated",
                        message = "Discount (${discountType} - ${referenceId}) validated for Invoice #${invoice.invoiceId}."
                    )
                }

                 _uiState.update { 
                    it.copy(
                        isLoading = false,
                        successMessage = "Discount validated for ${selectedInvoices.size} invoice(s). Notifications sent."
                    ) 
                }

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun processPayment(
        invoiceIds: List<Int>,
        paymentMethod: String,
        amountReceived: Double? = null,
        notes: String? = null,
        gcashReferenceNumber: String? = null,
        gcashAccountName: String? = null
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val selectedInvoices = _uiState.value.invoices.filter { 
                    invoiceIds.contains(it.invoiceId) 
                }
                
                if (selectedInvoices.isEmpty()) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "No valid invoices selected"
                        ) 
                    }
                    return@launch
                }

                val totalAmount = selectedInvoices.sumOf { it.totalAmount ?: 0.0 }
                
                if (paymentMethod == "Cash" && amountReceived != null) {
                    if (amountReceived < totalAmount) {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = "Amount received (₱${String.format("%.2f", amountReceived)}) is less than total amount (₱${String.format("%.2f", totalAmount)})"
                            ) 
                        }
                        return@launch
                    }
                }

                // Process payment for each invoice
                for (invoice in selectedInvoices) {
                    var invoiceAmount = invoice.totalAmount ?: 0.0
                    val invoicePatientId = invoice.patientId ?: 0
                    val invoiceAppointmentId = invoice.appointmentId
                    
                    if (invoiceAmount <= 0) invoiceAmount = 0.0 // Handle degenerate case
                    
                    if (invoicePatientId <= 0) {
                        continue // Skip invalid invoices
                    }
                    
                    // Fetch Current Paid amount before this transaction
                    val currentPayments = paymentRepository.getPaymentsForInvoices(listOf(invoice.invoiceId))
                    val alreadyPaid = currentPayments.sumOf { 
                        if (it.status == "Completed" || it.status == "Paid") it.amountPaid else 0.0 
                    }
                    // Calculate remaining balance logic
                    var remainingBalance = invoiceAmount - alreadyPaid
                    if (remainingBalance < 0) remainingBalance = 0.0

                    var amountToPayForThisInvoice = remainingBalance

                    if (paymentMethod == "Cash") {
                         // Calls service, returns ID.
                        paymentService.createCashPayment(
                            invoiceId = invoice.invoiceId,
                            amount = amountToPayForThisInvoice,
                            patientId = invoicePatientId,
                            appointmentId = invoiceAppointmentId,
                            notes = notes
                        )
                    } else if (paymentMethod == "GCash") {
                        if (gcashReferenceNumber.isNullOrBlank() || gcashAccountName.isNullOrBlank()) {
                            _uiState.update {it.copy(isLoading = false, error = "GCash reference number and account name are required")}
                            return@launch
                        }
                        val pid = paymentService.createGCashPayment(
                            invoiceId = invoice.invoiceId,
                            amount = amountToPayForThisInvoice,
                            referenceNumber = gcashReferenceNumber!!,
                            accountName = gcashAccountName!!,
                            patientId = invoicePatientId,
                            appointmentId = invoiceAppointmentId
                        )
                        // Wait for simulation to complete to ensure status is updated in DB
                        paymentService.simulatePaymentCompletion(pid.toInt(), delaySeconds = 2)
                    }

                    // Update invoice status based on (Already Paid + This Payment)
                    val totalPaidAfter = alreadyPaid + amountToPayForThisInvoice
                    val finalRemaining = invoiceAmount - totalPaidAfter
                    
                    val newStatus = when {
                        finalRemaining <= 0.01 -> "Paid"
                        totalPaidAfter > 0 -> "Partial"
                        else -> "Unpaid"
                    }
                    invoiceRepository.updateInvoiceStatus(invoice.invoiceId, newStatus)

                    // Update Appointment Status if Paid
                    if (newStatus == "Paid" && invoiceAppointmentId != null && invoiceAppointmentId > 0) {
                        appointmentRepository.updateAppointmentStatus(invoiceAppointmentId, "Completed")
                    }

                    // Send Notifications
                    if (newStatus == "Paid" || newStatus == "Partial") {
                        notificationRepository.createNotification(
                            userId = invoicePatientId,
                            userType = "Patient",
                            title = "Payment Confirmed",
                            message = "Payment of ₱${String.format("%,.2f", amountToPayForThisInvoice)} for Invoice #${invoice.invoiceId} confirmed."
                        )
                        notificationRepository.createNotification(
                            userId = 0,
                            userType = "Staff",
                            title = "Payment Received",
                            message = "Payment received from Patient ${invoicePatientId} for Invoice #${invoice.invoiceId}."
                        )
                    }
                }

                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        successMessage = "Payment processed successfully for ${selectedInvoices.size} invoice(s)"
                    ) 
                }
                
                loadUnpaidInvoices()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to process payment"
                    ) 
                }
            }
        }
    }


    fun clearMessages() {
        _uiState.update { 
            it.copy(error = null, successMessage = null) 
        }
    }
}

class PaymentViewModelFactory(
    private val invoiceRepository: InvoiceRepository,
    private val paymentRepository: PaymentRepository,
    private val appointmentRepository: AppointmentRepository,
    private val notificationRepository: NotificationRepository,
    private val paymentService: PaymentSimulationService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PaymentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PaymentViewModel(invoiceRepository, paymentRepository, appointmentRepository, notificationRepository, paymentService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
