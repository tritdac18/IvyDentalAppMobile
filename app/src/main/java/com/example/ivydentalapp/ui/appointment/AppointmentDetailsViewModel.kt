package com.example.ivydentalapp.ui.appointment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ivydentalapp.IvyDentalApplication
import com.example.ivydentalapp.data.local.entity.AppointmentEntity
import com.example.ivydentalapp.data.local.entity.PatientEntity
import com.example.ivydentalapp.data.local.entity.ReceptionistEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppointmentDetailsViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as IvyDentalApplication
    
    private val _appointment = MutableStateFlow<AppointmentEntity?>(null)
    val appointment: StateFlow<AppointmentEntity?> = _appointment.asStateFlow()
    
    private val _patient = MutableStateFlow<PatientEntity?>(null)
    val patient: StateFlow<PatientEntity?> = _patient.asStateFlow()
    
    private val _dentist = MutableStateFlow<ReceptionistEntity?>(null)
    val dentist: StateFlow<ReceptionistEntity?> = _dentist.asStateFlow()

    fun loadAppointment(appointmentId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val appt = app.database.appointmentDao().getAppointmentById(appointmentId)
            _appointment.value = appt
            
            if (appt != null) {
                // Load patient
                _patient.value = app.database.patientDao().getPatientById(appt.patientId)
                
                // Load dentist (assuming dentistId is in appointment or we need to find who is "Dentist")
                // The AppointmentEntity currently stores dentistId.
                if (appt.dentistId != 0) {
                     _dentist.value = app.database.receptionistDao().getReceptionistById(appt.dentistId)
                }
            }
        }
    }
}

class AppointmentDetailsViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppointmentDetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppointmentDetailsViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
