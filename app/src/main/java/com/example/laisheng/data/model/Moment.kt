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
    @SerializedName("is_collected") var isCollected: Boolean = false
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