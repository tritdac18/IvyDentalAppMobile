package com.example.ivydentalapp.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ivydentalapp.data.local.entity.PatientEntity
import com.example.ivydentalapp.data.local.entity.ReceptionistEntity
import com.example.ivydentalapp.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false,
    val currentUser: PatientEntity? = null,
    val currentReceptionist: ReceptionistEntity? = null,
    val userAllergies: List<String> = emptyList(),
    val isRegistrationSuccess: Boolean = false,
    val userType: String? = null, // "patient" or "receptionist"
)

class AuthViewModel(
    private val repository: AuthRepository,
    private val notificationRepository: com.example.ivydentalapp.data.repository.NotificationRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        if (repository.isLoggedIn()) {
            viewModelScope.launch {
                val userType = repository.getCurrentUserType()
                if (userType == "patient") {
                    val user = repository.getCurrentUser()
                    if (user == null) {
                        logout()
                        return@launch
                    }
                    val allergies = repository.getAllergies(user.patientId)
                    _uiState.update { 
                        it.copy(
                            isLoggedIn = true,
                            currentUser = user,
                            currentReceptionist = null,
                            userAllergies = allergies,
                            userType = "patient"
                        )
                    }
                } else if (userType == "receptionist" || userType == "manager") {
                    val receptionist = repository.getCurrentReceptionist()
                    if (receptionist == null) {
                        logout()
                        return@launch
                    }
                    _uiState.update { 
                        it.copy(
                            isLoggedIn = true,
                            currentUser = null,
                            currentReceptionist = receptionist,
                            userAllergies = emptyList(),
                            userType = userType // "receptionist" or "manager"
                        )
                    }
                }
            }
        }
    }

    fun logout() {
        repository.logout()
        _uiState.update { 
            it.copy(
                isLoggedIn = false,
                currentUser = null,
                currentReceptionist = null,
                userAllergies = emptyList(),
                userType = null
            )
        }
    }

    fun login(identifier: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            // Check if identifier exists as patient first
            val isPatient = repository.checkIfPatientExists(identifier)
            val isStaff = repository.checkIfReceptionistExists(identifier)
            
            when {
                isPatient -> {
                    // Try patient login only
                    val patientResult = repository.loginPatient(identifier, password)
                    patientResult.fold(
                        onSuccess = { patient ->
                            val allergies = repository.getAllergies(patient.patientId)
                            _uiState.update { 
                                it.copy(
                                    isLoading = false, 
                                    isLoggedIn = true, 
                                    currentUser = patient,
                                    currentReceptionist = null,
                                    userAllergies = allergies,
                                    userType = "patient",
                                    error = null
                                ) 
                            }
                        },
                        onFailure = { error ->
                            _uiState.update { 
                                it.copy(
                                    isLoading = false, 
                                    error = error.message ?: "Login failed"
                                ) 
                            }
                        }
                    )
                }
                isStaff -> {
                    // Try staff (receptionist/manager) login
                    val receptionistResult = repository.loginReceptionist(identifier, password)
                    receptionistResult.fold(
                        onSuccess = { receptionist ->
                            if (receptionist.status != null && receptionist.status.equals("Inactive", ignoreCase = true)) {
                                _uiState.update { 
                                    it.copy(
                                        isLoading = false,
                                        error = "Account is inactive. Please contact your manager."
                                    )
                                }
                            } else {
                                val type = receptionist.role.lowercase()
                                _uiState.update { 
                                    it.copy(
                                        isLoading = false, 
                                        isLoggedIn = true, 
                                        currentUser = null,
                                        currentReceptionist = receptionist,
                                        userAllergies = emptyList(),
                                        userType = type, // "receptionist" or "manager"
                                        error = null
                                    ) 
                                }
                            }
                        },
                        onFailure = { error ->
                            _uiState.update { 
                                it.copy(
                                    isLoading = false, 
                                    error = error.message ?: "Login failed" // Covers "unauthorized role" from repo
                                ) 
                            }
                        }
                    )
                }
                else -> {
                    // User not found in either table
                    android.util.Log.d("AuthViewModel", "User not found for identifier: $identifier")
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "User not found. Test accounts: receptionist/receptionist123 or manager/manager123"
                        ) 
                    }
                }
            }
        }
    }

    fun register(patient: PatientEntity, allergies: List<String>) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                
                // Register patient directly without email verification
                val verifiedPatient = patient.copy(
                    status = "verified",
                    createdAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                )
                
                android.util.Log.d("AuthViewModel", "Registering patient: ${verifiedPatient.email}")
                val result = repository.registerPatient(verifiedPatient, allergies)
                
                result.fold(
                    onSuccess = {
                        android.util.Log.d("AuthViewModel", "Patient registered successfully")
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                isRegistrationSuccess = true,
                                error = null
                            ) 
                        }
                    },
                    onFailure = { error ->
                        android.util.Log.e("AuthViewModel", "Failed to register patient", error)
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = error.message ?: "Registration failed"
                            ) 
                        }
                    }
                )
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Error during registration", e)
                e.printStackTrace()
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Registration failed: ${e.message}"
                    ) 
                }
            }
        }
    }


    fun updateProfile(patient: PatientEntity, allergies: List<String>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = repository.updatePatient(patient, allergies)
            result.fold(
                onSuccess = {
                    // Refresh current user data
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            currentUser = patient, 
                            userAllergies = allergies,
                            error = null
                        ) 
                    }
                    
                    // Create Notification
                    viewModelScope.launch {
                        notificationRepository.createNotification(
                            userId = patient.patientId,
                            userType = "Patient",
                            title = "Profile Updated",
                            message = "Your profile information has been successfully updated."
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = error.message ?: "Update failed"
                        ) 
                    }
                }
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun resetRegistrationState() {
        _uiState.update { it.copy(isRegistrationSuccess = false) }
    }

    fun updateReceptionistProfile(receptionist: ReceptionistEntity) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = repository.updateReceptionist(receptionist)
            result.fold(
                onSuccess = {
                    // Refresh current receptionist data
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            currentReceptionist = receptionist,
                            error = null
                        ) 
                    }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = error.message ?: "Update failed"
                        ) 
                    }
                }
            )
        }
    }

    fun setValidationError(message: String) {
        _uiState.update { it.copy(error = message) }
    }
}

class AuthViewModelFactory(
    private val repository: AuthRepository,
    private val notificationRepository: com.example.ivydentalapp.data.repository.NotificationRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repository, notificationRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
