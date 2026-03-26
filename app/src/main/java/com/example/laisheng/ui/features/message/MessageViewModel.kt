package com.example.laisheng.ui.features.message

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laisheng.data.model.ChatListItem
import com.example.laisheng.data.model.MessageContent
import com.example.laisheng.data.remote.NetworkModule
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

    private var currentUserId: String? = null

    init {
        viewModelScope.launch {
            SocketManager.messageFlow.collect {
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
                if (_uiState.value !is MessageUiState.Success) {
                    _uiState.value = MessageUiState.Loading
                }
                _isRefreshing.value = true
            }

            try {
                val mutualUsers = repository.getMutualFollowing()
                val chatList = repository.getChatList()
                val chatMap = chatList.associateBy { it.userId }

                val merged = mutualUsers.map { user ->
                    val existing = chatMap[user.id]
                    ChatListItem(
                        userId = user.id,
                        nickname = user.nickname,
                        avatar = user.avatar,
                        handle = user.handle,
                        messageContent = existing?.messageContent ?: MessageContent(text = "开始聊天吧"),
                        lastTime = existing?.lastTime,
                        unreadCount = existing?.unreadCount ?: 0,
                        isRead = existing?.isRead,
                        senderId = existing?.senderId
                    )
                }

                val remainingChats = chatList.filter { item ->
                    val targetId = item.userId ?: return@filter true
                    merged.none { it.userId == targetId }
                }

                _uiState.value = MessageUiState.Success(merged + remainingChats)
            } catch (e: Exception) {
                if (!isSilent) {
                    _uiState.value = MessageUiState.Error(e.message ?: "获取消息列表失败")
                }
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}
