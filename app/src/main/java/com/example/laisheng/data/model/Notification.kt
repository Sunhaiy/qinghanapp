package com.example.laisheng.data.model

import com.google.gson.annotations.SerializedName

data class NotificationItem(
    val id: String,
    @SerializedName("receiver_id") val receiverId: String? = null,
    @SerializedName("sender_id") val senderId: String? = null,
    val type: String = "",
    @SerializedName("moment_id") val momentId: String? = null,
    val content: String? = null,
    @SerializedName("is_read") val isRead: Boolean = false,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("sender_nickname") val senderNickname: String? = null,
    @SerializedName("sender_avatar") val senderAvatar: String? = null,
    @SerializedName("moment_preview") val momentPreview: String? = null
) {
    val title: String
        get() = when (type.lowercase()) {
            "follow" -> "新的关注"
            "like" -> "收到了点赞"
            "comment" -> "收到了评论"
            "collection" -> "收到了收藏"
            "message" -> "新的私信提醒"
            else -> "新的通知"
        }
}
