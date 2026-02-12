package com.example.laisheng.ui.features.mine.follows

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laisheng.data.remote.NetworkModule
import com.example.laisheng.data.model.User
import com.example.laisheng.data.repository.MomentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class FollowUiState {
    object Loading : FollowUiState()
    data class Success(val users: List<User>) : FollowUiState()
    data class Error(val message: String) : FollowUiState()
}

class FollowViewModel : ViewModel() {
    private val repository = MomentRepository(NetworkModule.apiService)

    private val _uiState = MutableStateFlow<FollowUiState>(FollowUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    fun loadFollowers(userId: String) {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val users = NetworkModule.apiService.getFollowers(userId)
                _uiState.value = FollowUiState.Success(users)
            } catch (e: Exception) {
                _uiState.value = FollowUiState.Error(e.message ?: "加载失败")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun loadFollowing(userId: String) {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val users = NetworkModule.apiService.getFollowing(userId)
                _uiState.value = FollowUiState.Success(users)
            } catch (e: Exception) {
                _uiState.value = FollowUiState.Error(e.message ?: "加载失败")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun loadMutual(userId: String) {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val users = repository.getMutualFollowing(userId)
                _uiState.value = FollowUiState.Success(users)
            } catch (e: Exception) {
                _uiState.value = FollowUiState.Error(e.message ?: "加载失败")
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}