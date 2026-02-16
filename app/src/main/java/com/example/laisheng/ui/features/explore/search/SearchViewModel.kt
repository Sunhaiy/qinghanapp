package com.example.laisheng.ui.features.explore.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.laisheng.data.model.Moment
import com.example.laisheng.data.model.User
import com.example.laisheng.data.remote.NetworkModule
import com.example.laisheng.data.repository.MomentRepository
import com.example.laisheng.ui.features.explore.ExploreViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MomentRepository(NetworkModule.apiService)

    private val userPrefs = com.example.laisheng.data.local.UserPrefs(application)
    
    // Search States
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    val searchHistory = _searchHistory.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        _searchHistory.value = userPrefs.getSearchHistory().toList().sorted().reversed() // Simple reverse order for now
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

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    private val _searchType = MutableStateFlow(SearchType.MOMENTS)
    val searchType = _searchType.asStateFlow()

    private val _momentSearchResults = MutableStateFlow<List<Moment>>(emptyList())
    val momentSearchResults = _momentSearchResults.asStateFlow()

    private val _userSearchResults = MutableStateFlow<List<User>>(emptyList())
    val userSearchResults = _userSearchResults.asStateFlow()

    enum class SearchType {
        MOMENTS, USERS
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
                    val users = repository.searchUsers(finalQuery)
                    _userSearchResults.value = users
                }
            } catch (e: Exception) {
                e.printStackTrace()
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
    
    // Re-implement basic interaction logic for search results (Like, Bookmark) if needed details are required.
    // For now, simple interactions can be passed through callbacks or handled if strictly necessary locally.
    // However, usually detailed interactions happen on Detail screens. 
    // If we need like/bookmark on search results directly:
    
    fun onLikeClick(userId: String, momentId: String) {
        viewModelScope.launch {
            val isNowLiked = repository.toggleLike(userId, momentId)
            _momentSearchResults.value = _momentSearchResults.value.map {
                if (it.id == momentId) {
                    it.copy(
                        isLiked = isNowLiked,
                        likesCount = if (isNowLiked) it.likesCount + 1 else it.likesCount - 1
                    )
                } else it
            }
        }
    }

    fun onBookmarkClick(userId: String, momentId: String, onShowDialog: () -> Unit) {
         viewModelScope.launch {
             // Basic bookmark toggle for search results
            val moment = _momentSearchResults.value.find { it.id == momentId } ?: return@launch
             val isNowCollected = repository.toggleCollection(userId, momentId)
             _momentSearchResults.value = _momentSearchResults.value.map {
                 if (it.id == momentId) it.copy(isCollected = isNowCollected) else it
             }
         }
    }
}
