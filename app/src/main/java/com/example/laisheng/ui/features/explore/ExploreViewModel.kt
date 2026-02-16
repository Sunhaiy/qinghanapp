package com.example.laisheng.ui.features.explore

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.laisheng.data.local.UserPrefs
import com.example.laisheng.data.remote.NetworkModule
import com.example.laisheng.data.model.Moment
import com.example.laisheng.data.model.CollectionFolder
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

    // Snackbar event: Message, ActionLabel, ActionCallback
    data class SnackbarEvent(val message: String, val actionLabel: String? = null, val onAction: (() -> Unit)? = null)
    private val _snackbarEvent = MutableSharedFlow<SnackbarEvent>()
    val snackbarEvent = _snackbarEvent.asSharedFlow()

    // 分页状态
    private var currentPage = 1
    private var isEndReached = false
    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore = _isLoadingMore.asStateFlow()

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
                } else it
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

    fun onBookmarkClick(userId: String, momentId: String, onShowDialog: () -> Unit) {
        viewModelScope.launch {
            val moment = _moments.value.find { it.id == momentId } ?: return@launch
            
            if (moment.isCollected) {
                // If already collected, toggle explicitly removes it
                val isStillCollected = repository.toggleCollection(userId, momentId)
                // Update local state
                 _moments.value = _moments.value.map {
                    if (it.id == momentId) it.copy(isCollected = isStillCollected) else it
                }
                if (!isStillCollected) {
                    _snackbarEvent.emit(SnackbarEvent("已取消收藏"))
                }
            } else {
                try {
                    // Not collected -> Add to last folder
                    var lastFolderId: String? = null
                    try {
                        lastFolderId = userPrefs.getLastCollectionFolder()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    
                    // Toggle to add
                    val isNowCollected = repository.toggleCollection(userId, momentId)
                    
                    if (isNowCollected) {
                        // Move to last folder if set
                        if (lastFolderId != null) {
                            repository.moveCollectionToFolder(momentId, userId, lastFolderId)
                        }
                        
                        // Update UI state
                        _moments.value = _moments.value.map {
                            if (it.id == momentId) it.copy(isCollected = true) else it
                        }
    
                        // Show Snackbar with "Modify" action
                        val folderName = _folders.value.find { it.id == lastFolderId }?.name ?: "默认收藏夹"
                        _snackbarEvent.emit(SnackbarEvent(
                            message = "已收藏到 $folderName",
                            actionLabel = "修改",
                            onAction = onShowDialog
                        ))
                    } else {
                        _snackbarEvent.emit(SnackbarEvent("收藏失败，请重试"))
                    }
                } catch (t: Throwable) {
                    t.printStackTrace()
                    _snackbarEvent.emit(SnackbarEvent("操作出错: ${t.message}"))
                }
            }
        }
    }

    fun confirmCollection(userId: String, momentId: String, folderId: String?) {
        viewModelScope.launch {
            // Save as last used
            userPrefs.saveLastCollectionFolder(folderId)

            // Move
            repository.moveCollectionToFolder(momentId, userId, folderId)
            
            val folderName = if (folderId == null) "默认收藏夹" else _folders.value.find { it.id == folderId }?.name ?: "收藏夹"
            _snackbarEvent.emit(SnackbarEvent("已移动到 $folderName"))

             // Ensure local state is collected (though it should be already)
             _moments.value = _moments.value.map {
                if (it.id == momentId) {
                    it.copy(isCollected = true)
                } else it
            }
        }
    }
}