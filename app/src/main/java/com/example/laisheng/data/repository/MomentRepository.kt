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

    // 2. 获取用户资料
    suspend fun getUserProfile(userId: String): User? {
        return try {
            apiService.getUser(userId)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 3. 获取瞬间列表 (支持分页)
    suspend fun getMoments(page: Int = 1, limit: Int = 10, currentUserId: String? = null): MomentResponse? {
        return try {
            apiService.getMoments(page, limit, currentUserId)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 4. 获取瞬间详情
    suspend fun getMomentDetail(id: String, currentUserId: String? = null): Moment? {
        return try {
            apiService.getMomentDetail(id, currentUserId)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 5. 获取特定用户的瞬间列表
    suspend fun getUserMoments(userId: String, page: Int = 1, limit: Int = 10, currentUserId: String? = null): MomentResponse? {
        return try {
            apiService.getUserMoments(userId, page, limit, currentUserId)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 6. 获取用户的收藏列表
    suspend fun getUserCollections(userId: String): List<Moment> {
        return try {
            apiService.getUserCollections(userId)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // 7. 点赞切换逻辑
    suspend fun toggleLike(userId: String, momentId: String): Boolean {
        return try {
            val response = apiService.toggleLike(ToggleRequest(userId, momentId))
            response["liked"] ?: false
        } catch (e: Exception) {
            false
        }
    }

    // 8. 收藏切换逻辑
    suspend fun toggleCollection(userId: String, momentId: String): Boolean {
        return try {
            val response = apiService.toggleCollection(ToggleRequest(userId, momentId))
            response["collected"] ?: false
        } catch (e: Exception) {
            false
        }
    }

    // 9. 获取瞬间评论列表
    suspend fun getMomentComments(momentId: String): List<Comment> {
        return try {
            apiService.getMomentComments(momentId)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // 10. 发表评论
    suspend fun postComment(userId: String, momentId: String, content: String): Comment? {
        return try {
            apiService.postComment(CommentRequest(userId, momentId, content))
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}