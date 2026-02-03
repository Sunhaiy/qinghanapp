package com.example.laisheng.ui.features.mine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laisheng.data.NetworkModule
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
        val mutualFriends: List<User> // 关键修复：添加互关好友列表
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
        currentUserId?.let { loadData(it, isRefresh = false) }
    }

    fun loadData(userId: String, isRefresh: Boolean = true) {
        currentUserId = userId
        viewModelScope.launch {
            if (isRefresh) {
                _isRefreshing.value = true
            }

            try {
                // 并行获取基础数据
                val user = repository.getUserProfile(userId)
                val followCounts = repository.getFollowCounts(userId) ?: FollowCounts(0, 0)
                val mutualFriends = repository.getMutualFollowing(userId) // 获取互关好友
                
                // 根据当前 Tab 获取对应列表
                val moments = repository.getUserMoments(userId, currentUserId = userId)?.data ?: emptyList()
                val liked = repository.getUserLikedMoments(userId)
                val collected = repository.getUserCollections(userId)

                if (user != null) {
                    _uiState.value = MineUiState.Success(
                        user = user,
                        moments = moments,
                        likedMoments = liked,
                        collectedMoments = collected,
                        followCounts = followCounts,
                        mutualFriends = mutualFriends
                    )
                } else {
                    _uiState.value = MineUiState.Error("获取资料失败")
                }
            } catch (e: Exception) {
                _uiState.value = MineUiState.Error(e.message ?: "网络错误")
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}