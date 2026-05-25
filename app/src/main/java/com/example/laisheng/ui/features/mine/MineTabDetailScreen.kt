package com.example.laisheng.ui.features.mine

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.laisheng.data.model.Moment
import com.example.laisheng.ui.components.LaishengLoading
import com.example.laisheng.ui.components.PostCard
import com.example.laisheng.ui.theme.AppIcon
import com.example.laisheng.ui.theme.AppIcons
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.HazeMaterials

@Composable
fun MineTabDetailScreen(
    hazeState: HazeState,
    userId: String,
    title: String,
    tab: MineTab,
    onBack: () -> Unit,
    onMomentClick: (String) -> Unit,
    onUserClick: (String) -> Unit = {},
    viewModel: MineViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedFolderId by viewModel.selectedFolderId.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showCreateDialog by remember { mutableStateOf(false) }
    var movingMoment by remember { mutableStateOf<Moment?>(null) }
    var keyword by remember { mutableStateOf("") }

    LaunchedEffect(userId, tab) {
        if (userId.isNotEmpty()) {
            viewModel.selectTab(tab)
            viewModel.loadData(userId)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.message.collect { snackbarHostState.showSnackbar(it) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        when (val state = uiState) {
            MineUiState.Loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LaishengLoading(strokeWidth = 2.dp)
            }

            is MineUiState.Error -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.message, color = MaterialTheme.colorScheme.error)
            }

            is MineUiState.Success -> {
                val sourceMoments = when (tab) {
                    MineTab.MOMENTS -> state.moments
                    MineTab.LIKED -> state.likedMoments
                    MineTab.COLLECTED -> state.collectedMoments
                }
                val moments = sourceMoments.filter { moment ->
                    keyword.isBlank() ||
                        moment.content.text.orEmpty().contains(keyword, ignoreCase = true) ||
                        moment.nickname.orEmpty().contains(keyword, ignoreCase = true) ||
                        moment.handle.orEmpty().contains(keyword, ignoreCase = true)
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .hazeSource(state = hazeState),
                    contentPadding = PaddingValues(top = 104.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = keyword,
                            onValueChange = { keyword = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            placeholder = { Text("搜索内容或用户") },
                            singleLine = true,
                            shape = RoundedCornerShape(22.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                            )
                        )
                    }

                    if (tab == MineTab.COLLECTED) {
                        item {
                            FolderList(
                                folders = state.folders,
                                selectedFolderId = selectedFolderId,
                                onFolderClick = { viewModel.selectFolder(it) },
                                onCreateClick = { showCreateDialog = true },
                                onDeleteFolder = { viewModel.deleteFolder(it) }
                            )
                        }
                    }

                    if (sourceMoments.isEmpty()) {
                        item {
                            EmptyTabState(tab = tab, message = emptyMessage(tab))
                        }
                    } else if (moments.isEmpty()) {
                        item {
                            EmptyTabState(tab = tab, message = "没有匹配结果")
                        }
                    } else {
                        items(moments, key = { it.id }) { moment ->
                            PostCard(
                                moment = moment,
                                onCardClick = { onMomentClick(moment.id) },
                                onUserClick = { onUserClick(moment.userId) },
                                onCommentClick = { onMomentClick(moment.id) },
                                onBookmarkClick = {
                                    if (tab == MineTab.COLLECTED) {
                                        movingMoment = moment
                                    } else {
                                        viewModel.toggleCollect(moment.id, userId)
                                    }
                                },
                                onLikeClick = { viewModel.toggleLike(moment.id, userId) }
                            )
                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            )
                        }
                    }
                }

                if (showCreateDialog) {
                    CreateFolderDialog(
                        onDismiss = { showCreateDialog = false },
                        onConfirm = {
                            viewModel.createFolder(it)
                            showCreateDialog = false
                        }
                    )
                }

                if (movingMoment != null) {
                    MoveToFolderDialog(
                        folders = state.folders,
                        onDismiss = { movingMoment = null },
                        onSelectFolder = { folderId ->
                            viewModel.moveCollectionToFolder(movingMoment!!.id, userId, folderId)
                            movingMoment = null
                        }
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )

        MineDetailTopBar(hazeState = hazeState, title = title, onBack = onBack)
    }
}

@Composable
private fun MineDetailTopBar(
    hazeState: HazeState,
    title: String,
    onBack: () -> Unit
) {
    val backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
    val dividerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .hazeEffect(state = hazeState, style = HazeMaterials.ultraThin())
            .background(backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            ) {
                Box(
                    modifier = Modifier
                        .clickable(onClick = onBack)
                        .padding(10.dp)
                ) {
                    AppIcon(glyph = AppIcons.ArrowLeft, tint = MaterialTheme.colorScheme.onSurface, size = 20.dp)
                }
            }
            Text(
                text = title,
                modifier = Modifier.padding(start = 14.dp),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold)
            )
        }
        HorizontalDivider(thickness = 0.5.dp, color = dividerColor)
    }
}

@Composable
private fun EmptyTabState(tab: MineTab, message: String) {
    val subMessage = when (tab) {
        MineTab.COLLECTED -> "可以新建收藏夹，或者把收藏移动到别的分类。"
        else -> "内容出现后会显示在这里。"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 120.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = subMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun emptyMessage(tab: MineTab): String = when (tab) {
    MineTab.MOMENTS -> "还没有发布内容"
    MineTab.LIKED -> "还没有喜欢的内容"
    MineTab.COLLECTED -> "当前收藏夹里还没有内容"
}
