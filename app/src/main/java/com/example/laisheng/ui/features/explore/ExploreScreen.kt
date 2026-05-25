package com.example.laisheng.ui.features.explore

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
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
    onUserClick: (String) -> Unit,
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
                showCollectionDialog = event.momentId
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
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0.dp)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = hazeState)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {


                // Default Feed
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
                                    onUserClick = { onUserClick(moment.userId) }, 
                                    onLikeClick = {
                                        viewModel.onLikeClick(userId, moment.id)
                                    },
                                    onCommentClick = { onMomentClick(moment.id) }, 
                                    onBookmarkClick = {
                                        viewModel.onBookmarkClick(userId, moment.id)
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
                                    com.example.laisheng.ui.components.LaishengLoading()
                                }
                            }
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

@Composable
fun EmptySearchState() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                androidx.compose.material.icons.Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("没有找到相关内容", color = MaterialTheme.colorScheme.outline)
        }
    }
}
