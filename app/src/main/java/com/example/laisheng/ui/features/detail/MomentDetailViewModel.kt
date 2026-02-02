package com.example.laisheng.ui.features.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laisheng.data.NetworkModule
import com.example.laisheng.data.model.Comment
import com.example.laisheng.data.model.Moment
import com.example.laisheng.data.repository.MomentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class MomentDetailUiState {
    object Loading : MomentDetailUiState()
    data class Success(val moment: Moment, val comments: List<Comment>) : MomentDetailUiState()
    data class Error(val message: String) : MomentDetailUiState()
}

class MomentDetailViewModel : ViewModel() {
    private val repository = MomentRepository(NetworkModule.apiService)

    private val _uiState = MutableStateFlow<MomentDetailUiState>(MomentDetailUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _isPostingComment = MutableStateFlow(false)
    val isPostingComment = _isPostingComment.asStateFlow()

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

    fun onBookmarkClick(userId: String, momentId: String) {
        viewModelScope.launch {
            val isNowCollected = repository.toggleCollection(userId, momentId)
            val currentState = _uiState.value
            if (currentState is MomentDetailUiState.Success) {
                val updatedMoment = currentState.moment.copy(isCollected = isNowCollected)
                _uiState.value = currentState.copy(moment = updatedMoment)
            }
        }
    }
}