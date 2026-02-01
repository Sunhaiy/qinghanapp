package com.example.laisheng.ui.features.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laisheng.data.NetworkModule
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

    fun refresh(userId: String? = null) {
        viewModelScope.launch {
            _isRefreshing.value = true
            _moments.value = repository.getMoments(userId)
            _isRefreshing.value = false
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