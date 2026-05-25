package com.example.laisheng.ui.features.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laisheng.data.model.NotificationItem
import com.example.laisheng.data.remote.NetworkModule
import com.example.laisheng.data.repository.MomentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class NotificationUiState {
    object Loading : NotificationUiState()
    data class Success(val items: List<NotificationItem>) : NotificationUiState()
    data class Error(val message: String) : NotificationUiState()
}

class NotificationViewModel : ViewModel() {
    private val repository = MomentRepository(NetworkModule.apiService)

    private val _uiState = MutableStateFlow<NotificationUiState>(NotificationUiState.Loading)
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    fun refreshUnreadCount() {
        viewModelScope.launch {
            _unreadCount.value = repository.getUnreadNotifications()
        }
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _uiState.value = NotificationUiState.Loading
            try {
                val items = repository.getNotifications()
                _uiState.value = NotificationUiState.Success(items)
                refreshUnreadCount()
            } catch (e: Exception) {
                _uiState.value = NotificationUiState.Error(e.message ?: "加载通知失败")
            }
        }
    }
}
