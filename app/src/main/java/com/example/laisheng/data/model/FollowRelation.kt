package com.example.laisheng.data.model

import com.google.gson.annotations.SerializedName

data class FollowRelation(
    @SerializedName("is_following") val isFollowing: Boolean = false,
    @SerializedName("is_followed_by") val isFollowedBy: Boolean = false,
    @SerializedName("is_mutual") val isMutual: Boolean = false
)
