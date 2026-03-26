package com.example.laisheng.data.remote

import android.util.Log
import com.example.laisheng.data.model.ChatMessage
import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.json.JSONObject

object SocketManager {
    private var socket: Socket? = null
    private val gson = Gson()

    private val _messageFlow = MutableSharedFlow<ChatMessage>(extraBufferCapacity = 64)
    val messageFlow = _messageFlow.asSharedFlow()

    fun connect(userId: String) {
        if (socket?.connected() == true) return

        try {
            val opts = IO.Options().apply {
                forceNew = true
                reconnection = true
            }

            socket = IO.socket(NetworkModule.BASE_URL, opts)

            socket?.on(Socket.EVENT_CONNECT) {
                Log.d("SocketManager", "Connected! Registering user: $userId")
                socket?.emit("register", userId)
            }

            socket?.on("new_message") { args ->
                val data = args.firstOrNull() ?: return@on
                try {
                    val messageJson = if (data is JSONObject) data.toString() else data.toString()
                    val chatMessage = gson.fromJson(messageJson, ChatMessage::class.java) ?: return@on
                    Log.d("SocketManager", "Broadcast new message: ${chatMessage.content.text}")
                    _messageFlow.tryEmit(chatMessage)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            socket?.connect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun disconnect() {
        socket?.disconnect()
        socket = null
    }
}
