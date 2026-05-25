package com.example.laisheng.data.remote

import com.example.laisheng.data.model.AuthResponse
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
import com.example.laisheng.data.model.ListEnvelope
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
import com.example.laisheng.data.model.ToggleResult
import com.example.laisheng.data.model.UnreadCountResponse
import com.example.laisheng.data.model.UploadResponse
import com.example.laisheng.data.model.User
import com.google.gson.JsonElement
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("api/users/login")
    suspend fun login(@Body request: Map<String, String>): AuthResponse

    @POST("api/users/register")
    suspend fun register(@Body request: Map<String, String>): AuthResponse

    @GET("api/users/me")
    suspend fun getMe(): User

    @POST("api/users/heartbeat")
    suspend fun heartbeat(@Body request: Map<String, String> = emptyMap()): Map<String, Any>

    @GET("api/users/{id}")
    suspend fun getUser(@Path("id") id: String): User

    @PUT("api/users/{id}")
    suspend fun updateProfile(
        @Path("id") id: String,
        @Body profile: Map<String, String?>
    ): User

    @GET("api/users/search")
    suspend fun searchUsers(@Query("q") query: String): List<User>

    @GET("api/moments")
    suspend fun getMoments(
        @Query("page") page: Int? = 1,
        @Query("limit") limit: Int? = 10
    ): MomentResponse

    @GET("api/moments/{id}")
    suspend fun getMomentDetail(@Path("id") id: String): Moment

    @GET("api/moments/user/{userId}")
    suspend fun getUserMoments(
        @Path("userId") userId: String,
        @Query("page") page: Int? = 1,
        @Query("limit") limit: Int? = 10
    ): MomentResponse

    @GET("api/moments/search")
    suspend fun searchMoments(
        @Query("q") query: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): MomentResponse

    @GET("api/moments/featured")
    suspend fun getFeaturedMoments(): List<Moment>

    @GET("api/moments/following")
    suspend fun getFollowingMoments(
        @Query("page") page: Int? = 1,
        @Query("limit") limit: Int? = 10
    ): MomentResponse

    @POST("api/moments")
    suspend fun createMoment(@Body request: CreateMomentRequest): Moment

    @DELETE("api/moments/{id}")
    suspend fun deleteMoment(@Path("id") id: String): Map<String, Any>

    @POST("api/comments")
    suspend fun postComment(@Body request: CommentRequest): Comment

    @GET("api/comments/moment/{momentId}")
    suspend fun getMomentComments(@Path("momentId") momentId: String): List<Comment>

    @POST("api/likes/toggle")
    suspend fun toggleLike(@Body request: MomentActionRequest): ToggleResult

    @GET("api/likes/user/me")
    suspend fun getMyLikedMoments(): JsonElement

    @POST("api/follows/toggle")
    suspend fun toggleFollow(@Body request: FollowToggleRequest): ToggleResult

    @GET("api/follows/counts")
    suspend fun getFollowCounts(): FollowCounts

    @GET("api/follows/counts/{userId}")
    suspend fun getFollowCountsByUser(@Path("userId") userId: String): FollowCounts

    @GET("api/follows/followers")
    suspend fun getFollowers(): JsonElement

    @GET("api/follows/followers/{userId}")
    suspend fun getFollowersByUser(@Path("userId") userId: String): JsonElement

    @GET("api/follows/following")
    suspend fun getFollowing(): JsonElement

    @GET("api/follows/following/{userId}")
    suspend fun getFollowingByUser(@Path("userId") userId: String): JsonElement

    @GET("api/follows/mutual")
    suspend fun getMutualFollowing(): JsonElement

    @GET("api/follows/relation/{userId}")
    suspend fun getFollowRelation(@Path("userId") userId: String): FollowRelation

    @POST("api/collections/toggle")
    suspend fun toggleCollection(@Body request: MomentActionRequest): ToggleResult

    @GET("api/collections/user/me")
    suspend fun getMyCollections(@Query("folderId") folderId: String? = null): JsonElement

    @GET("api/collections/folders")
    suspend fun getFolders(): JsonElement

    @POST("api/collections/folders")
    suspend fun createFolder(@Body request: FolderCreateRequest): CollectionFolder

    @DELETE("api/collections/folders/{id}")
    suspend fun deleteFolder(@Path("id") id: String): Map<String, Any>

    @PUT("api/collections/{id}")
    suspend fun moveCollectionToFolder(
        @Path("id") id: String,
        @Body request: MoveCollectionRequest
    ): Map<String, Any>

    @POST("api/messages")
    suspend fun sendMessage(@Body request: SendMessageRequest): ChatMessage

    @GET("api/messages/chat-list")
    suspend fun getChatList(): ListEnvelope<ChatListItem>

    @GET("api/messages/history/{userId2}")
    suspend fun getChatHistory(
        @Path("userId2") userId2: String,
        @Query("page") page: Int? = 1,
        @Query("limit") limit: Int? = 20
    ): ListEnvelope<ChatMessage>

    @GET("api/messages/unread")
    suspend fun getUnreadMessages(): UnreadCountResponse

    @GET("api/notifications")
    suspend fun getNotifications(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): JsonElement

    @GET("api/notifications/unread")
    suspend fun getUnreadNotifications(): UnreadCountResponse

    @GET("api/membership/plans")
    suspend fun getMembershipPlans(): ListEnvelope<MembershipPlan>

    @GET("api/membership/me")
    suspend fun getMembershipStatus(): MembershipStatus

    @POST("api/membership/activate")
    suspend fun activateMembership(@Body request: MembershipActivateRequest): MembershipStatus

    @GET("api/membership/orders")
    suspend fun getMembershipOrders(): ListEnvelope<MembershipOrder>

    @POST("api/history/view")
    suspend fun recordHistoryView(@Body request: HistoryViewRequest): Map<String, Any>

    @GET("api/history")
    suspend fun getHistory(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): HistoryResponse

    @DELETE("api/history/{momentId}")
    suspend fun deleteHistoryItem(@Path("momentId") momentId: String): Map<String, Any>

    @DELETE("api/history")
    suspend fun clearHistory(): Map<String, Any>

    @Multipart
    @POST("api/upload/single")
    suspend fun uploadFile(@Part file: MultipartBody.Part): UploadResponse
}
