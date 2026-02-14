package com.example.laisheng.data.local

import android.content.Context
import android.content.SharedPreferences

class UserPrefs(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    fun saveUserId(userId: String) {
        prefs.edit().putString("user_id", userId).apply()
    }

    fun getUserId(): String? {
        return prefs.getString("user_id", null)
    }


    // Theme: 0 = System, 1 = Light, 2 = Dark
    fun saveThemeMode(mode: Int) {
        prefs.edit().putInt("theme_mode", mode).apply()
    }

    fun getThemeMode(): Int {
        return prefs.getInt("theme_mode", 0) // Default to System
    }

    fun saveLastCollectionFolder(folderId: String?) {
        prefs.edit().putString("last_collection_folder", folderId).apply()
    }

    fun getLastCollectionFolder(): String? {
        return prefs.getString("last_collection_folder", null)
    }

    fun clear() {
        prefs.edit().remove("user_id").remove("theme_mode").remove("last_collection_folder").apply()
    }
}