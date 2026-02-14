package com.example.laisheng.data.model

import com.google.gson.annotations.SerializedName

data class CollectionFolder(
    val id: String,
    @SerializedName("user_id") val userId: String,
    val name: String,
    @SerializedName("created_at") val createdAt: String
)
