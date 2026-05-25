package com.example.laisheng.ui.features.explore.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.laisheng.data.local.UserPrefs
import com.example.laisheng.data.model.CollectionFolder
import com.example.laisheng.data.model.Moment
import com.example.laisheng.data.model.User
import com.example.laisheng.data.remote.NetworkModule
import com.example.laisheng.data.repository.MomentRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MomentRepository(NetworkModule.apiService)
    private val userPrefs = UserPrefs(application)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    val searchHistory = _searchHistory.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    private val _searchType = MutableStateFlow(SearchType.MOMENTS)
    val searchType = _searchType.asStateFlow()

    private val _momentSearchResults = MutableStateFlow<List<Moment>>(emptyList())
    val momentSearchResults = _momentSearchResults.asStateFlow()

    private val _userSearchResults = MutableStateFlow<List<User>>(emptyList())
    val userSearchResults = _userSearchResults.asStateFlow()

    private val _folders = MutableStateFlow<List<CollectionFolder>>(emptyList())
    val folders = _folders.asStateFlow()

    data class SnackbarEvent(
        val message: String,
        val actionLabel: String? = null,
        val momentId: String? = null
    )

    private val _snackbarEvent = MutableSharedFlow<SnackbarEvent>()
    val snackbarEvent = _snackbarEvent.asSharedFlow()

    enum class SearchType {
        MOMENTS,
        USERS
    }

    init {
        loadHistory()
    }

    private fun loadHistory() {
        _searchHistory.value = userPrefs.getSearchHistory().toList().sorted().reversed()
    }

    fun loadFolders(userId: String) {
        viewModelScope.launch {
            _folders.value = repository.getFolders(userId)
        }
    }

    fun addToHistory(query: String) {
        if (query.isBlank()) return
        val current = userPrefs.getSearchHistory().toMutableSet()
        current.add(query)
        userPrefs.saveSearchHistory(current)
        loadHistory()
    }

    fun clearHistory() {
        userPrefs.clearSearchHistory()
        loadHistory()
    }

    fun removeFromHistory(query: String) {
        val current = userPrefs.getSearchHistory().toMutableSet()
        current.remove(query)
        userPrefs.saveSearchHistory(current)
        loadHistory()
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        if (query.isEmpty()) {
            clearResults()
        }
    }

    fun onSearchTypeChange(type: SearchType) {
        _searchType.value = type
        if (_searchQuery.value.isNotEmpty()) {
            performSearch(_searchQuery.value)
        }
    }

    fun performSearch(query: String? = null) {
        val finalQuery = query ?: _searchQuery.value
        if (finalQuery.isBlank()) return

        addToHistory(finalQuery)
        _isSearching.value = true
        viewModelScope.launch {
            try {
                if (_searchType.value == SearchType.MOMENTS) {
                    val response = repository.searchMoments(finalQuery, page = 1)
                    _momentSearchResults.value = response?.data ?: emptyList()
                } else {
                    _userSearchResults.value = repository.searchUsers(finalQuery)
                }
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun clearSearch() {
        _searchQuery.value = ""
        clearResults()
    }

    private fun clearResults() {
        _isSearching.value = false
        _momentSearchResults.value = emptyList()
        _userSearchResults.value = emptyList()
    }

    fun onLikeClick(userId: String, momentId: String) {
        viewModelScope.launch {
            val isNowLiked = repository.toggleLike(userId, momentId)
            _momentSearchResults.value = _momentSearchResults.value.map {
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

    fun onBookmarkClick(userId: String, momentId: String) {
        viewModelScope.launch {
            val moment = _momentSearchResults.value.find { it.id == momentId } ?: return@launch

            if (moment.isCollected) {
                val isStillCollected = repository.toggleCollection(userId, momentId)
                _momentSearchResults.value = _momentSearchResults.value.map {
                    if (it.id == momentId) it.copy(isCollected = isStillCollected, folderId = null) else it
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
            _momentSearchResults.value = _momentSearchResults.value.map {
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
            _momentSearchResults.value = _momentSearchResults.value.map {
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
