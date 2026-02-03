package com.example.laisheng.ui.features.message

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laisheng.data.NetworkModule
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

    init {
        // --- 核心：启动监听 WebSocket 广播 ---
        viewModelScope.launch {
            SocketManager.messageFlow.collect { newMessage ->
                val currentState = _uiState.value
                if (currentState is ChatDetailUiState.Success) {
                    // 判断这条消息是不是当前对话的人发的 (或者是你自己发的)
                    // 注意：如果是你自己发的，已经在 sendMessage 里本地添加了，这里可以加判重
                    val isFromOther = newMessage.senderId != currentState.messages.firstOrNull()?.senderId
                    // 这里简化逻辑：只要属于该会话的消息且不重复，就添加
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
                        // 只有 ID 不在列表里才添加（防止 Socket 重复推送自己发的消息）
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