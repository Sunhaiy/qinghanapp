package com.example.laisheng.data.repository

import com.example.laisheng.data.model.*
import com.example.laisheng.data.remote.ApiService
import okhttp3.MultipartBody

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
    suspend fun getUserProfile(userId: String, currentUserId: String? = null): User? {
        return try {
            apiService.getUser(userId, currentUserId)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 3. 更新用户资料
    suspend fun updateProfile(userId: String, profile: Map<String, String?>): User? {
        return try {
            apiService.updateProfile(userId, profile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 4. 获取瞬间列表 (支持分页)
    suspend fun getMoments(page: Int = 1, limit: Int = 10, currentUserId: String? = null): MomentResponse? {
        return try {
            apiService.getMoments(page, limit, currentUserId)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 5. 获取瞬间详情
    suspend fun getMomentDetail(id: String, currentUserId: String? = null): Moment? {
        return try {
            apiService.getMomentDetail(id, currentUserId)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 6. 获取特定用户的瞬间列表
    suspend fun getUserMoments(userId: String, page: Int = 1, limit: Int = 10, currentUserId: String? = null): MomentResponse? {
        return try {
            apiService.getUserMoments(userId, page, limit, currentUserId)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun searchMoments(query: String, page: Int, limit: Int = 20, currentUserId: String? = null): MomentResponse? {
        return try {
            apiService.searchMoments(query, page, limit, currentUserId)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun searchUsers(query: String): List<User> {
        return try {
            apiService.searchUsers(query)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // 7. 获取用户的收藏列表
    suspend fun getUserCollections(userId: String, currentUserId: String? = null, folderId: String? = null): List<Moment> {
        return try {
            apiService.getUserCollections(userId, currentUserId, folderId)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // --- 收藏夹管理 ---
    suspend fun createFolder(userId: String, name: String): CollectionFolder? {
        return try {
            apiService.createFolder(mapOf("userId" to userId, "name" to name))
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getFolders(userId: String): List<CollectionFolder> {
        return try {
            apiService.getFolders(userId)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun deleteFolder(id: String, userId: String): Boolean {
        return try {
            apiService.deleteFolder(id, mapOf("userId" to userId))
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun moveCollectionToFolder(momentId: String, userId: String, folderId: String?): Boolean {
        return try {
            apiService.moveCollectionToFolder(momentId, mapOf("userId" to userId, "folderId" to folderId))
            true
        } catch (e: Exception) {
            false
        }
    }

    // 8. 获取用户点赞列表
    suspend fun getUserLikedMoments(userId: String, currentUserId: String? = null): List<Moment> {
        return try {
            apiService.getUserLikedMoments(userId, currentUserId)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // 9. 点赞切换逻辑
    suspend fun toggleLike(userId: String, momentId: String): Boolean {
        return try {
            val response = apiService.toggleLike(ToggleRequest(userId, momentId))
            response["liked"] ?: false
        } catch (e: Exception) {
            false
        }
    }

    // 10. 收藏切换逻辑
    suspend fun toggleCollection(userId: String, momentId: String): Boolean {
        return try {
            val response = apiService.toggleCollection(ToggleRequest(userId, momentId))
            response["collected"] ?: false
        } catch (e: Exception) {
            false
        }
    }

    // 11. 获取瞬间评论列表
    suspend fun getMomentComments(momentId: String): List<Comment> {
        return try {
            apiService.getMomentComments(momentId)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // 12. 发表评论
    suspend fun postComment(userId: String, momentId: String, content: String): Comment? {
        return try {
            apiService.postComment(CommentRequest(userId, momentId, content))
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 13. 获取关注/粉丝数
    suspend fun getFollowCounts(userId: String): FollowCounts? {
        return try {
            apiService.getFollowCounts(userId)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 14. 获取互关好友列表
    suspend fun getMutualFollowing(userId: String): List<User> {
        return try {
            apiService.getMutualFollowing(userId)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // 15. 获取聊天列表
    suspend fun getChatList(userId: String): List<ChatListItem> {
        return try {
            apiService.getChatList(userId)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // 16. 获取聊天历史记录
    suspend fun getChatHistory(userId1: String, userId2: String, page: Int = 1): List<ChatMessage> {
        return try {
            apiService.getChatHistory(userId1, userId2, page)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // 17. 发送私信
    suspend fun sendMessage(senderId: String, receiverId: String, text: String): ChatMessage? {
        return try {
            val request = SendMessageRequest(
                senderId = senderId,
                receiverId = receiverId,
                content = MessageContent(text = text, type = "text")
            )
            apiService.sendMessage(request)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 18. 上传文件
    suspend fun uploadFile(file: MultipartBody.Part): UploadResponse? {
        return try {
            apiService.uploadFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
