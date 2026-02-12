package com.example.laisheng.ui.features.message

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laisheng.data.remote.NetworkModule
import com.example.laisheng.data.model.ChatListItem
import com.example.laisheng.data.remote.SocketManager
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

    // 缓存当前用户 ID，方便收到消息时刷新
    private var currentUserId: String? = null

    init {
        // --- 核心：监听 WebSocket 广播 ---
        viewModelScope.launch {
            SocketManager.messageFlow.collect {
                // 只要有新消息进来，就自动刷新列表
                currentUserId?.let { userId ->
                    loadChatList(userId, isSilent = true)
                }
            }
        }
    }

    fun loadChatList(userId: String, isSilent: Boolean = false) {
        currentUserId = userId
        viewModelScope.launch {
            if (!isSilent) {
                // 如果不是静默刷新，且当前没有数据，才显示全屏加载
                if (_uiState.value !is MessageUiState.Success) {
                    _uiState.value = MessageUiState.Loading
                }
                _isRefreshing.value = true
            }
            
            try {
                val list = repository.getChatList(userId)
                _uiState.value = MessageUiState.Success(list)
            } catch (e: Exception) {
                if (!isSilent) {
                    _uiState.value = MessageUiState.Error(e.message ?: "获取消息失败")
                }
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}