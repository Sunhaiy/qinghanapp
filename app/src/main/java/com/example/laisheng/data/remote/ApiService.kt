package com.example.laisheng.data.remote

import com.example.laisheng.data.model.Quote
import retrofit2.http.GET

interface ApiService {
    // 这是一个免费的测试 API
    @GET("quotes/random")
    suspend fun getRandomQuote(): Quote
}