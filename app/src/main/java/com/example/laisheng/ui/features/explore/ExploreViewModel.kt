package com.example.laisheng.ui.features.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laisheng.data.remote.NetworkModule
import com.example.laisheng.data.model.Moment
import com.example.laisheng.data.repository.MomentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExploreViewModel : ViewModel() {

    private val repository = MomentRepository(NetworkModule.apiService)

    private val _moments = MutableStateFlow<List<Moment>>(emptyList())
    val moments = _moments.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

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

    fun onBookmarkClick(userId: String, momentId: String) {
        viewModelScope.launch {
            val isNowCollected = repository.toggleCollection(userId, momentId)
            _moments.value = _moments.value.map {
                if (it.id == momentId) {
                    it.copy(isCollected = isNowCollected)
                } else it
            }
        }
    }
}