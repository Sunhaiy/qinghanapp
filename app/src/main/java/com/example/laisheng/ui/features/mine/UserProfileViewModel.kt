package com.example.laisheng.ui.features.mine

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.laisheng.data.local.UserPrefs
import com.example.laisheng.data.model.CollectionFolder
import com.example.laisheng.data.model.FollowCounts
import com.example.laisheng.data.model.FollowRelation
import com.example.laisheng.data.model.Moment
import com.example.laisheng.data.model.User
import com.example.laisheng.data.remote.NetworkModule
import com.example.laisheng.data.repository.MomentRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class UserProfileUiState {
    object Loading : UserProfileUiState()
    data class Success(
        val user: User,
        val moments: List<Moment>,
        val followCounts: FollowCounts,
        val relation: FollowRelation
    ) : UserProfileUiState()

    data class Error(val message: String) : UserProfileUiState()
}

class UserProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val userPrefs = UserPrefs(application.applicationContext)
    private val repository = MomentRepository(NetworkModule.apiService)

    private val _uiState = MutableStateFlow<UserProfileUiState>(UserProfileUiState.Loading)
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()

    private val _folders = MutableStateFlow<List<CollectionFolder>>(emptyList())
    val folders = _folders.asStateFlow()

    data class SnackbarEvent(
        val message: String,
        val actionLabel: String? = null,
        val momentId: String? = null
    )

    private val _snackbarEvent = MutableSharedFlow<SnackbarEvent>()
    val snackbarEvent = _snackbarEvent.asSharedFlow()

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
                val followCounts = repository.getFollowCounts(targetUserId) ?: FollowCounts(0, 0)
                val relation = if (targetUserId == currentUserId) {
                    FollowRelation()
                } else {
                    repository.getFollowRelation(targetUserId) ?: FollowRelation()
                }

                _uiState.value = UserProfileUiState.Success(
                    user = user.copy(isFollowed = relation.isFollowing),
                    moments = momentsResponse?.data.orEmpty(),
                    followCounts = followCounts,
                    relation = relation
                )
                _folders.value = repository.getFolders(currentUserId)
            } catch (e: Exception) {
                _uiState.value = UserProfileUiState.Error(e.message ?: "加载失败")
            }
        }
    }

    fun toggleFollow(targetUserId: String, currentUserId: String) {
        if (targetUserId == currentUserId) return

        viewModelScope.launch {
            runCatching { repository.toggleFollow(targetUserId) }
                .getOrNull()
                ?.let { isFollowed ->
                    val currentState = _uiState.value as? UserProfileUiState.Success ?: return@let
                    val updatedCounts = currentState.followCounts.copy(
                        followersCount = (currentState.followCounts.followersCount + if (isFollowed) 1 else -1)
                            .coerceAtLeast(0)
                    )
                    val updatedRelation = currentState.relation.copy(
                        isFollowing = isFollowed,
                        isMutual = isFollowed && currentState.relation.isFollowedBy
                    )
                    _uiState.value = currentState.copy(
                        user = currentState.user.copy(isFollowed = isFollowed),
                        followCounts = updatedCounts,
                        relation = updatedRelation
                    )
                }
        }
    }

    fun onLikeClick(userId: String?, momentId: String) {
        if (userId == null) return

        viewModelScope.launch {
            val isNowLiked = repository.toggleLike(momentId = momentId)
            val currentState = _uiState.value as? UserProfileUiState.Success ?: return@launch
            _uiState.value = currentState.copy(
                moments = currentState.moments.map { moment ->
                    if (moment.id == momentId) {
                        moment.copy(
                            isLiked = isNowLiked,
                            likesCount = if (isNowLiked) {
                                moment.likesCount + 1
                            } else {
                                (moment.likesCount - 1).coerceAtLeast(0)
                            }
                        )
                    } else {
                        moment
                    }
                }
            )
        }
    }

    fun onBookmarkClick(userId: String?, momentId: String) {
        if (userId == null) return

        viewModelScope.launch {
            val currentState = _uiState.value as? UserProfileUiState.Success ?: return@launch
            val moment = currentState.moments.find { it.id == momentId } ?: return@launch

            if (moment.isCollected) {
                val isStillCollected = repository.toggleCollection(userId, momentId)
                _uiState.value = currentState.copy(
                    moments = currentState.moments.map { item ->
                        if (item.id == momentId) item.copy(isCollected = isStillCollected, folderId = null) else item
                    }
                )
                return@launch
            }

            val isNowCollected = repository.toggleCollection(momentId = momentId)
            if (!isNowCollected) {
                _snackbarEvent.emit(SnackbarEvent("收藏失败，请重试"))
                return@launch
            }

            val lastFolderId = userPrefs.getLastCollectionFolder()
            repository.moveCollectionToFolder(momentId, userId, lastFolderId)

            _uiState.value = currentState.copy(
                moments = currentState.moments.map { item ->
                    if (item.id == momentId) {
                        item.copy(isCollected = true, folderId = lastFolderId)
                    } else {
                        item
                    }
                }
            )
            _snackbarEvent.emit(
                SnackbarEvent(
                    message = "已收藏到${folderDisplayName(lastFolderId)}",
                    actionLabel = "修改",
                    momentId = momentId
                )
            )
        }
    }

    fun confirmCollection(userId: String, momentId: String, folderId: String?) {
        viewModelScope.launch {
            userPrefs.saveLastCollectionFolder(folderId)
            repository.moveCollectionToFolder(momentId, userId, folderId)
            val currentState = _uiState.value as? UserProfileUiState.Success ?: return@launch
            _uiState.value = currentState.copy(
                moments = currentState.moments.map { moment ->
                    if (moment.id == momentId) moment.copy(isCollected = true, folderId = folderId) else moment
                }
            )
        }
    }

    private fun folderDisplayName(folderId: String?): String =
        if (folderId.isNullOrBlank()) {
            userPrefs.getDefaultCollectionFolderName()
        } else {
            _folders.value.firstOrNull { it.id == folderId }?.name ?: userPrefs.getDefaultCollectionFolderName()
        }
}
