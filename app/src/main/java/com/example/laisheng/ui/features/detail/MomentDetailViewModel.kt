package com.example.laisheng.ui.features.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.laisheng.data.local.UserPrefs
import com.example.laisheng.data.remote.NetworkModule
import com.example.laisheng.data.model.Comment
import com.example.laisheng.data.model.Moment
import com.example.laisheng.data.model.CollectionFolder
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

    private val _uiState = MutableStateFlow<MomentDetailUiState>(MomentDetailUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _isPostingComment = MutableStateFlow(false)
    val isPostingComment = _isPostingComment.asStateFlow()

    // Snackbar event
    data class SnackbarEvent(val message: String, val actionLabel: String? = null, val onAction: (() -> Unit)? = null)
    private val _snackbarEvent = MutableSharedFlow<SnackbarEvent>()
    val snackbarEvent = _snackbarEvent.asSharedFlow()

    fun loadMomentDetail(momentId: String, currentUserId: String?) {
        viewModelScope.launch {
            _uiState.value = MomentDetailUiState.Loading
            try {
                val moment = repository.getMomentDetail(momentId, currentUserId)
                val comments = repository.getMomentComments(momentId)
                if (moment != null) {
                    _uiState.value = MomentDetailUiState.Success(moment, comments)
                } else {
                    _uiState.value = MomentDetailUiState.Error("找不到该瞬间")
                }
            } catch (e: Exception) {
                _uiState.value = MomentDetailUiState.Error(e.message ?: "未知错误")
            }
        }
    }

    fun postComment(userId: String, momentId: String, content: String) {
        if (content.isBlank()) return
        
        viewModelScope.launch {
            _isPostingComment.value = true
            try {
                val newComment = repository.postComment(userId, momentId, content)
                if (newComment != null) {
                    val currentState = _uiState.value
                    if (currentState is MomentDetailUiState.Success) {
                        val updatedComments = repository.getMomentComments(momentId)
                        _uiState.value = currentState.copy(comments = updatedComments)
                    }
                }
            } catch (e: Exception) {
            } finally {
                _isPostingComment.value = false
            }
        }
    }

    fun onLikeClick(userId: String, momentId: String) {
        viewModelScope.launch {
            val isNowLiked = repository.toggleLike(userId, momentId)
            val currentState = _uiState.value
            if (currentState is MomentDetailUiState.Success) {
                val updatedMoment = currentState.moment.copy(
                    isLiked = isNowLiked,
                    likesCount = if (isNowLiked) currentState.moment.likesCount + 1 else currentState.moment.likesCount - 1
                )
                _uiState.value = currentState.copy(moment = updatedMoment)
            }
        }
    }

    fun onBookmarkClick(userId: String, momentId: String, onShowDialog: () -> Unit) {
        viewModelScope.launch {
            val currentState = _uiState.value as? MomentDetailUiState.Success ?: return@launch
            val moment = currentState.moment

            if (moment.isCollected) {
                // If currently collected: Toggle to remove
                val isStillCollected = repository.toggleCollection(userId, momentId)
                 _uiState.value = currentState.copy(
                    moment = moment.copy(isCollected = isStillCollected)
                )
                if (!isStillCollected) {
                     _snackbarEvent.emit(SnackbarEvent("已取消收藏"))
                }
            } else {
                // Not collected -> Add
                 val lastFolderId = userPrefs.getLastCollectionFolder()
                 val isNowCollected = repository.toggleCollection(userId, momentId)
                 
                 if (isNowCollected) {
                     if (lastFolderId != null) {
                         repository.moveCollectionToFolder(momentId, userId, lastFolderId)
                     }
                     
                    _uiState.value = currentState.copy(
                        moment = moment.copy(isCollected = true)
                    )
                     
                     val folderName = _folders.value.find { it.id == lastFolderId }?.name ?: "默认收藏夹"
                    _snackbarEvent.emit(SnackbarEvent(
                        message = "已收藏到 $folderName",
                        actionLabel = "修改",
                        onAction = onShowDialog
                    ))
                 } else {
                     _snackbarEvent.emit(SnackbarEvent("收藏失败，请重试"))
                 }
            }
        }
    }

    private val _folders = MutableStateFlow<List<CollectionFolder>>(emptyList())
    val folders = _folders.asStateFlow()

    fun loadFolders(userId: String) {
        viewModelScope.launch {
            val folders = repository.getFolders(userId)
            if (folders != null) {
                _folders.value = folders
            }
        }
    }

    fun confirmCollection(userId: String, momentId: String, folderId: String?) {
        viewModelScope.launch {
            userPrefs.saveLastCollectionFolder(folderId)
            repository.moveCollectionToFolder(momentId, userId, folderId)
            
            val folderName = if (folderId == null) "默认收藏夹" else _folders.value.find { it.id == folderId }?.name ?: "收藏夹"
            _snackbarEvent.emit(SnackbarEvent("已移动到 $folderName"))

            val currentState = _uiState.value
            if (currentState is MomentDetailUiState.Success) {
                _uiState.value = currentState.copy(
                    moment = currentState.moment.copy(isCollected = true)
                )
            }
        }
    }
}