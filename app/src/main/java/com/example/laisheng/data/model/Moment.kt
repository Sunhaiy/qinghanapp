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
    val nickname: String?,
    val handle: String?,
    val avatar: String?,
    @SerializedName("is_liked") var isLiked: Boolean = false,
    @SerializedName("is_collected") var isCollected: Boolean = false,
    @SerializedName("ip_location") val ipLocation: String? = null,
    @SerializedName("user_ip_location") val userIpLocation: String? = null
)

data class MomentContent(
    val text: String?,
    val type: String, // text, image, voice, mixed
    val attachments: List<Attachment>? = null
)

data class Attachment(
    val type: String, // image, voice
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

// --- 私信相关模型 ---
data class ChatMessage(
    val id: String,
    @SerializedName("sender_id") val senderId: String,
    @SerializedName("receiver_id") val receiverId: String,
    val content: MessageContent,
    @SerializedName("created_at") val createdAt: String,
    // 详情页可能需要的发送者信息
    val nickname: String? = null,
    val avatar: String? = null
)

data class MessageContent(
    val text: String?,
    val type: String, // text, image, voice
    val attachments: List<Attachment>? = emptyList() // 新增附件支持
)

data class ChatListItem(
    // 对方的 ID
    @SerializedName("user_id", alternate = ["id", "other_user_id", "contact_id"]) val userId: String?,
    val nickname: String?,
    val avatar: String?,
    val handle: String?,
    @SerializedName("last_message") val lastMessage: String?,
    @SerializedName("last_time") val lastTime: String?,
    @SerializedName("unread_count") val unreadCount: Int = 0
)

data class SendMessageRequest(
    @SerializedName("sender_id") val senderId: String,
    @SerializedName("receiver_id") val receiverId: String,
    val content: MessageContent
)