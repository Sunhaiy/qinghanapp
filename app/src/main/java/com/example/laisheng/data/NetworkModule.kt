package com.example.laisheng.data

import com.example.laisheng.data.remote.ApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {
    // 模拟器必须使用 10.0.2.2，不能用 localhost
    private const val BASE_URL = "http://10.0.2.2:3000/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}