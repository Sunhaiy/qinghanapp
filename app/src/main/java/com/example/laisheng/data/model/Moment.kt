package com.example.laisheng.data.model

import com.google.gson.annotations.SerializedName

data class MomentResponse(
    val data: List<Moment>
)

data class Moment(
    val id: String,
    @SerializedName("user_id") val userId: String,
    val content: String?,
    @SerializedName("likes_count") var likesCount: Int = 0,
    @SerializedName("comments_count") val commentsCount: Int = 0,
    @SerializedName("created_at") val createdAt: String,
    val nickname: String?,
    val handle: String?,
    val avatar: String?,
    
    // 关键修复：添加 SerializedName，后端返回的是下划线命名的 is_liked 和 is_collected
    @SerializedName("is_liked") var isLiked: Boolean = false,
    @SerializedName("is_collected") var isCollected: Boolean = false
)