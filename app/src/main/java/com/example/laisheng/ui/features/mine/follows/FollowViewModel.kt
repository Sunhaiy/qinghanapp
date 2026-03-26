package com.example.laisheng.ui.features.mine.follows

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laisheng.data.model.User
import com.example.laisheng.data.remote.NetworkModule
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
        load { repository.getFollowers() }
    }

    fun loadFollowing(userId: String) {
        load { repository.getFollowing() }
    }

    fun loadMutual(userId: String) {
        load { repository.getMutualFollowing() }
    }

    private fun load(block: suspend () -> List<User>) {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                _uiState.value = FollowUiState.Success(block())
            } catch (e: Exception) {
                _uiState.value = FollowUiState.Error(e.message ?: "加载失败")
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}
