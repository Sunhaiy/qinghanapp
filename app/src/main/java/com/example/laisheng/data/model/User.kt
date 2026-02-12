package com.example.laisheng.data.model

import com.google.gson.annotations.SerializedName

data class User(
    val id: String,
    val handle: String,
    val nickname: String,
    val avatar: String?,
    val bio: String?,
    @SerializedName("bg_image") val bgImage: String?, // 处理下划线命名转驼峰
    @SerializedName("is_followed") val isFollowed: Boolean? = false
)