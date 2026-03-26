package com.example.laisheng.data.remote

import com.example.laisheng.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {
    val BASE_URL: String = BuildConfig.BASE_URL

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val token = AuthSession.token
            val requestBuilder = chain.request().newBuilder()
            if (!token.isNullOrBlank()) {
                requestBuilder.header("Authorization", "Bearer $token")
            }
            chain.proceed(requestBuilder.build())
        }
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)

    fun formatUrl(url: String?): String? {
        if (url.isNullOrBlank()) return null
        if (url.startsWith("http") && !url.contains("localhost") && !url.contains("127.0.0.1")) {
            return url
        }

        val hostPart = BASE_URL.removeSuffix("/")
        return if (url.startsWith("http")) {
            url.replace("http://localhost:3000", hostPart)
                .replace("https://localhost:3000", hostPart)
                .replace("http://127.0.0.1:3000", hostPart)
        } else {
            val cleanPath = if (url.startsWith("/")) url else "/$url"
            hostPart + cleanPath
        }
    }
}
