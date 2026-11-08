package com.example.ivydentalapp.ui.manager

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ivydentalapp.IvyDentalApplication
import com.example.ivydentalapp.data.local.entity.PatientEntity
import com.example.ivydentalapp.data.local.entity.ReceptionistEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReadOnlyProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val database = (application as IvyDentalApplication).database

    private val _staff = MutableStateFlow<ReceptionistEntity?>(null)
    val staff: StateFlow<ReceptionistEntity?> = _staff.asStateFlow()

    private val _patient = MutableStateFlow<PatientEntity?>(null)
    val patient: StateFlow<PatientEntity?> = _patient.asStateFlow()

    fun loadStaff(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _staff.value = database.receptionistDao().getReceptionistById(id)
        }
    }

    fun loadPatient(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _patient.value = database.patientDao().getPatientById(id)
        }
    }
}

class ReadOnlyProfileViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReadOnlyProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReadOnlyProfileViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
