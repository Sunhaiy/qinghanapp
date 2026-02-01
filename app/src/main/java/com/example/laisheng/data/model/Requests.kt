package com.example.laisheng.data.model

import com.google.gson.annotations.SerializedName

// 发布瞬间的请求体
data class CreateMomentRequest(
    @SerializedName("user_id") val userId: String,
    val content: String
)

// 点赞/收藏请求体
data class ToggleRequest(
    @SerializedName("user_id") val userId: String,
    @SerializedName("moment_id") val momentId: String
)