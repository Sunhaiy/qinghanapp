package com.example.laisheng.ui.features.message

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laisheng.data.local.MessageLocalStore
import com.example.laisheng.data.model.ChatMessage
import com.example.laisheng.data.remote.NetworkModule
import com.example.laisheng.data.remote.SocketManager
import com.example.laisheng.data.repository.MomentRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

    private var selfUserId: String? = null
    private var targetUserId: String? = null
    private var targetNickname: String? = null
    private var targetAvatar: String? = null
    private var pollingJob: Job? = null

    init {
        viewModelScope.launch {
            SocketManager.messageFlow.collect { newMessage ->
                val me = selfUserId ?: return@collect
                val other = targetUserId ?: return@collect
                val belongsToCurrentChat =
                    (newMessage.senderId == me && newMessage.receiverId == other) ||
                        (newMessage.senderId == other && newMessage.receiverId == me)
                if (!belongsToCurrentChat) return@collect

                MessageLocalStore.upsertMessage(newMessage, selfUserId = me, fallbackPeerId = other)
                mergeMessages(listOf(newMessage))
            }
        }
    }

    fun loadHistory(
        userId: String,
        otherId: String,
        otherNickname: String? = null,
        otherAvatar: String? = null,
        forceRefresh: Boolean = false
    ) {
        selfUserId = userId
        targetUserId = otherId
        targetNickname = otherNickname ?: targetNickname
        targetAvatar = otherAvatar ?: targetAvatar
        MessageLocalStore.bindPeer(otherId, nickname = targetNickname, avatar = targetAvatar)

        if (pollingJob == null) {
            startPolling()
        }

        viewModelScope.launch {
            if (!forceRefresh || _uiState.value !is ChatDetailUiState.Success) {
                _uiState.value = ChatDetailUiState.Loading
            }
            try {
                val user = repository.getUserProfile(userId)
                _myAvatar.value = user?.avatar

                val cached = MessageLocalStore.getConversation(userId, otherId)
                if (cached.isNotEmpty()) {
                    mergeMessages(cached)
                }

                val history = repository.getChatHistory(userId, otherId)
                mergeMessages(history)
            } catch (e: Exception) {
                if (_uiState.value !is ChatDetailUiState.Success) {
                    _uiState.value = ChatDetailUiState.Error(e.message ?: "加载聊天记录失败")
                }
            }
        }
    }

    fun sendMessage(senderId: String, receiverId: String, text: String) {
        if (text.isBlank()) return

        viewModelScope.launch {
            _isSending.value = true
            try {
                MessageLocalStore.bindPeer(receiverId, nickname = targetNickname, avatar = targetAvatar)
                val newMessage = repository.sendMessage(senderId, receiverId, text)
                if (newMessage != null) {
                    MessageLocalStore.upsertMessage(newMessage, selfUserId = senderId, fallbackPeerId = receiverId)
                    mergeMessages(listOf(newMessage))
                } else {
                    loadHistory(senderId, receiverId, targetNickname, targetAvatar, forceRefresh = true)
                }
            } finally {
                _isSending.value = false
            }
        }
    }

    private fun mergeMessages(incoming: List<ChatMessage>) {
        val me = selfUserId
        val other = targetUserId

        if (incoming.isNotEmpty()) {
            MessageLocalStore.upsertMessages(incoming, selfUserId = me, fallbackPeerId = other)
        }

        val currentMessages = (_uiState.value as? ChatDetailUiState.Success)?.messages.orEmpty()
        val cachedMessages = if (!me.isNullOrBlank() && !other.isNullOrBlank()) {
            MessageLocalStore.getConversation(me, other)
        } else {
            emptyList()
        }

        val merged = (currentMessages + cachedMessages + incoming)
            .filter { it.id.isNotBlank() }
            .distinctBy { it.id }
            .sortedBy { it.createdAt }

        _uiState.value = ChatDetailUiState.Success(merged)
    }

    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                delay(2000)
                val me = selfUserId ?: continue
                val other = targetUserId ?: continue
                loadHistory(me, other, targetNickname, targetAvatar, forceRefresh = true)
            }
        }
    }

    override fun onCleared() {
        pollingJob?.cancel()
        super.onCleared()
    }
}
