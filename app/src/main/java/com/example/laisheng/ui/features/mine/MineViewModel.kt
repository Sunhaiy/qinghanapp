package com.example.laisheng.ui.features.mine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laisheng.data.NetworkModule
import com.example.laisheng.data.model.Moment
import com.example.laisheng.data.model.User
import com.example.laisheng.data.repository.MomentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class MineUiState {
    object Loading : MineUiState()
    data class Success(val user: User, val moments: List<Moment>) : MineUiState()
    data class Error(val message: String) : MineUiState()
}

class MineViewModel : ViewModel() {
    private val repository = MomentRepository(NetworkModule.apiService)

    private val _uiState = MutableStateFlow<MineUiState>(MineUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private var currentPage = 1
    private var isEndReached = false

    fun loadData(userId: String, isRefresh: Boolean = true) {
        viewModelScope.launch {
            if (isRefresh) {
                _isRefreshing.value = true
                currentPage = 1
                isEndReached = false
            }

            try {
                // 1. 获取用户信息
                val user = repository.getUserProfile(userId)
                // 2. 获取用户动态
                val response = repository.getUserMoments(userId, page = currentPage, currentUserId = userId)

                if (user != null && response != null) {
                    val currentMoments = if (isRefresh) response.data else {
                        val current = (_uiState.value as? MineUiState.Success)?.moments ?: emptyList()
                        current + response.data
                    }
                    
                    _uiState.value = MineUiState.Success(user, currentMoments)
                    
                    if (response.data.size < response.limit) {
                        isEndReached = true
                    }
                } else {
                    _uiState.value = MineUiState.Error("获取数据失败")
                }
            } catch (e: Exception) {
                _uiState.value = MineUiState.Error(e.message ?: "未知错误")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun loadNextPage(userId: String) {
        if (isEndReached || _uiState.value !is MineUiState.Success) return
        currentPage++
        loadData(userId, isRefresh = false)
    }
}