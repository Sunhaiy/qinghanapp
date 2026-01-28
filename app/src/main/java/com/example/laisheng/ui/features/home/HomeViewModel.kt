package com.example.laisheng.ui.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laisheng.data.model.Post
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    // 用于横向滑动 Pager 的精选帖子列表
    private val _featuredPosts = MutableStateFlow<List<Post>>(emptyList())
    val featuredPosts: StateFlow<List<Post>> = _featuredPosts

    init {
        // 加载精选帖子数据
        fetchFeaturedPosts()
    }

    // 模拟获取精选帖子数据
    private fun fetchFeaturedPosts() {
        viewModelScope.launch {
            val posts = listOf(
                Post(
                    id = 101,
                    username = "编辑精选",
                    handle = "@featured",
                    content = "探索未知，发现自我，每一次点击都有新的故事。",
                    timestamp = "刚刚",
                    likeCount = 1024,
                    commentCount = 256
                ),
                Post(
                    id = 102,
                    username = "热门趋势",
                    handle = "@trending",
                    content = "今日热门：与我们一起，深入探讨最新的科技动态与未来趋势。",
                    timestamp = "1小时前",
                    likeCount = 2048,
                    commentCount = 512
                ),
                Post(
                    id = 103,
                    username = "灵感角落",
                    handle = "@inspiration",
                    content = "捕捉生活中的小确幸，分享那些让你会心一笑的瞬间。",
                    timestamp = "3小时前",
                    likeCount = 876,
                    commentCount = 128
                )
            )
            _featuredPosts.value = posts
        }
    }
}
