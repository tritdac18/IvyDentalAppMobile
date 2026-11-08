package com.example.ivydentalapp.ui.receptionist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ivydentalapp.data.local.entity.AppointmentEntity
import com.example.ivydentalapp.data.local.entity.NotificationEntity
import com.example.ivydentalapp.data.repository.AppointmentRepository
import com.example.ivydentalapp.data.repository.NotificationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class ReceptionistAppointmentViewModel(
    application: Application,
    private val appointmentRepository: AppointmentRepository,
    private val notificationRepository: NotificationRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AppointmentUiState())
    val uiState: StateFlow<AppointmentUiState> = _uiState.asStateFlow()

    data class AppointmentUiState(
        val isLoading: Boolean = false,
        val error: String? = null,
        val successMessage: String? = null
    )

    fun bookWalkInAppointment(
        patientId: Int,
        dentistId: Int?,
        serviceType: String,
        price: Double,
        scheduledDateTime: String, // Expecting "yyyy-MM-dd hh:mm a"
        durationMinutes: Int
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // Prevent booking for past date/time
                val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a", java.util.Locale.US)
                val selectedDateTime = try {
                    java.time.LocalDateTime.parse(scheduledDateTime, formatter)
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Invalid date or time format"
                    )
                    return@launch
                }

                if (!selectedDateTime.isAfter(java.time.LocalDateTime.now())) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "You cannot book an appointment in the past or for a time that has already passed."
                    )
                    return@launch
                }

                val newAppointment = AppointmentEntity(
                    patientId = patientId,
                    dentistId = dentistId ?: 0,
                    scheduledDatetime = scheduledDateTime,
                    durationMinutes = durationMinutes,
                    status = "Scheduled",
                    serviceType = serviceType,
                    price = price,
                    serviceId = null // Assuming optional or looked up if needed, but entity has nullable
                )

                // Use bookAppointment which returns Result
                val result = appointmentRepository.bookAppointment(newAppointment)
                
                if (result.isSuccess) {
                    val appointmentId = result.getOrNull() ?: 0

                    // Notify Patient
                    notificationRepository.createNotification(
                        userId = patientId,
                        userType = "Patient",
                        title = "Appointment Booked",
                        message = "Your walk-in appointment for $serviceType has been scheduled for $scheduledDateTime."
                    )

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Appointment booked successfully (ID: $appointmentId)"
                    )
                } else {
                    throw Exception(result.exceptionOrNull()?.message ?: "Unknown error")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to book appointment: ${e.message}"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}
