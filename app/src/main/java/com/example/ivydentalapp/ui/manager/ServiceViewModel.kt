package com.example.ivydentalapp.ui.manager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ivydentalapp.data.local.entity.ServiceEntity
import com.example.ivydentalapp.data.repository.ServiceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ServiceUiState(
    val isLoading: Boolean = false,
    val services: List<ServiceEntity> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null
)

class ServiceViewModel(
    private val serviceRepository: ServiceRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ServiceUiState())
    val uiState: StateFlow<ServiceUiState> = _uiState.asStateFlow()

    init {
        loadServices()
    }

    fun loadServices() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val services = serviceRepository.getActiveServices()
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        services = services
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message
                    ) 
                }
            }
        }
    }

    fun addService(
        serviceName: String,
        description: String?,
        basePrice: Double,
        durationMinutes: Int
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val service = ServiceEntity(
                    serviceName = serviceName,
                    description = description,
                    basePrice = basePrice,
                    durationMinutes = durationMinutes,
                    isActive = true
                )
                serviceRepository.insertService(service)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        successMessage = "Service added successfully"
                    ) 
                }
                loadServices()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to add service"
                    ) 
                }
            }
        }
    }

    fun updateService(service: ServiceEntity) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                serviceRepository.updateService(service)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        successMessage = "Service updated successfully"
                    ) 
                }
                loadServices()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to update service"
                    ) 
                }
            }
        }
    }

    fun deleteService(serviceId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                serviceRepository.deleteService(serviceId)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        successMessage = "Service deleted successfully"
                    ) 
                }
                loadServices()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to delete service"
                    ) 
                }
            }
        }
    }

    fun toggleServiceStatus(service: ServiceEntity) {
        viewModelScope.launch {
            try {
                val updatedService = service.copy(isActive = !service.isActive)
                serviceRepository.updateService(updatedService)
                loadServices()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = e.message ?: "Failed to update service status") 
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

class ServiceViewModelFactory(
    private val serviceRepository: ServiceRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ServiceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ServiceViewModel(serviceRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

