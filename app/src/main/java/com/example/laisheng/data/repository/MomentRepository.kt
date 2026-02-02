package com.example.laisheng.data.repository

import com.example.laisheng.data.model.*
import com.example.laisheng.data.remote.ApiService

class MomentRepository(private val apiService: ApiService) {

    // 1.登录逻辑
    suspend fun login(handle: String, password: String): User? {
        return try {
            apiService.login(mapOf("handle" to handle, "password" to password))
        } catch (e: Exception) {
            null
        }
    }

    // 2. 获取瞬间列表 (支持分页)
    suspend fun getMoments(page: Int = 1, limit: Int = 10, currentUserId: String? = null): MomentResponse? {
        return try {
            apiService.getMoments(page, limit, currentUserId)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 3. 点赞切换逻辑
    suspend fun toggleLike(userId: String, momentId: String): Boolean {
        return try {
            val response = apiService.toggleLike(ToggleRequest(userId, momentId))
            response["liked"] ?: false
        } catch (e: Exception) {
            false
        }
    }

    // 4. 收藏切换逻辑
    suspend fun toggleCollection(userId: String, momentId: String): Boolean {
        return try {
            val response = apiService.toggleCollection(ToggleRequest(userId, momentId))
            response["collected"] ?: false
        } catch (e: Exception) {
            false
        }
    }
}