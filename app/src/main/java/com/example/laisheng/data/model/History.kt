package com.example.laisheng.data.model

import com.google.gson.annotations.SerializedName

data class HistoryResponse(
    val page: Int,
    val limit: Int,
    val data: List<HistoryItem>
)

data class HistoryItem(
    val id: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("moment_id") val momentId: String,
    val source: String,
    @SerializedName("first_viewed_at") val firstViewedAt: String,
    @SerializedName("last_viewed_at") val lastViewedAt: String,
    @SerializedName("view_count") val viewCount: Int,
    val content: MomentContent,
    @SerializedName("likes_count") val likesCount: Int = 0,
    @SerializedName("comments_count") val commentsCount: Int = 0,
    @SerializedName("moment_created_at") val momentCreatedAt: String,
    @SerializedName("ip_location") val ipLocation: String? = null,
    @SerializedName("author_id") val authorId: String,
    val nickname: String? = null,
    val avatar: String? = null,
    val handle: String? = null
)

fun HistoryItem.toMoment(): Moment =
    Moment(
        id = momentId,
        userId = authorId,
        content = content,
        likesCount = likesCount,
        commentsCount = commentsCount,
        createdAt = momentCreatedAt,
        nickname = nickname,
        handle = handle,
        avatar = avatar,
        ipLocation = ipLocation,
        userIpLocation = ipLocation
    )
