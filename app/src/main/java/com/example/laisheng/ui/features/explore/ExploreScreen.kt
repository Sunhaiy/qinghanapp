package com.example.laisheng.ui.features.explore

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

import com.example.laisheng.ui.components.PostCard
import com.example.laisheng.ui.features.mine.MoveToFolderDialog
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun ExploreScreen(
    hazeState: HazeState,
    paddingValues: PaddingValues,
    userId: String,
    onMomentClick: (String) -> Unit,
    onUserClick: (String) -> Unit, // Add callback
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    viewModel: ExploreViewModel = viewModel()
) {
    val moments by viewModel.moments.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()

    val folders by viewModel.folders.collectAsState()
    var showCollectionDialog by remember { mutableStateOf<String?>(null) } // momentId or null

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            viewModel.refresh(userId)
            viewModel.loadFolders(userId)
        }
    }

    // Snackbar state
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Collect Snackbar events
    LaunchedEffect(viewModel) {
        viewModel.snackbarEvent.collect { event ->
            val result = snackbarHostState.showSnackbar(
                message = event.message,
                actionLabel = event.actionLabel,
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                event.onAction?.invoke()
            }
        }
    }

    Scaffold(
        snackbarHost = { 
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding())
            ) 
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
                itemsIndexed(moments) { index, moment ->
                    with(sharedTransitionScope) {
                        PostCard(
                            moment = moment,
                            modifier = Modifier.sharedElement(
                                rememberSharedContentState(key = "item-${moment.id}"),
                                animatedVisibilityScope = animatedVisibilityScope
                            ),
                            onCardClick = { onMomentClick(moment.id) },
                            onUserClick = { onUserClick(moment.userId) }, // Wire up
                            onLikeClick = {
                                viewModel.onLikeClick(userId, moment.id)
                            },
                            onCommentClick = { onMomentClick(moment.id) }, // 点击评论按钮也跳转到详情页
                            onBookmarkClick = {
                                viewModel.onBookmarkClick(userId, moment.id) {
                                     showCollectionDialog = moment.id
                                }
                            }
                        )
                    }
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                    
                    if (index >= moments.size - 2) {
                        LaunchedEffect(moments.size) {
                            viewModel.loadNextPage(userId)
                        }
                    }
                }

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

        if (showCollectionDialog != null) {
            MoveToFolderDialog(
                folders = folders,
                onDismiss = { showCollectionDialog = null },
                onSelectFolder = { folderId ->
                    viewModel.confirmCollection(userId, showCollectionDialog!!, folderId)
                    showCollectionDialog = null
                }
            )
        }
    }
}
}