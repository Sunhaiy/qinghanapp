package com.example.laisheng.ui.features.explore

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.laisheng.ui.composes.PostCard
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    hazeState: HazeState,
    paddingValues: PaddingValues,
    userId: String,
    viewModel: ExploreViewModel = viewModel()
) {
    val moments by viewModel.moments.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    // 关键点 1: 当 userId 准备好时，带着 userId 刷新数据
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            viewModel.refresh(userId)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .hazeSource(state = hazeState)
    ) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            // 关键点 2: 下拉刷新时也要带着 userId
            onRefresh = { viewModel.refresh(userId) },
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = paddingValues
            ) {
                items(moments) { moment ->
                    PostCard(
                        moment = moment,
                        onLikeClick = {
                            viewModel.onLikeClick(userId, moment.id)
                        },
                        onBookmarkClick = {
                            viewModel.onBookmarkClick(userId, moment.id)
                        }
                    )
                }
            }
        }
    }
}