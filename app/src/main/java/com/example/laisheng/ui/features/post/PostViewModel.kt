package com.example.laisheng.ui.features.post

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laisheng.data.NetworkModule
import com.example.laisheng.data.repository.MomentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class PostUiState {
    object Idle : PostUiState()
    object Loading : PostUiState()
    object Success : PostUiState()
    data class Error(val message: String) : PostUiState()
}

class PostViewModel : ViewModel() {
    private val repository = MomentRepository(NetworkModule.apiService)

    private val _uiState = MutableStateFlow<PostUiState>(PostUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun createMoment(userId: String, content: String) {
        if (content.isBlank()) {
            _uiState.value = PostUiState.Error("内容不能为空")
            return
        }

        viewModelScope.launch {
            _uiState.value = PostUiState.Loading
            try {
                // 我们之前在 ApiService 里定义了 createMoment
                // 这里调用它
                val response = NetworkModule.apiService.createMoment(
                    com.example.laisheng.data.model.CreateMomentRequest(userId, content)
                )
                _uiState.value = PostUiState.Success
            } catch (e: Exception) {
                _uiState.value = PostUiState.Error("发布失败: ${e.message}")
            }
        }
    }
    
    fun resetState() {
        _uiState.value = PostUiState.Idle
    }
}