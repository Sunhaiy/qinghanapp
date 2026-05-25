package com.example.laisheng.data.local

import android.content.Context
import com.example.laisheng.data.model.ChatListItem
import com.example.laisheng.data.model.ChatMessage
import com.example.laisheng.data.model.MessageContent
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object MessageLocalStore {
    private data class PeerMeta(
        val nickname: String? = null,
        val avatar: String? = null,
        val handle: String? = null
    )

    private const val PREFS_NAME = "message_local_store"
    private const val KEY_MESSAGES = "messages"
    private const val KEY_PEER_META = "peer_meta"

    private val gson = Gson()
    private val messages = linkedMapOf<String, ChatMessage>()
    private val peerMeta = mutableMapOf<String, PeerMeta>()
    private var prefs: android.content.SharedPreferences? = null

    @Synchronized
    fun init(context: Context) {
        if (prefs != null) return
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        restore()
    }

    @Synchronized
    fun upsertChatList(items: List<ChatListItem>) {
        items.forEach { item ->
            val peerId = item.userId ?: return@forEach
            val existing = peerMeta[peerId]
            peerMeta[peerId] = PeerMeta(
                nickname = item.nickname ?: existing?.nickname,
                avatar = item.avatar ?: existing?.avatar,
                handle = item.handle ?: existing?.handle
            )
        }
        persist()
    }

    @Synchronized
    fun bindPeer(peerId: String, nickname: String? = null, avatar: String? = null, handle: String? = null) {
        if (peerId.isBlank()) return
        val existing = peerMeta[peerId]
        peerMeta[peerId] = PeerMeta(
            nickname = nickname ?: existing?.nickname,
            avatar = avatar ?: existing?.avatar,
            handle = handle ?: existing?.handle
        )
        persist()
    }

    @Synchronized
    fun upsertMessage(message: ChatMessage, selfUserId: String? = null, fallbackPeerId: String? = null) {
        if (message.id.isBlank()) return
        messages[message.id] = message

        val peerId = when {
            !selfUserId.isNullOrBlank() && message.senderId == selfUserId -> message.receiverId
            !selfUserId.isNullOrBlank() && message.receiverId == selfUserId -> message.senderId
            !fallbackPeerId.isNullOrBlank() -> fallbackPeerId
            else -> null
        }

        if (!peerId.isNullOrBlank()) {
            bindPeer(
                peerId = peerId,
                nickname = message.nickname,
                avatar = message.avatar
            )
        }
        persist()
    }

    @Synchronized
    fun upsertMessages(
        incoming: List<ChatMessage>,
        selfUserId: String? = null,
        fallbackPeerId: String? = null
    ) {
        incoming.forEach { upsertMessage(it, selfUserId, fallbackPeerId) }
    }

    @Synchronized
    fun getConversation(selfUserId: String, otherUserId: String): List<ChatMessage> =
        messages.values
            .filter { message ->
                (message.senderId == selfUserId && message.receiverId == otherUserId) ||
                    (message.senderId == otherUserId && message.receiverId == selfUserId)
            }
            .sortedBy { it.createdAt }

    @Synchronized
    fun getChatList(selfUserId: String): List<ChatListItem> =
        messages.values
            .groupBy { message ->
                if (message.senderId == selfUserId) message.receiverId else message.senderId
            }
            .mapNotNull { (peerId, peerMessages) ->
                val latest = peerMessages.maxByOrNull { it.createdAt } ?: return@mapNotNull null
                val meta = peerMeta[peerId]
                ChatListItem(
                    userId = peerId,
                    nickname = meta?.nickname ?: latest.nickname,
                    avatar = meta?.avatar ?: latest.avatar,
                    handle = meta?.handle,
                    messageContent = latest.content.takeIf { !it.text.isNullOrBlank() }
                        ?: MessageContent(text = ""),
                    lastTime = latest.createdAt,
                    unreadCount = 0,
                    isRead = true,
                    senderId = latest.senderId
                )
            }
            .sortedByDescending { it.lastTime }

    @Synchronized
    private fun persist() {
        val sharedPrefs = prefs ?: return
        sharedPrefs.edit()
            .putString(KEY_MESSAGES, gson.toJson(messages.values.toList()))
            .putString(KEY_PEER_META, gson.toJson(peerMeta))
            .apply()
    }

    @Synchronized
    private fun restore() {
        val sharedPrefs = prefs ?: return
        messages.clear()
        peerMeta.clear()

        val messagesJson = sharedPrefs.getString(KEY_MESSAGES, null)
        if (!messagesJson.isNullOrBlank()) {
            val type = object : TypeToken<List<ChatMessage>>() {}.type
            runCatching { gson.fromJson<List<ChatMessage>>(messagesJson, type) }
                .getOrNull()
                .orEmpty()
                .forEach { message -> messages[message.id] = message }
        }

        val peersJson = sharedPrefs.getString(KEY_PEER_META, null)
        if (!peersJson.isNullOrBlank()) {
            val type = object : TypeToken<Map<String, PeerMeta>>() {}.type
            runCatching { gson.fromJson<Map<String, PeerMeta>>(peersJson, type) }
                .getOrNull()
                .orEmpty()
                .forEach { (key, value) -> peerMeta[key] = value }
        }
    }
}
