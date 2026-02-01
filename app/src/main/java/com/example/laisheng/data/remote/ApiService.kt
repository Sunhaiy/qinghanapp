package com.example.laisheng.data.remote

import com.example.laisheng.data.model.*
import retrofit2.http.*

interface ApiService {

    // --- 用户相关 ---
    @GET("api/users/{id}")
    suspend fun getUser(@Path("id") id: String): User

    // --- 瞬间 (Moments) 相关 ---

    // 获取瞬间列表 (流)
    @GET("api/moments")
    suspend fun getMoments(): MomentResponse // 修改这里：返回包装后的对象

    // 发布瞬间
    @POST("api/moments")
    suspend fun createMoment(@Body request: CreateMomentRequest): Moment

    // --- 互动相关 ---

    // 点赞 (切换状态)
    @POST("api/likes/toggle")
    suspend fun toggleLike(@Body request: ToggleRequest): Map<String, Boolean>
}