package com.example.laisheng.data.repository

import com.example.laisheng.data.model.*
import com.example.laisheng.data.remote.ApiService

class MomentRepository(private val apiService: ApiService) {

    // 1.登录逻辑
    suspend fun login(handle: String): User? {
        return try {
            apiService.login(mapOf("handle" to handle))
        } catch (e: Exception) {
            null
        }
    }

    // 2. 获取瞬间列表 (支持传入当前用户 ID 以获取点赞/收藏状态)
    suspend fun getMoments(currentUserId: String? = null): List<Moment> {
        return try {
            val response = apiService.getMoments(currentUserId)
            response.data
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
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