package com.example.laisheng.ui.features.explore

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.laisheng.data.local.UserPrefs
import com.example.laisheng.data.model.CollectionFolder
import com.example.laisheng.data.model.Moment
import com.example.laisheng.data.remote.NetworkModule
import com.example.laisheng.data.repository.MomentRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExploreViewModel(application: Application) : AndroidViewModel(application) {
    private val userPrefs = UserPrefs(application.applicationContext)
    private val repository = MomentRepository(NetworkModule.apiService)

    private val _moments = MutableStateFlow<List<Moment>>(emptyList())
    val moments = _moments.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    data class SnackbarEvent(
        val message: String,
        val actionLabel: String? = null,
        val momentId: String? = null
    )
    private val _snackbarEvent = MutableSharedFlow<SnackbarEvent>()
    val snackbarEvent = _snackbarEvent.asSharedFlow()

    private var currentPage = 1
    private var isEndReached = false
    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore = _isLoadingMore.asStateFlow()

    private val _folders = MutableStateFlow<List<CollectionFolder>>(emptyList())
    val folders = _folders.asStateFlow()

    fun refresh(userId: String? = null) {
        viewModelScope.launch {
            _isRefreshing.value = true
            currentPage = 1
            isEndReached = false
            val response = repository.getMoments(page = currentPage, currentUserId = userId)
            if (response != null) {
                _moments.value = response.data
                if (response.data.isEmpty() || response.data.size < response.limit) {
                    isEndReached = true
                }
            }
            _isRefreshing.value = false
        }
    }

    fun loadNextPage(userId: String? = null) {
        if (_isLoadingMore.value || isEndReached) return

        viewModelScope.launch {
            _isLoadingMore.value = true
            val nextPage = currentPage + 1
            val response = repository.getMoments(page = nextPage, currentUserId = userId)
            if (response != null) {
                if (response.data.isNotEmpty()) {
                    _moments.value = _moments.value + response.data
                    currentPage = nextPage
                }
                if (response.data.size < response.limit) {
                    isEndReached = true
                }
            } else {
                isEndReached = true
            }
            _isLoadingMore.value = false
        }
    }

    fun onLikeClick(userId: String, momentId: String) {
        viewModelScope.launch {
            val isNowLiked = repository.toggleLike(userId, momentId)
            _moments.value = _moments.value.map {
                if (it.id == momentId) {
                    it.copy(
                        isLiked = isNowLiked,
                        likesCount = if (isNowLiked) it.likesCount + 1 else it.likesCount - 1
                    )
                } else {
                    it
                }
            }
        }
    }

    fun loadFolders(userId: String) {
        viewModelScope.launch {
            _folders.value = repository.getFolders(userId)
        }
    }

    fun onBookmarkClick(userId: String, momentId: String) {
        viewModelScope.launch {
            val moment = _moments.value.find { it.id == momentId } ?: return@launch

            if (moment.isCollected) {
                val isStillCollected = repository.toggleCollection(userId, momentId)
                _moments.value = _moments.value.map {
                    if (it.id == momentId) it.copy(isCollected = isStillCollected) else it
                }
                return@launch
            }

            val isNowCollected = repository.toggleCollection(userId, momentId)
            if (!isNowCollected) {
                _snackbarEvent.emit(SnackbarEvent("收藏失败，请重试"))
                return@launch
            }

            val lastFolderId = userPrefs.getLastCollectionFolder()
            repository.moveCollectionToFolder(momentId, userId, lastFolderId)
            val folderName = folderDisplayName(lastFolderId)
            _moments.value = _moments.value.map {
                if (it.id == momentId) it.copy(isCollected = true, folderId = lastFolderId) else it
            }
            _snackbarEvent.emit(
                SnackbarEvent(
                    message = "已收藏到$folderName",
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
            _moments.value = _moments.value.map {
                if (it.id == momentId) it.copy(isCollected = true, folderId = folderId) else it
            }
        }
    }

    private fun folderDisplayName(folderId: String?): String =
        if (folderId.isNullOrBlank()) {
            userPrefs.getDefaultCollectionFolderName()
        } else {
            _folders.value.firstOrNull { it.id == folderId }?.name ?: userPrefs.getDefaultCollectionFolderName()
        }
}
