package com.example.ivydentalapp.ui.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ivydentalapp.data.local.entity.NotificationEntity
import com.example.ivydentalapp.data.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotificationUiState(
    val notifications: List<NotificationEntity> = emptyList(),
    val hasUnread: Boolean = false
)

class NotificationViewModel(
    private val repository: NotificationRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    fun loadNotifications(userId: Int, userType: String) {
        viewModelScope.launch {
            repository.getUserNotifications(userId, userType).collect { notifications ->
                _uiState.update { 
                    it.copy(
                        notifications = notifications,
                        hasUnread = notifications.any { !it.isRead }
                    ) 
                }
            }
        }
    }
    
    fun markAsRead(notificationId: Int) {
        viewModelScope.launch {
            repository.markAsRead(notificationId)
        }
    }
    
    fun markAllAsRead(userId: Int, userType: String) {
        viewModelScope.launch {
            repository.markAllAsRead(userId, userType)
        }
    }
}

class NotificationViewModelFactory(
    private val repository: NotificationRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NotificationViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
