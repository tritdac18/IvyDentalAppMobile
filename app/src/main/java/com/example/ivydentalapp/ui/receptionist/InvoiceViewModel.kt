package com.example.ivydentalapp.ui.receptionist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ivydentalapp.data.local.entity.AppointmentEntity
import com.example.ivydentalapp.data.local.entity.InvoiceEntity
import com.example.ivydentalapp.data.local.entity.PatientEntity
import com.example.ivydentalapp.data.repository.AppointmentRepository
import com.example.ivydentalapp.data.repository.InvoiceRepository
import com.example.ivydentalapp.data.repository.ServiceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class InvoiceViewModel(
    application: Application,
    private val invoiceRepository: InvoiceRepository,
    private val appointmentRepository: AppointmentRepository,
    private val patientDao: com.example.ivydentalapp.data.local.dao.PatientDao,
    private val serviceRepository: ServiceRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(InvoiceUiState())
    val uiState: StateFlow<InvoiceUiState> = _uiState.asStateFlow()

    data class InvoiceUiState(
        val invoices: List<InvoiceEntity> = emptyList(),
        val patients: List<PatientEntity> = emptyList(),
        val appointments: List<AppointmentEntity> = emptyList(),
        val outstandingBalance: Double = 0.0,
        val dailyRevenue: Double = 0.0,
        val invoicesGenerated: Int = 0,
        val isLoading: Boolean = false,
        val error: String? = null,
        val successMessage: String? = null
    )

    init {
        observeInvoices()
        loadStats()
        loadPatients()
        loadAppointmentsForInvoice() 
    }

    fun observeInvoices() {
        viewModelScope.launch(Dispatchers.IO) {
            invoiceRepository.getAllInvoices().collect { invoiceList ->
                _uiState.value = _uiState.value.copy(invoices = invoiceList)
            }
        }
    }
    
    fun loadPatients() {
        viewModelScope.launch(Dispatchers.IO) {
             val list = patientDao.getAllPatients()
             _uiState.value = _uiState.value.copy(patients = list)
        }
    }

    fun loadAppointmentsForInvoice() {
        viewModelScope.launch(Dispatchers.IO) {
            val apps = appointmentRepository.getAllAppointments()
            _uiState.value = _uiState.value.copy(appointments = apps)
        }
    }
    
    fun loadAppointmentsForPatient(patientId: Int) {
          viewModelScope.launch(Dispatchers.IO) {
              val apps = appointmentRepository.getAppointmentsWithoutInvoicesForPatient(patientId)
               _uiState.value = _uiState.value.copy(appointments = apps)
          }
    }

    fun loadStats() {
        viewModelScope.launch(Dispatchers.IO) {
             _uiState.value = _uiState.value.copy(
                 outstandingBalance = invoiceRepository.getOutstandingBalance(),
                 dailyRevenue = invoiceRepository.getDailyRevenue(),
                 invoicesGenerated = invoiceRepository.getInvoiceCount()
             )
        }
    }

    fun createInvoiceFromAppointment(
        appointmentId: Int,
        patientId: Int,
        totalAmount: Double,
        additionalExpenses: Double
    ) {
         viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val newInvoice = InvoiceEntity(
                    patientId = patientId,
                    appointmentId = appointmentId,
                    totalAmount = totalAmount,
                    paymentStatus = "Unpaid",
                    generatedAt = LocalDateTime.now().toString()
                )
                
                val id = invoiceRepository.createInvoice(newInvoice)
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Invoice #$id created successfully."
                )
                // Invoices update automatically via Flow
            } catch (e: Exception) {
                 _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to create invoice: ${e.message}"
                )
            }
         }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}
