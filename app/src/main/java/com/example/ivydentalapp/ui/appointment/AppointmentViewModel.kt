package com.example.ivydentalapp.ui.appointment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ivydentalapp.data.local.entity.AppointmentEntity
import com.example.ivydentalapp.data.local.entity.InvoiceEntity
import com.example.ivydentalapp.data.local.entity.PaymentEntity
import com.example.ivydentalapp.data.repository.AppointmentRepository
import com.example.ivydentalapp.data.repository.InvoiceRepository
import com.example.ivydentalapp.data.service.PaymentSimulationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class AppointmentUiState(
    val isLoading: Boolean = false,
    val appointments: List<AppointmentEntity> = emptyList(),
    val error: String? = null,
    val isBookingSuccess: Boolean = false,
    val showDownpaymentDialog: Boolean = false,
    val pendingAppointment: PendingAppointment? = null
)

data class PendingAppointment(
    val patientId: Int,
    val service: Service,
    val doctor: Doctor,
    val date: String,
    val time: String
)

data class Doctor(val id: Int, val name: String, val specialization: String)
data class Service(val id: Int, val name: String, val price: Double, val durationMinutes: Int)

class AppointmentViewModel(
    private val repository: AppointmentRepository,
    private val invoiceRepository: InvoiceRepository,
    private val paymentService: PaymentSimulationService,
    private val notificationRepository: com.example.ivydentalapp.data.repository.NotificationRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AppointmentUiState())
    val uiState: StateFlow<AppointmentUiState> = _uiState.asStateFlow()

    // Appointments for a specific dentist & date (used for availability checks)
    private val _dayAppointments = MutableStateFlow<List<AppointmentEntity>>(emptyList())
    val dayAppointments: StateFlow<List<AppointmentEntity>> = _dayAppointments.asStateFlow()

    // Demo Data
    val availableDoctors = listOf(
        Doctor(1, "Dr. Sarah Smith", "General Dentist"),
        Doctor(2, "Dr. John Doe", "Orthodontist"),
        Doctor(3, "Dr. Emily White", "Endodontist"),
        Doctor(4, "Dr. Michael Brown", "Oral Surgeon")
    )

    // Available services (aligned with default ServiceRepository)
    val availableServices = listOf(
        Service(1, "General Checkup", 500.0, 30),
        Service(2, "Teeth Cleaning", 800.0, 45),
        Service(3, "Extraction", 1200.0, 60),
        Service(4, "Root Canal", 5000.0, 90),
        Service(5, "Braces Consultation", 1500.0, 30),
        Service(6, "Dental X-Ray", 1000.0, 30),
        Service(7, "Braces Attachments", 8000.0, 60)
    )

    // Service prerequisites
    private val serviceRequirements: Map<String, List<String>> = mapOf(
        "Braces Attachments" to listOf("Dental X-Ray", "Braces Consultation")
    )

    fun loadDoctorDayAppointments(dentistId: Int, date: String) {
        viewModelScope.launch {
            try {
                val appointments = repository.getDentistAppointmentsByDate(dentistId, date)
                _dayAppointments.value = appointments
            } catch (e: Exception) {
                // We don't surface this as a hard error to keep booking flow smooth
                _dayAppointments.value = emptyList()
            }
        }
    }

    fun clearDoctorDayAppointments() {
        _dayAppointments.value = emptyList()
    }

    fun loadAppointments(patientId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                repository.getAppointments(patientId).collect { appointments ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            appointments = appointments
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

    fun proceedToDownpayment(patientId: Int, service: Service, doctor: Doctor, date: String, time: String) {
        viewModelScope.launch {
            // Basic validation
            if (date.isBlank() || time.isBlank()) {
                _uiState.update { it.copy(error = "Please select date and time") }
                return@launch
            }

            // Validate that the selected datetime is not in the past
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a", Locale.US)
            val selectedDateTime = try {
                LocalDateTime.parse("$date $time", formatter)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Invalid date or time format") }
                return@launch
            }

            if (!selectedDateTime.isAfter(LocalDateTime.now())) {
                _uiState.update {
                    it.copy(
                        error = "You cannot book an appointment in the past or for a time that has already passed."
                    )
                }
                return@launch
            }

            // Check service prerequisites
            val requiredServices = serviceRequirements[service.name].orEmpty()
            if (requiredServices.isNotEmpty()) {
                try {
                    val completedAppointments = repository.getCompletedAppointments(patientId)
                    val completedServiceNames = completedAppointments
                        .mapNotNull { it.serviceType }
                        .toSet()

                    val missing = requiredServices.filter { it !in completedServiceNames }
                    if (missing.isNotEmpty()) {
                        _uiState.update { 
                            it.copy(
                                error = "This service requires completed appointments for: ${missing.joinToString(", ")} before you can proceed."
                            ) 
                        }
                        return@launch
                    }
                } catch (e: Exception) {
                    _uiState.update { it.copy(error = e.message ?: "Failed to verify service requirements") }
                    return@launch
                }
            }
            
            _uiState.update { 
                it.copy(
                    showDownpaymentDialog = true,
                    pendingAppointment = PendingAppointment(patientId, service, doctor, date, time),
                    error = null
                ) 
            }
        }
    }
    
    fun dismissDownpaymentDialog() {
        _uiState.update { it.copy(showDownpaymentDialog = false, pendingAppointment = null) }
    }
    
    fun resetBookingSuccess() {
        _uiState.update { it.copy(isBookingSuccess = false) }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun processDownpayment(referenceNumber: String, accountName: String) {

        viewModelScope.launch {
            val pending = _uiState.value.pendingAppointment ?: return@launch
            
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            // Calculate downpayment
            val downpaymentAmount = pending.service.price * 0.5
            
            // Create GCash payment
            val paymentId = paymentService.createGCashPayment(
                invoiceId = 0, // Pending
                amount = downpaymentAmount,
                referenceNumber = referenceNumber,
                accountName = accountName,
                patientId = pending.patientId,
                appointmentId = null // Pending
            )
            
            // Create Appointment
            val appointment = AppointmentEntity(
                patientId = pending.patientId,
                dentistId = pending.doctor.id,
                scheduledDatetime = "${pending.date} ${pending.time}",
                durationMinutes = pending.service.durationMinutes,
                serviceType = pending.service.name,
                price = pending.service.price,
                serviceId = pending.service.id,
                status = "Scheduled"
            )

            val result = repository.bookAppointment(appointment)
            
            result.fold(
                onSuccess = { appointmentId ->
                    // Stop Automatic Invoice Creation
                    /*
                    val invoice = InvoiceEntity(...)
                    val invoiceId = invoiceRepository.createInvoice(invoice)
                    */
                    
                    // Update Payment to link to Appointment
                    val payment = paymentService.getPaymentById(paymentId.toInt())
                    payment?.let {
                        val updatedPayment = it.copy(
                            // invoiceId remains 0 or whatever it was
                            appointmentId = appointmentId.toInt(),
                            paymentType = "Downpayment",
                            status = "Completed"
                        )
                        paymentService.updatePayment(updatedPayment)
                    }
                    
                    // Create Notification
                    notificationRepository.createNotification(
                        userId = pending.patientId,
                        userType = "Patient",
                        title = "Appointment Confirmed",
                        message = "Your appointment for ${pending.service.name} on ${pending.date} at ${pending.time} is successfully booked."
                    )
                    
                    // Success State
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            isBookingSuccess = true,
                            showDownpaymentDialog = false,
                            pendingAppointment = null
                        ) 
                    }
                },
                onFailure = { e ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = e.message ?: "Booking failed",
                            showDownpaymentDialog = false
                        ) 
                    }
                }
            )
        }
    }
    
    // ... existing methods ...
    
    // ... (skip down to Factory)

}

class AppointmentViewModelFactory(
    private val repository: AppointmentRepository,
    private val invoiceRepository: InvoiceRepository,
    private val paymentService: PaymentSimulationService,
    private val notificationRepository: com.example.ivydentalapp.data.repository.NotificationRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppointmentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppointmentViewModel(repository, invoiceRepository, paymentService, notificationRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
