package com.example.laisheng.ui.features.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.laisheng.data.local.UserPrefs
import com.example.laisheng.data.model.Moment
import com.example.laisheng.data.remote.NetworkModule
import com.example.laisheng.data.repository.MomentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MomentRepository(NetworkModule.apiService)
    private val userPrefs = UserPrefs(application)
    private val currentUserId = userPrefs.getUserId()

    private val _featuredMoments = MutableStateFlow<List<Moment>>(emptyList())
    val featuredMoments = _featuredMoments.asStateFlow()

    private val _followingMoments = MutableStateFlow<List<Moment>>(emptyList())
    val followingMoments = _followingMoments.asStateFlow()
    
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private var followingPage = 1
    private val followingLimit = 10
    private var isFollowingEndReached = false

    init {
        refresh()
    }

    fun refresh() {
        if (currentUserId.isNullOrEmpty()) return
        
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                // Fetch Featured
                val featured = repository.getFeaturedMoments(currentUserId)
                _featuredMoments.value = featured ?: emptyList()

                // Fetch Following (Reset)
                followingPage = 1
                isFollowingEndReached = false
                val following = repository.getFollowingMoments(followingPage, followingLimit, currentUserId)
                _followingMoments.value = following?.data ?: emptyList()
                if ((following?.data?.size ?: 0) < followingLimit) {
                    isFollowingEndReached = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isRefreshing.value = false
            }
        }
    }
    
    fun loadMoreFollowing() {
        if (isFollowingEndReached || _isRefreshing.value || currentUserId.isNullOrEmpty()) return
        
        viewModelScope.launch {
            try {
                followingPage++
                val response = repository.getFollowingMoments(followingPage, followingLimit, currentUserId)
                val newItems = response?.data ?: emptyList()
                
                if (newItems.isNotEmpty()) {
                    _followingMoments.value += newItems
                }
                
                if (newItems.size < followingLimit) {
                    isFollowingEndReached = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
                followingPage--
            }
        }
    }
    
    // Stub methods for interactions (Like, Collect, etc.) - can reuse logic from other ViewModels or Repository
}
