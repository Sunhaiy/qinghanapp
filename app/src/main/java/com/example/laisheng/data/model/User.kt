package com.example.laisheng.data.model

import com.google.gson.annotations.SerializedName

data class User(
    val id: String,
    val handle: String,
    val nickname: String,
    val avatar: String?,
    val bio: String?,
    @SerializedName("bg_image") val bgImage: String?,
    @SerializedName("is_followed") val isFollowed: Boolean? = false,
    @SerializedName("ip_location") val ipLocation: String? = null,
    @SerializedName("handle_last_updated_at") val handleLastUpdatedAt: String? = null
)