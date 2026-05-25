package com.example.laisheng.data.model

import com.google.gson.annotations.SerializedName

data class CreateMomentRequest(
    val content: MomentContent
)

data class MomentActionRequest(
    @SerializedName("moment_id") val momentId: String
)

data class CommentRequest(
    @SerializedName("moment_id") val momentId: String,
    val content: String
)

data class FollowToggleRequest(
    @SerializedName("followingId") val followingId: String
)

data class FolderCreateRequest(
    val name: String
)

data class MoveCollectionRequest(
    val folderId: String?
)

data class HistoryViewRequest(
    @SerializedName("moment_id") val momentId: String,
    val source: String
)

data class Comment(
    val id: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("moment_id") val momentId: String,
    val content: String,
    @SerializedName("created_at") val createdAt: String,
    val nickname: String?,
    val avatar: String?
)
