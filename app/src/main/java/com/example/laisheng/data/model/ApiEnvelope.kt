package com.example.laisheng.data.model

import com.google.gson.annotations.SerializedName

data class ListEnvelope<T>(
    @SerializedName(value = "value", alternate = ["data"]) val value: List<T> = emptyList(),
    @SerializedName(value = "Count", alternate = ["count"]) val count: Int = value.size
)

data class ToggleResult(
    val liked: Boolean? = null,
    val collected: Boolean? = null,
    val followed: Boolean? = null
)

data class UnreadCountResponse(
    @SerializedName("unread_count") val unreadCount: Int = 0
)
