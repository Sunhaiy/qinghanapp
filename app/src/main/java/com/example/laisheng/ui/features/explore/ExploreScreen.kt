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
            // 顶部留出 TopBar 的空间 (TopBar 高度大约是状态栏 + 文字高度)
            item { 
                Spacer(modifier = Modifier.height(60.dp)) 
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
