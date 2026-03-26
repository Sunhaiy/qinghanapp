package com.example.laisheng.data.model

import com.google.gson.annotations.SerializedName

data class MomentResponse(
    val page: Int,
    val limit: Int,
    val data: List<Moment>
)

data class Moment(
    val id: String,
    @SerializedName("user_id") val userId: String,
    val content: MomentContent,
    @SerializedName("likes_count") var likesCount: Int = 0,
    @SerializedName("comments_count") val commentsCount: Int = 0,
    @SerializedName("created_at") val createdAt: String,
    val nickname: String? = null,
    val handle: String? = null,
    val avatar: String? = null,
    @SerializedName("is_liked") var isLiked: Boolean = false,
    @SerializedName("is_collected") var isCollected: Boolean = false,
    @SerializedName("ip_location") val ipLocation: String? = null,
    @SerializedName("user_ip_location") val userIpLocation: String? = null,
    @SerializedName("collection_id") val collectionId: String? = null,
    @SerializedName("folder_id") val folderId: String? = null
)

data class MomentContent(
    val text: String? = null,
    val type: String = "text",
    val attachments: List<Attachment>? = null,
    val topic: String? = null
)

data class Attachment(
    val type: String,
    val url: String,
    val width: Int? = null,
    val height: Int? = null,
    val duration: Int? = null
)

data class UploadResponse(
    val message: String,
    val url: String,
    val mimetype: String,
    val size: Int
)

data class FollowCounts(
    @SerializedName("followers_count") val followersCount: Int,
    @SerializedName("following_count") val followingCount: Int
)

data class ChatMessage(
    val id: String,
    @SerializedName("sender_id") val senderId: String,
    @SerializedName("receiver_id") val receiverId: String,
    val content: MessageContent,
    @SerializedName("created_at") val createdAt: String,
    val nickname: String? = null,
    val avatar: String? = null
)

data class MessageContent(
    val text: String? = null,
    val type: String = "text",
    val attachments: List<Attachment>? = emptyList()
)

data class ChatListItem(
    @SerializedName(value = "contact_id", alternate = ["user_id", "id", "other_user_id"]) val userId: String? = null,
    val nickname: String? = null,
    val avatar: String? = null,
    val handle: String? = null,
    @SerializedName("content") val messageContent: MessageContent? = null,
    @SerializedName(value = "created_at", alternate = ["last_time"]) val lastTime: String? = null,
    @SerializedName("unread_count") val unreadCount: Int = 0,
    @SerializedName("is_read") val isRead: Boolean? = null,
    @SerializedName("sender_id") val senderId: String? = null
) {
    val lastMessage: String?
        get() = messageContent?.text
}

data class SendMessageRequest(
    @SerializedName("receiver_id") val receiverId: String,
    val content: MessageContent
)
