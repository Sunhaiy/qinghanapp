package com.example.laisheng.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.laisheng.data.model.User

class UserPrefs(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    fun saveAuth(token: String, user: User) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_USER_ID, user.id)
            .putString(KEY_HANDLE, user.handle)
            .putString(KEY_NICKNAME, user.nickname)
            .putString(KEY_AVATAR, user.avatar)
            .putString(KEY_BIO, user.bio)
            .putString(KEY_BG_IMAGE, user.bgImage)
            .putString(KEY_IP_LOCATION, user.ipLocation)
            .putString(KEY_HANDLE_UPDATED_AT, user.handleLastUpdatedAt)
            .putString(KEY_MEMBERSHIP_STATUS, user.membershipStatus)
            .putString(KEY_MEMBERSHIP_LEVEL, user.membershipLevel)
            .putString(KEY_MEMBERSHIP_STARTED_AT, user.membershipStartedAt)
            .putString(KEY_MEMBERSHIP_EXPIRES_AT, user.membershipExpiresAt)
            .apply()
    }

    fun saveUserId(userId: String) {
        prefs.edit().putString(KEY_USER_ID, userId).apply()
    }

    fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun getCachedUser(): User? {
        val userId = getUserId() ?: return null
        val handle = prefs.getString(KEY_HANDLE, null) ?: return null
        val nickname = prefs.getString(KEY_NICKNAME, null) ?: return null
        return User(
            id = userId,
            handle = handle,
            nickname = nickname,
            avatar = prefs.getString(KEY_AVATAR, null),
            bio = prefs.getString(KEY_BIO, null),
            bgImage = prefs.getString(KEY_BG_IMAGE, null),
            ipLocation = prefs.getString(KEY_IP_LOCATION, null),
            handleLastUpdatedAt = prefs.getString(KEY_HANDLE_UPDATED_AT, null),
            membershipStatus = prefs.getString(KEY_MEMBERSHIP_STATUS, null),
            membershipLevel = prefs.getString(KEY_MEMBERSHIP_LEVEL, null),
            membershipStartedAt = prefs.getString(KEY_MEMBERSHIP_STARTED_AT, null),
            membershipExpiresAt = prefs.getString(KEY_MEMBERSHIP_EXPIRES_AT, null)
        )
    }

    fun saveThemeMode(mode: Int) {
        prefs.edit().putInt(KEY_THEME_MODE, mode).apply()
    }

    fun getThemeMode(): Int = prefs.getInt(KEY_THEME_MODE, 0)

    fun saveLastCollectionFolder(folderId: String?) {
        prefs.edit().putString(KEY_LAST_COLLECTION_FOLDER, folderId).apply()
    }

    fun getLastCollectionFolder(): String? =
        prefs.getString(KEY_LAST_COLLECTION_FOLDER, null)

    fun saveDefaultCollectionFolderName(name: String) {
        prefs.edit().putString(KEY_DEFAULT_COLLECTION_FOLDER_NAME, name).apply()
    }

    fun getDefaultCollectionFolderName(): String =
        prefs.getString(KEY_DEFAULT_COLLECTION_FOLDER_NAME, "默认收藏夹") ?: "默认收藏夹"

    fun getSearchHistory(): Set<String> =
        prefs.getStringSet(KEY_SEARCH_HISTORY, emptySet()) ?: emptySet()

    fun saveSearchHistory(history: Set<String>) {
        prefs.edit().putStringSet(KEY_SEARCH_HISTORY, history).apply()
    }

    fun clearSearchHistory() {
        prefs.edit().remove(KEY_SEARCH_HISTORY).apply()
    }

    fun clearAuth() {
        prefs.edit()
            .remove(KEY_TOKEN)
            .remove(KEY_USER_ID)
            .remove(KEY_HANDLE)
            .remove(KEY_NICKNAME)
            .remove(KEY_AVATAR)
            .remove(KEY_BIO)
            .remove(KEY_BG_IMAGE)
            .remove(KEY_IP_LOCATION)
            .remove(KEY_HANDLE_UPDATED_AT)
            .remove(KEY_MEMBERSHIP_STATUS)
            .remove(KEY_MEMBERSHIP_LEVEL)
            .remove(KEY_MEMBERSHIP_STARTED_AT)
            .remove(KEY_MEMBERSHIP_EXPIRES_AT)
            .remove(KEY_LAST_COLLECTION_FOLDER)
            .apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val KEY_TOKEN = "token"
        const val KEY_USER_ID = "user_id"
        const val KEY_HANDLE = "handle"
        const val KEY_NICKNAME = "nickname"
        const val KEY_AVATAR = "avatar"
        const val KEY_BIO = "bio"
        const val KEY_BG_IMAGE = "bg_image"
        const val KEY_IP_LOCATION = "ip_location"
        const val KEY_HANDLE_UPDATED_AT = "handle_last_updated_at"
        const val KEY_MEMBERSHIP_STATUS = "membership_status"
        const val KEY_MEMBERSHIP_LEVEL = "membership_level"
        const val KEY_MEMBERSHIP_STARTED_AT = "membership_started_at"
        const val KEY_MEMBERSHIP_EXPIRES_AT = "membership_expires_at"
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_LAST_COLLECTION_FOLDER = "last_collection_folder"
        const val KEY_DEFAULT_COLLECTION_FOLDER_NAME = "default_collection_folder_name"
        const val KEY_SEARCH_HISTORY = "search_history"
    }
}
