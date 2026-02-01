package com.example.laisheng.data.model

import com.google.gson.annotations.SerializedName

// 对应 Postman 返回的整体对象
data class MomentResponse(
    val page: Int,
    val limit: Int,
    val data: List<Moment>
)

data class Moment(
    val id: String,
    @SerializedName("user_id") val userId: String,
    val content: String?,
    @SerializedName("likes_count") val likesCount: Int = 0,
    @SerializedName("comments_count") val commentsCount: Int = 0,
    @SerializedName("created_at") val createdAt: String,
    val nickname: String?,
    val handle: String?,
    val avatar: String?
)