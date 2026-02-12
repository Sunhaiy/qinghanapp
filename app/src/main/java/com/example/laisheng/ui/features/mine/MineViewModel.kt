package com.example.laisheng.ui.features.mine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laisheng.data.remote.NetworkModule
import com.example.laisheng.data.model.FollowCounts
import com.example.laisheng.data.model.Moment
import com.example.laisheng.data.model.User
import com.example.laisheng.data.repository.MomentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class MineUiState {
    object Loading : MineUiState()
    data class Success(
        val user: User,
        val moments: List<Moment>,
        val likedMoments: List<Moment>,
        val collectedMoments: List<Moment>,
        val followCounts: FollowCounts,
        val mutualFriends: List<User>
    ) : MineUiState()
    data class Error(val message: String) : MineUiState()
}

enum class MineTab {
    MOMENTS, LIKED, COLLECTED
}

class MineViewModel : ViewModel() {
    private val repository = MomentRepository(NetworkModule.apiService)

    private val _uiState = MutableStateFlow<MineUiState>(MineUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _selectedTab = MutableStateFlow(MineTab.MOMENTS)
    val selectedTab = _selectedTab.asStateFlow()

    private var currentUserId: String? = null

    fun selectTab(tab: MineTab) {
        _selectedTab.value = tab
    }

    fun loadData(userId: String, isRefresh: Boolean = true) {
        currentUserId = userId
        viewModelScope.launch {
            if (isRefresh) _isRefreshing.value = true
            try {
                val user = repository.getUserProfile(userId)
                val followCounts = repository.getFollowCounts(userId) ?: FollowCounts(0, 0)
                val mutualFriends = repository.getMutualFollowing(userId)
                
                // 1. 获取所有列表的原始数据
                val momentsRaw = repository.getUserMoments(userId, currentUserId = userId)?.data ?: emptyList()
                val likedRaw = repository.getUserLikedMoments(userId, currentUserId = userId)
                val collectedRaw = repository.getUserCollections(userId, currentUserId = userId)

                // 2. 生成已点赞和已收藏的 ID 集合，确保跨 Tab 的一致性
                val likedIds = likedRaw.map { it.id }.toSet()
                val collectedIds = collectedRaw.map { it.id }.toSet()

                // 3. 核心修复：强制同步所有 Moment 的状态位，无论它在哪一个 Tab 列表里
                fun sync(m: Moment) = m.copy(
                    isLiked = likedIds.contains(m.id),
                    isCollected = collectedIds.contains(m.id)
                )

                val moments = momentsRaw.map(::sync)
                val liked = likedRaw.map(::sync)
                val collected = collectedRaw.map(::sync)

                if (user != null) {
                    _uiState.value = MineUiState.Success(user, moments, liked, collected, followCounts, mutualFriends)
                }
            } catch (e: Exception) {
                _uiState.value = MineUiState.Error(e.message ?: "网络错误")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun toggleLike(momentId: String, userId: String) {
        viewModelScope.launch {
            try {
                repository.toggleLike(userId, momentId)
                loadData(userId, isRefresh = false) // 交互后立即重新拉取并同步状态
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun toggleCollect(momentId: String, userId: String) {
        viewModelScope.launch {
            try {
                repository.toggleCollection(userId, momentId)
                loadData(userId, isRefresh = false)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }
}
