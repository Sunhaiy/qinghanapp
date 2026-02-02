package com.example.laisheng.ui.features.explore

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()

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
            onRefresh = { viewModel.refresh(userId) },
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = paddingValues
            ) {
                // 使用 itemsIndexed 来判断是否滑动到了最后一项
                itemsIndexed(moments) { index, moment ->
                    PostCard(
                        moment = moment,
                        onLikeClick = {
                            viewModel.onLikeClick(userId, moment.id)
                        },
                        onBookmarkClick = {
                            viewModel.onBookmarkClick(userId, moment.id)
                        }
                    )
                    
                    // 触发加载更多的判断逻辑：当显示到最后 2 条时就开始预加载下一页
                    if (index >= moments.size - 2) {
                        LaunchedEffect(moments.size) {
                            viewModel.loadNextPage(userId)
                        }
                    }
                }

                // 在列表底部显示加载中状态
                if (isLoadingMore) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
        }
    }
}