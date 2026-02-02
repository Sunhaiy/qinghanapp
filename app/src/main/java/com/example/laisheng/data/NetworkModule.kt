package com.example.laisheng.data

import com.example.laisheng.data.remote.ApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {
    // 你的电脑局域网 IP
    const val BASE_URL = "http://192.168.1.2:3000/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)

    /**
     * 自动处理 URL 转换：
     * 将后端返回的 localhost 或 127.0.0.1 替换为当前配置的局域网 IP。
     * 这样无论是在模拟器 (10.0.2.2) 还是真机 (192.168.x.x) 都能正常显示图片和播放音频。
     */
    fun formatUrl(url: String?): String? {
        if (url == null) return null
        // 提取 BASE_URL 中的 IP 部分 (例如 http://192.168.1.2)
        val hostPart = BASE_URL.substringBeforeLast(":3000/")
        return url.replace("http://localhost", hostPart)
                  .replace("http://127.0.0.1", hostPart)
    }
}