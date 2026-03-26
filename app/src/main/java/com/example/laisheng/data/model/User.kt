package com.example.laisheng.data.model

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    val token: String? = null,
    val user: User? = null,
    val id: String? = null,
    val handle: String? = null,
    val nickname: String? = null,
    val avatar: String? = null,
    val bio: String? = null,
    @SerializedName("bg_image") val bgImage: String? = null,
    @SerializedName("is_followed") val isFollowed: Boolean? = false,
    @SerializedName("ip_location") val ipLocation: String? = null,
    @SerializedName("handle_last_updated_at") val handleLastUpdatedAt: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("membership_status") val membershipStatus: String? = null,
    @SerializedName("membership_level") val membershipLevel: String? = null,
    @SerializedName("membership_started_at") val membershipStartedAt: String? = null,
    @SerializedName("membership_expires_at") val membershipExpiresAt: String? = null
)

data class User(
    val id: String,
    val handle: String,
    val nickname: String,
    val avatar: String? = null,
    val bio: String? = null,
    @SerializedName("bg_image") val bgImage: String? = null,
    @SerializedName("is_followed") val isFollowed: Boolean? = false,
    @SerializedName("ip_location") val ipLocation: String? = null,
    @SerializedName("handle_last_updated_at") val handleLastUpdatedAt: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("membership_status") val membershipStatus: String? = null,
    @SerializedName("membership_level") val membershipLevel: String? = null,
    @SerializedName("membership_started_at") val membershipStartedAt: String? = null,
    @SerializedName("membership_expires_at") val membershipExpiresAt: String? = null
)

fun AuthResponse.toUserOrNull(): User? {
    val userId = id ?: return null
    val userHandle = handle ?: return null
    val userNickname = nickname ?: return null
    return User(
        id = userId,
        handle = userHandle,
        nickname = userNickname,
        avatar = avatar,
        bio = bio,
        bgImage = bgImage,
        isFollowed = isFollowed,
        ipLocation = ipLocation,
        handleLastUpdatedAt = handleLastUpdatedAt,
        createdAt = createdAt,
        membershipStatus = membershipStatus,
        membershipLevel = membershipLevel,
        membershipStartedAt = membershipStartedAt,
        membershipExpiresAt = membershipExpiresAt
    )
}
