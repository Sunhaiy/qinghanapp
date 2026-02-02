package com.example.laisheng.data.remote

import com.example.laisheng.data.model.*
import okhttp3.MultipartBody
import retrofit2.http.*

interface ApiService {

    // --- 用户相关 ---
    @POST("api/users/login")
    suspend fun login(@Body request: Map<String, String>): User

    @POST("api/users/register")
    suspend fun register(@Body request: Map<String, String>): User

    @GET("api/users/{id}")
    suspend fun getUser(@Path("id") id: String): User

    // --- 瞬间 (Moments) 相关 ---
    @GET("api/moments")
    suspend fun getMoments(
        @Query("page") page: Int? = 1,
        @Query("limit") limit: Int? = 10,
        @Query("current_user_id") currentUserId: String? = null
    ): MomentResponse

    @GET("api/moments/{id}")
    suspend fun getMomentDetail(
        @Path("id") id: String,
        @Query("current_user_id") currentUserId: String? = null
    ): Moment

    @GET("api/moments/user/{userId}")
    suspend fun getUserMoments(
        @Path("userId") userId: String,
        @Query("page") page: Int? = 1,
        @Query("limit") limit: Int? = 10,
        @Query("current_user_id") currentUserId: String? = null
    ): MomentResponse

    @POST("api/moments")
    suspend fun createMoment(@Body request: CreateMomentRequest): Moment

    // --- 互动相关 (点赞/收藏) ---
    @POST("api/likes/toggle")
    suspend fun toggleLike(@Body request: ToggleRequest): Map<String, Boolean>

    @POST("api/collections/toggle")
    suspend fun toggleCollection(@Body request: ToggleRequest): Map<String, Boolean>

    @GET("api/collections/user/{userId}")
    suspend fun getUserCollections(@Path("userId") userId: String): List<Moment>

    // --- 关注相关 ---
    @POST("api/follows/toggle")
    suspend fun toggleFollow(@Body request: Map<String, String>): Map<String, Boolean>

    @GET("api/follows/counts/{userId}")
    suspend fun getFollowCounts(@Path("userId") userId: String): FollowCounts

    // --- 评论相关 ---
    @POST("api/comments")
    suspend fun postComment(@Body request: CommentRequest): Comment

    @GET("api/comments/moment/{momentId}")
    suspend fun getMomentComments(@Path("momentId") momentId: String): List<Comment>

    // --- 私信相关 ---
    @POST("api/messages")
    suspend fun sendMessage(@Body request: SendMessageRequest): ChatMessage

    @GET("api/messages/chat-list/{userId}")
    suspend fun getChatList(@Path("userId") userId: String): List<ChatListItem>

    @GET("api/messages/history/{userId1}/{userId2}")
    suspend fun getChatHistory(
        @Path("userId1") userId1: String,
        @Path("userId2") userId2: String,
        @Query("page") page: Int? = 1,
        @Query("limit") limit: Int? = 50
    ): List<ChatMessage>

    // --- 上传相关 ---
    @Multipart
    @POST("api/upload/single")
    suspend fun uploadFile(
        @Part file: MultipartBody.Part
    ): UploadResponse
}