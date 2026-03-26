package com.example.laisheng.ui.features.mine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laisheng.data.model.FollowCounts
import com.example.laisheng.data.model.Moment
import com.example.laisheng.data.model.User
import com.example.laisheng.data.remote.NetworkModule
import com.example.laisheng.data.repository.MomentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class UserProfileUiState {
    object Loading : UserProfileUiState()
    data class Success(
        val user: User,
        val moments: List<Moment>,
        val followCounts: FollowCounts
    ) : UserProfileUiState()

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
                val user = repository.getUserProfile(targetUserId, currentUserId)
                    ?: throw IllegalStateException("用户不存在")
                val momentsResponse = repository.getUserMoments(
                    userId = targetUserId,
                    currentUserId = currentUserId
                )
                val followCounts = if (targetUserId == currentUserId) {
                    repository.getFollowCounts() ?: FollowCounts(0, 0)
                } else {
                    FollowCounts(0, 0)
                }

                _uiState.value = UserProfileUiState.Success(
                    user = user,
                    moments = momentsResponse?.data.orEmpty(),
                    followCounts = followCounts
                )
            } catch (e: Exception) {
                _uiState.value = UserProfileUiState.Error(e.message ?: "加载失败")
            }
        }
    }

    fun toggleFollow(targetUserId: String, currentUserId: String) {
        viewModelScope.launch {
            try {
                val isFollowed = repository.toggleFollow(targetUserId)
                val currentState = _uiState.value as? UserProfileUiState.Success ?: return@launch
                val updatedUser = currentState.user.copy(isFollowed = isFollowed)
                _uiState.value = currentState.copy(user = updatedUser)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun onLikeClick(userId: String?, momentId: String) {
        if (userId == null) return
        viewModelScope.launch {
            val isNowLiked = repository.toggleLike(momentId = momentId)
            val currentState = _uiState.value as? UserProfileUiState.Success ?: return@launch
            val updatedMoments = currentState.moments.map { moment ->
                if (moment.id == momentId) {
                    moment.copy(
                        isLiked = isNowLiked,
                        likesCount = if (isNowLiked) moment.likesCount + 1 else moment.likesCount - 1
                    )
                } else {
                    moment
                }
            }
            _uiState.value = currentState.copy(moments = updatedMoments)
        }
    }

    fun onBookmarkClick(userId: String?, momentId: String) {
        if (userId == null) return
        viewModelScope.launch {
            val isNowCollected = repository.toggleCollection(momentId = momentId)
            val currentState = _uiState.value as? UserProfileUiState.Success ?: return@launch
            val updatedMoments = currentState.moments.map { moment ->
                if (moment.id == momentId) moment.copy(isCollected = isNowCollected) else moment
            }
            _uiState.value = currentState.copy(moments = updatedMoments)
        }
    }
}
