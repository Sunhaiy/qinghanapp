package com.example.laisheng.data.model

import com.google.gson.annotations.SerializedName

// 发布瞬间的请求体
data class CreateMomentRequest(
    @SerializedName("user_id") val userId: String,
    val content: MomentContent // 修改这里：改为 MomentContent 对象以适配 JSONB
)

// 点赞/收藏请求体
data class ToggleRequest(
    @SerializedName("user_id") val userId: String,
    @SerializedName("moment_id") val momentId: String
)

// 评论请求体
data class CommentRequest(
    @SerializedName("user_id") val userId: String,
    @SerializedName("moment_id") val momentId: String,
    val content: String
)

// 评论返回实体
data class Comment(
    val id: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("moment_id") val momentId: String,
    val content: String,
    @SerializedName("created_at") val createdAt: String,
    val nickname: String?,
    val avatar: String?
)