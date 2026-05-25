package com.example.laisheng.ui.features.mine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laisheng.data.model.CollectionFolder
import com.example.laisheng.data.model.FollowCounts
import com.example.laisheng.data.model.Moment
import com.example.laisheng.data.model.User
import com.example.laisheng.data.remote.NetworkModule
import com.example.laisheng.data.repository.ApiMessageException
import com.example.laisheng.data.repository.MomentRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
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
        val mutualFriends: List<User>,
        val folders: List<CollectionFolder> = emptyList()
    ) : MineUiState()

    data class Error(val message: String) : MineUiState()
}

enum class MineTab {
    MOMENTS,
    LIKED,
    COLLECTED
}

class MineViewModel : ViewModel() {
    private val repository = MomentRepository(NetworkModule.apiService)

    private val _uiState = MutableStateFlow<MineUiState>(MineUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _selectedTab = MutableStateFlow(MineTab.MOMENTS)
    val selectedTab = _selectedTab.asStateFlow()

    private val _selectedFolderId = MutableStateFlow<String?>(null)
    val selectedFolderId = _selectedFolderId.asStateFlow()

    private val _message = MutableSharedFlow<String>()
    val message = _message.asSharedFlow()

    private var currentUserId: String? = null

    fun selectTab(tab: MineTab) {
        _selectedTab.value = tab
    }

    fun loadData(userId: String, isRefresh: Boolean = true) {
        currentUserId = userId
        viewModelScope.launch {
            if (isRefresh) _isRefreshing.value = true
            try {
                val user = repository.getCurrentUser() ?: repository.getUserProfile(userId)
                val effectiveUserId = user?.id ?: userId
                val followCounts = repository.getFollowCounts() ?: FollowCounts(0, 0)
                val mutualFriends = repository.getMutualFollowing()
                val momentsRaw = repository.getUserMoments(effectiveUserId, currentUserId = effectiveUserId)?.data.orEmpty()
                val likedRaw = repository.getUserLikedMoments(effectiveUserId, currentUserId = effectiveUserId)
                val folders = repository.getFolders()
                val collectedRaw = repository.getUserCollections(
                    userId = effectiveUserId,
                    currentUserId = effectiveUserId,
                    folderId = _selectedFolderId.value
                )

                val likedIds = likedRaw.map { it.id }.toSet()
                val collectedIds = collectedRaw.map { it.id }.toSet()

                fun sync(moment: Moment) = moment.copy(
                    isLiked = moment.id in likedIds,
                    isCollected = moment.id in collectedIds
                )

                if (user != null) {
                    _uiState.value = MineUiState.Success(
                        user = user,
                        moments = momentsRaw.map(::sync),
                        likedMoments = likedRaw.map(::sync),
                        collectedMoments = collectedRaw.map(::sync),
                        followCounts = followCounts,
                        mutualFriends = mutualFriends,
                        folders = folders
                    )
                } else {
                    _uiState.value = MineUiState.Error("加载用户信息失败")
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
            repository.toggleLike(momentId = momentId)
            loadData(userId, isRefresh = false)
        }
    }

    fun toggleCollect(momentId: String, userId: String) {
        viewModelScope.launch {
            repository.toggleCollection(momentId = momentId)
            loadData(userId, isRefresh = false)
        }
    }

    fun selectFolder(folderId: String?) {
        _selectedFolderId.value = folderId
        currentUserId?.let { loadData(it, isRefresh = false) }
    }

    fun createFolder(name: String) {
        viewModelScope.launch {
            val uid = currentUserId ?: return@launch
            try {
                repository.createFolder(name)
                loadData(uid, isRefresh = false)
                _message.emit("收藏夹已创建")
            } catch (e: ApiMessageException) {
                _message.emit(e.message ?: "创建收藏夹失败")
            } catch (_: Exception) {
                _message.emit("创建收藏夹失败")
            }
        }
    }

    fun deleteFolder(folderId: String) {
        viewModelScope.launch {
            currentUserId?.let { uid ->
                repository.deleteFolder(folderId)
                _selectedFolderId.value = null
                loadData(uid, isRefresh = false)
                _message.emit("收藏夹已删除")
            }
        }
    }

    fun moveCollectionToFolder(momentId: String, userId: String, folderId: String?) {
        viewModelScope.launch {
            repository.moveCollectionToFolder(momentId, folderId = folderId)
            loadData(userId, isRefresh = false)
            _message.emit(if (folderId == null) "已移到未分类" else "已移动到新的收藏夹")
        }
    }
}
