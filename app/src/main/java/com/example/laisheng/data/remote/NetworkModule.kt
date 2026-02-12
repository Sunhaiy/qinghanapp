package com.example.laisheng.data.remote

import com.example.laisheng.data.remote.ApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {
    // 局域网 IP
    const val BASE_URL = "http://192.168.1.2:3000/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)

    /**
     * 增强版 URL 格式化：
     * 1. 自动补全相对路径 (如 uploads/xxx.jpg -> http://192.168.1.2:3000/uploads/xxx.jpg)
     * 2. 自动替换 localhost 为局域网 IP
     * 3. 保持外部 HTTPS 链接不变
     */
    fun formatUrl(url: String?): String? {
        if (url == null || url.isBlank()) return null
        
        // 如果是完整的外部链接且不含 localhost，直接返回
        if (url.startsWith("http") && !url.contains("localhost") && !url.contains("127.0.0.1")) {
            return url
        }

        // 如果是 localhost 或相对路径，统一处理
        val cleanBase = BASE_URL.removeSuffix("/")
        val hostPart = cleanBase // 比如 http://192.168.1.2:3000

        var result = if (url.startsWith("http")) {
            url.replace("http://localhost:3000", hostPart)
               .replace("https://localhost:3000", hostPart)
               .replace("http://127.0.0.1:3000", hostPart)
        } else {
            // 处理相对路径
            val cleanPath = if (url.startsWith("/")) url else "/$url"
            hostPart + cleanPath
        }
        return result
    }
}