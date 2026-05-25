package com.example.laisheng.ui.features.message

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laisheng.data.local.MessageLocalStore
import com.example.laisheng.data.model.ChatListItem
import com.example.laisheng.data.model.MessageContent
import com.example.laisheng.data.model.NotificationItem
import com.example.laisheng.data.remote.NetworkModule
import com.example.laisheng.data.remote.SocketManager
import com.example.laisheng.data.repository.MomentRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class MessageUiState {
    object Loading : MessageUiState()
    data class Success(
        val notifications: List<NotificationItem>,
        val chatList: List<ChatListItem>
    ) : MessageUiState()
    data class Error(val message: String) : MessageUiState()
}

class MessageViewModel : ViewModel() {
    private val repository = MomentRepository(NetworkModule.apiService)

    private val _uiState = MutableStateFlow<MessageUiState>(MessageUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private var currentUserId: String? = null
    private var pollingJob: Job? = null

    init {
        viewModelScope.launch {
            SocketManager.messageFlow.collect { message ->
                val userId = currentUserId ?: return@collect
                MessageLocalStore.upsertMessage(message, selfUserId = userId)
                loadMessageHub(userId, isSilent = true)
            }
        }
        viewModelScope.launch {
            SocketManager.notificationFlow.collect {
                currentUserId?.let { userId -> loadMessageHub(userId, isSilent = true) }
            }
        }
    }

    fun loadMessageHub(userId: String, isSilent: Boolean = false) {
        currentUserId = userId
        if (pollingJob == null) {
            startPolling()
        }

        viewModelScope.launch {
            if (!isSilent) {
                if (_uiState.value !is MessageUiState.Success) {
                    _uiState.value = MessageUiState.Loading
                }
                _isRefreshing.value = true
            }

            try {
                val notifications = repository.getNotifications(limit = 6)
                val mutualUsers = repository.getMutualFollowing()
                val remoteChatList = repository.getChatList()
                MessageLocalStore.upsertChatList(remoteChatList)

                val cachedChatList = MessageLocalStore.getChatList(userId)
                val mergedChatList = mergeChatList(remoteChatList, cachedChatList, mutualUsers)

                _uiState.value = MessageUiState.Success(
                    notifications = notifications,
                    chatList = mergedChatList
                )
            } catch (e: Exception) {
                if (!isSilent) {
                    _uiState.value = MessageUiState.Error(e.message ?: "获取消息失败")
                }
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    private fun mergeChatList(
        remoteChatList: List<ChatListItem>,
        cachedChatList: List<ChatListItem>,
        mutualUsers: List<com.example.laisheng.data.model.User>
    ): List<ChatListItem> {
        val mergedByUser = linkedMapOf<String, ChatListItem>()

        fun upsert(item: ChatListItem) {
            val userId = item.userId ?: return
            val existing = mergedByUser[userId]
            mergedByUser[userId] = if (existing == null) {
                item
            } else {
                val existingTime = existing.lastTime.orEmpty()
                val itemTime = item.lastTime.orEmpty()
                if (itemTime >= existingTime) {
                    item.copy(
                        nickname = item.nickname ?: existing.nickname,
                        avatar = item.avatar ?: existing.avatar,
                        handle = item.handle ?: existing.handle,
                        unreadCount = maxOf(item.unreadCount, existing.unreadCount)
                    )
                } else {
                    existing.copy(
                        nickname = existing.nickname ?: item.nickname,
                        avatar = existing.avatar ?: item.avatar,
                        handle = existing.handle ?: item.handle,
                        unreadCount = maxOf(existing.unreadCount, item.unreadCount)
                    )
                }
            }
        }

        remoteChatList.forEach(::upsert)
        cachedChatList.forEach(::upsert)

        mutualUsers.forEach { user ->
            val existing = mergedByUser[user.id]
            if (existing == null) {
                mergedByUser[user.id] = ChatListItem(
                    userId = user.id,
                    nickname = user.nickname,
                    avatar = user.avatar,
                    handle = user.handle,
                    messageContent = MessageContent(text = "开始聊天吧"),
                    unreadCount = 0
                )
            } else {
                mergedByUser[user.id] = existing.copy(
                    nickname = existing.nickname ?: user.nickname,
                    avatar = existing.avatar ?: user.avatar,
                    handle = existing.handle ?: user.handle
                )
            }
        }

        return mergedByUser.values.sortedByDescending { it.lastTime.orEmpty() }
    }

    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                delay(2000)
                currentUserId?.let { loadMessageHub(it, isSilent = true) }
            }
        }
    }

    override fun onCleared() {
        pollingJob?.cancel()
        super.onCleared()
    }
}
