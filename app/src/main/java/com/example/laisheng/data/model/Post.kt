package com.example.laisheng.data.model

data class Post(
    val id: Int,
    val username: String,
    val handle: String,
    val content: String,
    val timestamp: String,
    val likeCount: Int,
    val commentCount: Int,
    val avatarUrl: String? = null
)
