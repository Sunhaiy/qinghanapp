package com.example.laisheng.ui.features.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.laisheng.data.local.UserPrefs
import com.example.laisheng.data.model.CollectionFolder
import com.example.laisheng.data.model.Comment
import com.example.laisheng.data.model.Moment
import com.example.laisheng.data.remote.NetworkModule
import com.example.laisheng.data.repository.MomentRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class MomentDetailUiState {
    object Loading : MomentDetailUiState()
    data class Success(val moment: Moment, val comments: List<Comment>) : MomentDetailUiState()
    data class Error(val message: String) : MomentDetailUiState()
}

class MomentDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val userPrefs = UserPrefs(application.applicationContext)
    private val repository = MomentRepository(NetworkModule.apiService)

    data class SnackbarEvent(
        val message: String,
        val actionLabel: String? = null,
        val momentId: String? = null
    )

    private val _uiState = MutableStateFlow<MomentDetailUiState>(MomentDetailUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _isPostingComment = MutableStateFlow(false)
    val isPostingComment = _isPostingComment.asStateFlow()

    private val _snackbarEvent = MutableSharedFlow<SnackbarEvent>()
    val snackbarEvent = _snackbarEvent.asSharedFlow()

    private val _folders = MutableStateFlow<List<CollectionFolder>>(emptyList())
    val folders = _folders.asStateFlow()

    fun loadMomentDetail(momentId: String, currentUserId: String?) {
        viewModelScope.launch {
            _uiState.value = MomentDetailUiState.Loading
            try {
                val moment = repository.getMomentDetail(momentId, currentUserId)
                val comments = repository.getMomentComments(momentId)
                if (moment != null) {
                    repository.recordHistoryView(momentId, "detail")
                    _uiState.value = MomentDetailUiState.Success(moment, comments)
                } else {
                    _uiState.value = MomentDetailUiState.Error("找不到这条瞬间")
                }
            } catch (e: Exception) {
                _uiState.value = MomentDetailUiState.Error(e.message ?: "加载失败")
            }
        }
    }

    fun postComment(userId: String, momentId: String, content: String) {
        if (content.isBlank()) return

        viewModelScope.launch {
            _isPostingComment.value = true
            try {
                val newComment = repository.postComment(userId, momentId, content.trim())
                if (newComment == null) {
                    _snackbarEvent.emit(SnackbarEvent("评论发送失败"))
                    return@launch
                }

                val currentState = _uiState.value as? MomentDetailUiState.Success ?: return@launch
                val updatedComments = repository.getMomentComments(momentId)
                _uiState.value = currentState.copy(
                    moment = currentState.moment.copy(commentsCount = updatedComments.size),
                    comments = updatedComments
                )
            } catch (e: Exception) {
                _snackbarEvent.emit(SnackbarEvent(e.message ?: "评论发送失败"))
            } finally {
                _isPostingComment.value = false
            }
        }
    }

    fun onLikeClick(userId: String, momentId: String) {
        viewModelScope.launch {
            val isNowLiked = repository.toggleLike(userId, momentId)
            val currentState = _uiState.value as? MomentDetailUiState.Success ?: return@launch
            _uiState.value = currentState.copy(
                moment = currentState.moment.copy(
                    isLiked = isNowLiked,
                    likesCount = if (isNowLiked) {
                        currentState.moment.likesCount + 1
                    } else {
                        (currentState.moment.likesCount - 1).coerceAtLeast(0)
                    }
                )
            )
        }
    }

    fun onBookmarkClick(userId: String, momentId: String) {
        viewModelScope.launch {
            val currentState = _uiState.value as? MomentDetailUiState.Success ?: return@launch
            val moment = currentState.moment

            if (moment.isCollected) {
                val isStillCollected = repository.toggleCollection(userId, momentId)
                _uiState.value = currentState.copy(moment = moment.copy(isCollected = isStillCollected, folderId = null))
                return@launch
            }

            val isNowCollected = repository.toggleCollection(userId, momentId)
            if (!isNowCollected) {
                _snackbarEvent.emit(SnackbarEvent("收藏失败，请重试"))
                return@launch
            }

            val lastFolderId = userPrefs.getLastCollectionFolder()
            repository.moveCollectionToFolder(momentId, userId, lastFolderId)
            _uiState.value = currentState.copy(moment = moment.copy(isCollected = true, folderId = lastFolderId))
            _snackbarEvent.emit(
                SnackbarEvent(
                    message = "已收藏到${folderDisplayName(lastFolderId)}",
                    actionLabel = "修改",
                    momentId = momentId
                )
            )
        }
    }

    fun loadFolders(userId: String) {
        viewModelScope.launch {
            _folders.value = repository.getFolders(userId)
        }
    }

    fun confirmCollection(userId: String, momentId: String, folderId: String?) {
        viewModelScope.launch {
            userPrefs.saveLastCollectionFolder(folderId)
            repository.moveCollectionToFolder(momentId, userId, folderId)
            val currentState = _uiState.value as? MomentDetailUiState.Success ?: return@launch
            _uiState.value = currentState.copy(
                moment = currentState.moment.copy(isCollected = true, folderId = folderId)
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
