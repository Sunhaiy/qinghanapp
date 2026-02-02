package com.example.laisheng.ui.features.message

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laisheng.data.NetworkModule
import com.example.laisheng.data.model.ChatListItem
import com.example.laisheng.data.repository.MomentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class MessageUiState {
    object Loading : MessageUiState()
    data class Success(val chatList: List<ChatListItem>) : MessageUiState()
    data class Error(val message: String) : MessageUiState()
}

class MessageViewModel : ViewModel() {
    private val repository = MomentRepository(NetworkModule.apiService)

    private val _uiState = MutableStateFlow<MessageUiState>(MessageUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    fun loadChatList(userId: String) {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val list = repository.getChatList(userId)
                _uiState.value = MessageUiState.Success(list)
            } catch (e: Exception) {
                _uiState.value = MessageUiState.Error(e.message ?: "获取消息失败")
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}