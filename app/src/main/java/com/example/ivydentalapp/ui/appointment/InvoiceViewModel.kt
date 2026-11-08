package com.example.ivydentalapp.ui.appointment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ivydentalapp.data.local.entity.InvoiceEntity
import com.example.ivydentalapp.data.repository.InvoiceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InvoiceUiState(
    val isLoading: Boolean = false,
    val invoices: List<InvoiceEntity> = emptyList(),
    val error: String? = null
)

class InvoiceViewModel(private val repository: InvoiceRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(InvoiceUiState())
    val uiState: StateFlow<InvoiceUiState> = _uiState.asStateFlow()

    fun loadInvoices(patientId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                repository.getInvoices(patientId).collect { invoices ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            invoices = invoices
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
}

class InvoiceViewModelFactory(private val repository: InvoiceRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InvoiceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InvoiceViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
