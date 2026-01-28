package com.example.laisheng.ui.features.explore

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.laisheng.data.model.Post
import com.example.laisheng.ui.composes.PostCard
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

@Composable
fun ExploreScreen(hazeState: HazeState) {
    // 为探索页面添加更多卡片内容
    val mockPosts = listOf(
        Post(
            id = 1,
            username = "云游",
            handle = "@clouds",
            content = "有时候，最震耳欲聋的，是沉默。\n\n不需要过多的解释，懂你的人自然会懂。\n在这个快节奏的时代，能找到一个可以一起发呆的人，也是一种奢侈。",
            timestamp = "1小时前",
            likeCount = 244,
            commentCount = 33
        ),
        Post(
            id = 2,
            username = "开发者",
            handle = "@dev_user",
            content = "今天又写了一整天的代码，虽然很累，但是看到成果真的很开心。#Android #Compose",
            timestamp = "2小时前",
            likeCount = 102,
            commentCount = 5
        ),
        Post(
            id = 3,
            username = "美食家",
            handle = "@foodie",
            content = "刚刚发现了一家超赞的街边小吃，幸福感爆棚！没有什么是一顿美食解决不了的。",
            timestamp = "3小时前",
            likeCount = 378,
            commentCount = 88
        ),
        Post(
            id = 4,
            username = "旅行的意义",
            handle = "@traveler",
            content = "世界的尽头，是回家的路。但在那之前，我想先看看世界的所有风景。",
            timestamp = "5小时前",
            likeCount = 199,
            commentCount = 21
        ),
        Post(
            id = 5,
            username = "书虫",
            handle = "@bookworm",
            content = "在书中，我遇见了未曾谋面的自己，也抵达了从未踏足的远方。",
            timestamp = "昨天",
            likeCount = 98,
            commentCount = 12
        ),
        Post(
            id = 6,
            username = "电影迷",
            handle = "@cinephile",
            content = "一部好的电影，就像一场完美的人生梦境。你最喜欢哪部电影？",
            timestamp = "昨天",
            likeCount = 451,
            commentCount = 150
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .hazeSource(state = hazeState)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            // 顶部留出 TopBar 的空间
            item { 
                Spacer(modifier = Modifier.height(64.dp))
            }
            
            items(mockPosts) { post ->
                PostCard(post = post)
            }
            
            // 底部留出 BottomNavigation 的空间
            item { 
                Spacer(modifier = Modifier.height(80.dp)) 
            }
        }
    }
}
