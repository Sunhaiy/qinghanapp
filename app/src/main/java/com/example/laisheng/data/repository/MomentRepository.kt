package com.example.laisheng.data.repository

import android.util.Log
import com.example.laisheng.data.model.ChatListItem
import com.example.laisheng.data.model.ChatMessage
import com.example.laisheng.data.model.CollectionFolder
import com.example.laisheng.data.model.Comment
import com.example.laisheng.data.model.CommentRequest
import com.example.laisheng.data.model.CreateMomentRequest
import com.example.laisheng.data.model.FolderCreateRequest
import com.example.laisheng.data.model.FollowCounts
import com.example.laisheng.data.model.FollowRelation
import com.example.laisheng.data.model.FollowToggleRequest
import com.example.laisheng.data.model.HistoryResponse
import com.example.laisheng.data.model.HistoryViewRequest
import com.example.laisheng.data.model.MembershipActivateRequest
import com.example.laisheng.data.model.MembershipOrder
import com.example.laisheng.data.model.MembershipPlan
import com.example.laisheng.data.model.MembershipStatus
import com.example.laisheng.data.model.Moment
import com.example.laisheng.data.model.MomentActionRequest
import com.example.laisheng.data.model.MomentResponse
import com.example.laisheng.data.model.MoveCollectionRequest
import com.example.laisheng.data.model.NotificationItem
import com.example.laisheng.data.model.SendMessageRequest
import com.example.laisheng.data.model.MessageContent
import com.example.laisheng.data.model.UploadResponse
import com.example.laisheng.data.model.User
import com.example.laisheng.data.model.toUserOrNull
import com.example.laisheng.data.remote.ApiService
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import okhttp3.MultipartBody
import retrofit2.HttpException

class ApiMessageException(message: String) : Exception(message)

class MomentRepository(private val apiService: ApiService) {
    private val tag = "MomentRepository"
    private val gson = Gson()

    suspend fun login(handle: String, password: String): User? =
        try {
            val response = apiService.login(
                mapOf("handle" to handle.trim().removePrefix("@"), "password" to password)
            )
            response.user ?: response.toUserOrNull()
        } catch (_: Exception) {
            null
        }

    suspend fun getCurrentUser(): User? =
        try {
            apiService.getMe()
        } catch (_: Exception) {
            null
        }

    suspend fun getUserProfile(userId: String, currentUserId: String? = null): User? =
        try {
            if (currentUserId != null && currentUserId == userId) {
                apiService.getMe()
            } else {
                apiService.getUser(userId)
            }
        } catch (_: Exception) {
            null
        }

    suspend fun updateProfile(userId: String, profile: Map<String, String?>): User? =
        try {
            apiService.updateProfile(userId, profile)
        } catch (_: Exception) {
            null
        }

    suspend fun getMoments(page: Int = 1, limit: Int = 10, currentUserId: String? = null): MomentResponse? =
        try {
            apiService.getMoments(page, limit)
        } catch (_: Exception) {
            null
        }

    suspend fun getMomentDetail(id: String, currentUserId: String? = null): Moment? =
        try {
            apiService.getMomentDetail(id)
        } catch (_: Exception) {
            null
        }

    suspend fun getUserMoments(
        userId: String,
        page: Int = 1,
        limit: Int = 10,
        currentUserId: String? = null
    ): MomentResponse? =
        try {
            apiService.getUserMoments(userId, page, limit)
        } catch (_: Exception) {
            null
        }

    suspend fun searchMoments(
        query: String,
        page: Int,
        limit: Int = 20,
        currentUserId: String? = null
    ): MomentResponse? =
        try {
            apiService.searchMoments(query, page, limit)
        } catch (_: Exception) {
            null
        }

    suspend fun getFeaturedMoments(currentUserId: String? = null): List<Moment>? =
        try {
            apiService.getFeaturedMoments()
        } catch (_: Exception) {
            null
        }

    suspend fun getFollowingMoments(
        page: Int = 1,
        limit: Int = 10,
        currentUserId: String? = null
    ): MomentResponse? =
        try {
            apiService.getFollowingMoments(page, limit)
        } catch (_: Exception) {
            null
        }

    suspend fun searchUsers(query: String): List<User> =
        try {
            apiService.searchUsers(query)
        } catch (_: Exception) {
            emptyList()
        }

    suspend fun getUserCollections(
        userId: String,
        currentUserId: String? = null,
        folderId: String? = null
    ): List<Moment> =
        try {
            if (currentUserId != null && userId == currentUserId) {
                parseListPayload(apiService.getMyCollections(folderId))
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(tag, "getUserCollections failed userId=$userId currentUserId=$currentUserId folderId=$folderId", e)
            emptyList()
        }

    suspend fun createFolder(name: String): CollectionFolder {
        try {
            return apiService.createFolder(FolderCreateRequest(name))
        } catch (e: HttpException) {
            throw ApiMessageException(extractErrorMessage(e))
        }
    }

    suspend fun getFolders(userId: String? = null): List<CollectionFolder> =
        try {
            parseListPayload(apiService.getFolders())
        } catch (e: Exception) {
            Log.e(tag, "getFolders failed", e)
            emptyList()
        }

    suspend fun deleteFolder(id: String, userId: String? = null): Boolean =
        try {
            apiService.deleteFolder(id)
            true
        } catch (_: Exception) {
            false
        }

    suspend fun moveCollectionToFolder(momentId: String, userId: String? = null, folderId: String?): Boolean =
        try {
            apiService.moveCollectionToFolder(momentId, MoveCollectionRequest(folderId))
            true
        } catch (_: Exception) {
            false
        }

    suspend fun getUserLikedMoments(userId: String? = null, currentUserId: String? = null): List<Moment> =
        try {
            parseListPayload(apiService.getMyLikedMoments())
        } catch (e: Exception) {
            Log.e(tag, "getUserLikedMoments failed userId=$userId currentUserId=$currentUserId", e)
            emptyList()
        }

    suspend fun toggleLike(userId: String? = null, momentId: String): Boolean =
        try {
            apiService.toggleLike(MomentActionRequest(momentId)).liked ?: false
        } catch (_: Exception) {
            false
        }

    suspend fun toggleCollection(userId: String? = null, momentId: String): Boolean =
        try {
            apiService.toggleCollection(MomentActionRequest(momentId)).collected ?: false
        } catch (_: Exception) {
            false
        }

    suspend fun getMomentComments(momentId: String): List<Comment> =
        try {
            apiService.getMomentComments(momentId)
        } catch (_: Exception) {
            emptyList()
        }

    suspend fun postComment(userId: String? = null, momentId: String, content: String): Comment? =
        try {
            apiService.postComment(CommentRequest(momentId, content))
        } catch (_: Exception) {
            null
        }

    suspend fun getFollowCounts(userId: String? = null): FollowCounts? =
        try {
            if (userId.isNullOrBlank()) apiService.getFollowCounts() else apiService.getFollowCountsByUser(userId)
        } catch (_: Exception) {
            null
        }

    suspend fun getFollowers(userId: String? = null): List<User> =
        try {
            if (userId.isNullOrBlank()) {
                parseListPayload(apiService.getFollowers())
            } else {
                parseListPayload(apiService.getFollowersByUser(userId))
            }
        } catch (e: Exception) {
            Log.e(tag, "getFollowers failed userId=$userId", e)
            emptyList()
        }

    suspend fun getFollowing(userId: String? = null): List<User> =
        try {
            if (userId.isNullOrBlank()) {
                parseListPayload(apiService.getFollowing())
            } else {
                parseListPayload(apiService.getFollowingByUser(userId))
            }
        } catch (e: Exception) {
            Log.e(tag, "getFollowing failed userId=$userId", e)
            emptyList()
        }

    suspend fun getMutualFollowing(userId: String? = null): List<User> =
        try {
            parseListPayload(apiService.getMutualFollowing())
        } catch (e: Exception) {
            Log.e(tag, "getMutualFollowing failed userId=$userId", e)
            emptyList()
        }

    suspend fun toggleFollow(targetUserId: String): Boolean =
        try {
            apiService.toggleFollow(FollowToggleRequest(targetUserId)).followed ?: false
        } catch (_: Exception) {
            false
        }

    suspend fun getFollowRelation(targetUserId: String): FollowRelation? =
        try {
            apiService.getFollowRelation(targetUserId)
        } catch (_: Exception) {
            null
        }

    suspend fun getChatList(userId: String? = null): List<ChatListItem> =
        try {
            apiService.getChatList().value
        } catch (_: Exception) {
            emptyList()
        }

    suspend fun getChatHistory(userId1: String? = null, userId2: String, page: Int = 1): List<ChatMessage> =
        try {
            apiService.getChatHistory(userId2, page = page).value
        } catch (_: Exception) {
            emptyList()
        }

    suspend fun sendMessage(senderId: String? = null, receiverId: String, text: String): ChatMessage? =
        try {
            apiService.sendMessage(
                SendMessageRequest(
                    receiverId = receiverId,
                    content = MessageContent(text = text, type = "text")
                )
            )
        } catch (_: Exception) {
            null
        }

    suspend fun getUnreadMessages(): Int =
        try {
            apiService.getUnreadMessages().unreadCount
        } catch (_: Exception) {
            0
        }

    suspend fun getNotifications(page: Int = 1, limit: Int = 20): List<NotificationItem> =
        try {
            parseListPayload(apiService.getNotifications(page, limit))
        } catch (e: Exception) {
            Log.e(tag, "getNotifications failed page=$page limit=$limit", e)
            emptyList()
        }

    suspend fun getUnreadNotifications(): Int =
        try {
            apiService.getUnreadNotifications().unreadCount
        } catch (_: Exception) {
            0
        }

    suspend fun getMembershipPlans(): List<MembershipPlan> =
        try {
            apiService.getMembershipPlans().value
        } catch (_: Exception) {
            emptyList()
        }

    suspend fun getMembershipStatus(): MembershipStatus? =
        try {
            apiService.getMembershipStatus()
        } catch (_: Exception) {
            null
        }

    suspend fun activateMembership(planCode: String): MembershipStatus? =
        try {
            apiService.activateMembership(MembershipActivateRequest(planCode))
        } catch (_: Exception) {
            null
        }

    suspend fun getMembershipOrders(): List<MembershipOrder> =
        try {
            apiService.getMembershipOrders().value
        } catch (_: Exception) {
            emptyList()
        }

    suspend fun recordHistoryView(momentId: String, source: String = "detail"): Boolean =
        try {
            apiService.recordHistoryView(HistoryViewRequest(momentId, source))
            true
        } catch (_: Exception) {
            false
        }

    suspend fun getHistory(page: Int = 1, limit: Int = 20): HistoryResponse? =
        try {
            apiService.getHistory(page, limit)
        } catch (_: Exception) {
            null
        }

    suspend fun deleteHistory(momentId: String): Boolean =
        try {
            apiService.deleteHistoryItem(momentId)
            true
        } catch (_: Exception) {
            false
        }

    suspend fun clearHistory(): Boolean =
        try {
            apiService.clearHistory()
            true
        } catch (_: Exception) {
            false
        }

    suspend fun uploadFile(file: MultipartBody.Part): UploadResponse? =
        try {
            apiService.uploadFile(file)
        } catch (_: Exception) {
            null
        }

    private fun extractErrorMessage(exception: HttpException): String {
        val body = exception.response()?.errorBody()?.string().orEmpty()
        return when {
            body.contains("folder", ignoreCase = true) && body.contains("limit", ignoreCase = true) ->
                "达到会员限制"
            body.contains("会员限制") -> "达到会员限制"
            body.contains("达到会员限制") -> "达到会员限制"
            body.isNotBlank() -> body
            else -> "请求失败"
        }
    }

    private inline fun <reified T> parseListPayload(payload: JsonElement): List<T> {
        val listType = object : TypeToken<List<T>>() {}.type
        return when {
            payload.isJsonArray -> gson.fromJson(payload, listType) ?: emptyList()
            payload.isJsonObject -> {
                val obj = payload.asJsonObject
                val container = when {
                    obj.has("value") -> obj.get("value")
                    obj.has("data") -> obj.get("data")
                    else -> return emptyList()
                }
                gson.fromJson(container, listType) ?: emptyList()
            }
            else -> emptyList()
        }
    }
}
