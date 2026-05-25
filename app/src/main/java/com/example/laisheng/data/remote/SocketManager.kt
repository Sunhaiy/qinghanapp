package com.example.laisheng.data.remote

import android.util.Log
import com.example.laisheng.data.model.ChatMessage
import com.example.laisheng.data.model.NotificationItem
import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.json.JSONObject

object SocketManager {
    private const val TAG = "SocketManager"

    private var socket: Socket? = null
    private val gson = Gson()

    private val _messageFlow = MutableSharedFlow<ChatMessage>(extraBufferCapacity = 64)
    val messageFlow = _messageFlow.asSharedFlow()

    private val _notificationFlow = MutableSharedFlow<NotificationItem>(extraBufferCapacity = 32)
    val notificationFlow = _notificationFlow.asSharedFlow()

    fun connect(userId: String) {
        val token = AuthSession.token
        if (socket?.connected() == true || token.isNullOrBlank()) return

        try {
            val opts = IO.Options().apply {
                forceNew = true
                reconnection = true
                timeout = 10000
                auth = mapOf("token" to token)
            }

            socket = IO.socket(NetworkModule.BASE_URL, opts)
            registerSocketListeners()
            socket?.connect()
        } catch (e: Exception) {
            Log.e(TAG, "Socket init failed", e)
        }
    }

    private fun registerSocketListeners() {
        socket?.on(Socket.EVENT_CONNECT) {
            Log.d(TAG, "Connected with JWT auth")
        }

        socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
            Log.e(TAG, "Connect error: ${args.firstOrNull()}")
        }

        socket?.on(Socket.EVENT_DISCONNECT) { args ->
            Log.d(TAG, "Disconnected: ${args.firstOrNull()}")
        }

        socket?.on("new_message") { args ->
            val data = args.firstOrNull() ?: return@on
            parseMessage(data)?.let {
                Log.d(TAG, "Receive new_message id=${it.id}")
                _messageFlow.tryEmit(it)
            }
        }

        socket?.on("new_notification") { args ->
            val data = args.firstOrNull() ?: return@on
            parseNotification(data)?.let {
                Log.d(TAG, "Receive new_notification id=${it.id}")
                _notificationFlow.tryEmit(it)
            }
        }
    }

    private fun parseMessage(data: Any): ChatMessage? {
        return try {
            val rawJson = when (data) {
                is JSONObject -> data.toString()
                else -> gson.toJson(data)
            }
            gson.fromJson(rawJson, ChatMessage::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Parse message failed", e)
            null
        }
    }

    private fun parseNotification(data: Any): NotificationItem? {
        return try {
            val rawJson = when (data) {
                is JSONObject -> data.toString()
                else -> gson.toJson(data)
            }
            gson.fromJson(rawJson, NotificationItem::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Parse notification failed", e)
            null
        }
    }

    fun disconnect() {
        socket?.disconnect()
        socket?.off()
        socket = null
    }
}
