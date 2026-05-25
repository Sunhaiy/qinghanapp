package com.example.laisheng.data.remote

object AuthSession {
    @Volatile
    var token: String? = null
        private set

    fun updateToken(newToken: String?) {
        token = newToken?.takeIf { it.isNotBlank() }
    }

    fun clear() {
        token = null
    }
}
