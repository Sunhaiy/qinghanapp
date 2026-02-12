package com.example.laisheng.ui.features.message

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laisheng.data.remote.NetworkModule
import com.example.laisheng.data.model.ChatMessage
import com.example.laisheng.data.remote.SocketManager
import com.example.laisheng.data.repository.MomentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ChatDetailUiState {
    object Loading : ChatDetailUiState()
    data class Success(val messages: List<ChatMessage>) : ChatDetailUiState()
    data class Error(val message: String) : ChatDetailUiState()
}

class ChatDetailViewModel : ViewModel() {
    private val repository = MomentRepository(NetworkModule.apiService)

    private val _uiState = MutableStateFlow<ChatDetailUiState>(ChatDetailUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending = _isSending.asStateFlow()

    private val _myAvatar = MutableStateFlow<String?>(null)
    val myAvatar = _myAvatar.asStateFlow()

    init {
        viewModelScope.launch {
            SocketManager.messageFlow.collect { newMessage ->
                val currentState = _uiState.value
                if (currentState is ChatDetailUiState.Success) {
                    if (newMessage.id !in currentState.messages.map { it.id }) {
                        _uiState.value = currentState.copy(
                            messages = currentState.messages + newMessage
                        )
                    }
                }
            }
        }
    }

    fun loadHistory(userId: String, otherId: String) {
        viewModelScope.launch {
            // 加载自己的头像
            val user = repository.getUserProfile(userId)
            _myAvatar.value = user?.avatar

            _uiState.value = ChatDetailUiState.Loading
            try {
                val history = repository.getChatHistory(userId, otherId)
                _uiState.value = ChatDetailUiState.Success(history.reversed())
            } catch (e: Exception) {
                _uiState.value = ChatDetailUiState.Error(e.message ?: "加载失败")
            }
        }
    }

    fun sendMessage(senderId: String, receiverId: String, text: String) {
        if (text.isBlank()) return
        
        viewModelScope.launch {
            _isSending.value = true
            try {
                val newMessage = repository.sendMessage(senderId, receiverId, text)
                if (newMessage != null) {
                    val currentState = _uiState.value
                    if (currentState is ChatDetailUiState.Success) {
                        if (newMessage.id !in currentState.messages.map { it.id }) {
                            _uiState.value = currentState.copy(
                                messages = currentState.messages + newMessage
                            )
                        }
                    }
                }
            } catch (e: Exception) {
            } finally {
                _isSending.value = false
            }
        }
    }
}