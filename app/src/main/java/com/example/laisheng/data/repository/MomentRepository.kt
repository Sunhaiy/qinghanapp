package com.example.laisheng.data.repository

import com.example.laisheng.data.model.Moment
import com.example.laisheng.data.remote.ApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MomentRepository(private val apiService: ApiService) {

    // 获取瞬间流，使用 Flow 发射数据
    fun getMoments(): Flow<List<Moment>> = flow {
        try {
            val response = apiService.getMoments()
            emit(response.data) // 发射 data 列表
        } catch (e: Exception) {
            e.printStackTrace()
            emit(emptyList())
        }
    }
}