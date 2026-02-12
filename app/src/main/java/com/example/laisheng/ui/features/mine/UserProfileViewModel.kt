package com.example.laisheng.ui.features.mine

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laisheng.data.local.UserPrefs
import com.example.laisheng.data.model.User
import com.example.laisheng.data.model.Moment
import com.example.laisheng.data.remote.NetworkModule
import com.example.laisheng.data.remote.SocketManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.laisheng.data.model.FollowCounts
import com.example.laisheng.data.repository.MomentRepository

sealed class UserProfileUiState {
    object Loading : UserProfileUiState()
    data class Success(val user: User, val moments: List<Moment>, val followCounts: FollowCounts) : UserProfileUiState()
    data class Error(val message: String) : UserProfileUiState()
}

class UserProfileViewModel : ViewModel() {
    private val repository = MomentRepository(NetworkModule.apiService)
    private val _uiState = MutableStateFlow<UserProfileUiState>(UserProfileUiState.Loading)
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()

    fun loadProfile(targetUserId: String, currentUserId: String) {
        viewModelScope.launch {
            _uiState.value = UserProfileUiState.Loading
            try {
                // 并行请求优化：使用 async/await (这里保持简单顺次调用，实际项目中可用 async)
                val user = repository.getUserProfile(targetUserId, currentUserId) ?: throw Exception("用户不存在")
                val momentsResponse = repository.getUserMoments(targetUserId, currentUserId = currentUserId)
                val followCounts = repository.getFollowCounts(targetUserId) ?: FollowCounts(0, 0)
                
                _uiState.value = UserProfileUiState.Success(user, momentsResponse?.data ?: emptyList(), followCounts)
            } catch (e: Exception) {
                _uiState.value = UserProfileUiState.Error(e.message ?: "加载失败")
            }
        }
    }

    fun toggleFollow(targetUserId: String, currentUserId: String) {
        viewModelScope.launch {
            try {
                val result = NetworkModule.apiService.toggleFollow(mapOf("followerId" to currentUserId, "followingId" to targetUserId))
                val isFollowed = result["followed"] ?: false
                
                val currentState = _uiState.value
                if (currentState is UserProfileUiState.Success) {
                    val updatedUser = currentState.user.copy(isFollowed = isFollowed)
                    val oldCounts = currentState.followCounts
                    val newFollowersCount = if (isFollowed) oldCounts.followersCount + 1 else oldCounts.followersCount - 1
                    val updatedCounts = oldCounts.copy(followersCount = newFollowersCount)
                    
                    _uiState.value = currentState.copy(user = updatedUser, followCounts = updatedCounts)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun onLikeClick(userId: String?, momentId: String) {
        if (userId == null) return
        viewModelScope.launch {
            val isNowLiked = repository.toggleLike(userId, momentId)
            
            val currentState = _uiState.value
            if (currentState is UserProfileUiState.Success) {
                val updatedMoments = currentState.moments.map { moment ->
                    if (moment.id == momentId) {
                        moment.copy(
                            isLiked = isNowLiked,
                            likesCount = if (isNowLiked) moment.likesCount + 1 else moment.likesCount - 1
                        )
                    } else moment
                }
                _uiState.value = currentState.copy(moments = updatedMoments)
            }
        }
    }

    fun onBookmarkClick(userId: String?, momentId: String) {
        if (userId == null) return
        viewModelScope.launch {
            // 注意：PostCard 可能需要 isCollected 字段来显示收藏状态。
            // Moment 模型里是 isCollected 还是 isBookmarked? 查看 API 返回。
            // 假设 MomentRepository.toggleCollection 返回新的状态
            val isNowCollected = repository.toggleCollection(userId, momentId)
            
             val currentState = _uiState.value
            if (currentState is UserProfileUiState.Success) {
                val updatedMoments = currentState.moments.map { moment ->
                    if (moment.id == momentId) {
                        moment.copy(isCollected = isNowCollected)
                    } else moment
                }
                _uiState.value = currentState.copy(moments = updatedMoments)
            }
        }
    }
}
