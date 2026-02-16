package com.example.laisheng.ui.features.mine

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.composables.icons.lucide.*
import com.example.laisheng.data.remote.NetworkModule
import com.example.laisheng.data.model.FollowCounts
import com.example.laisheng.data.model.User
import com.example.laisheng.data.model.CollectionFolder
import com.example.laisheng.ui.components.PostCard
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.hazeSource

import com.example.laisheng.ui.MainViewModel
import com.example.laisheng.ui.theme.Dimens

import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi

import com.example.laisheng.ui.components.LaishengLoading

@OptIn(ExperimentalMaterial3Api::class, ExperimentalHazeMaterialsApi::class)
@Composable
fun MineScreen(
    hazeState: HazeState,
    userId: String,
    paddingValues: PaddingValues,
    onFollowClick: (String, String, String) -> Unit,
    onEditClick: (String, String, String, String?, String?, String?) -> Unit,
    onMomentClick: (String) -> Unit,
    onSettingsClick: () -> Unit = {},
    viewModel: MineViewModel = viewModel(),
    mainViewModel: MainViewModel // Add MainViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    
    // Theme state
    val themeMode by mainViewModel.themeMode.collectAsState()
    
    // Folder state
    val selectedFolderId by viewModel.selectedFolderId.collectAsState()
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var showMoveDialog by remember { mutableStateOf(false) }
    var longPressedMomentId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) viewModel.loadData(userId)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.loadData(userId) },
            modifier = Modifier.fillMaxSize()
        ) {
            when (val state = uiState) {
                is MineUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        LaishengLoading(strokeWidth = 2.dp)
                    }
                }
                is MineUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().hazeSource(state = hazeState),
                        contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding())
                    ) {
                        item {
                            ProfileHeader(
                                user = state.user, 
                                followCounts = state.followCounts,
                                mutualCount = state.mutualFriends.size,
                                onStatClick = { type, title -> onFollowClick(userId, title, type) },
                                onEditClick = {
                                    onEditClick(state.user.nickname, state.user.handle, state.user.bio ?: "", state.user.avatar, state.user.bgImage, state.user.handleLastUpdatedAt)
                                }
                            )
                        }

                        item {
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                shadowElevation = 0.dp, // Remove shadow for cleaner look
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp) // Add spacing
                            ) {
                                TabRow(
                                    selectedTabIndex = selectedTab.ordinal,
                                    containerColor = Color.Transparent,
                                    contentColor = MaterialTheme.colorScheme.primary,
                                    divider = {},
                                    indicator = { tabPositions ->
                                        if (selectedTab.ordinal < tabPositions.size) {
                                            val currentTab = tabPositions[selectedTab.ordinal]
                                            val indicatorWidth = 16.dp
                                            
                                            // 居中短横条指示器
                                            Box(
                                                Modifier
                                                    .tabIndicatorOffset(currentTab)
                                                    .wrapContentSize(Alignment.BottomCenter)
                                                    .width(indicatorWidth)
                                                    .height(3.dp)
                                                    .background(
                                                        color = MaterialTheme.colorScheme.primary,
                                                        shape = RoundedCornerShape(3.dp)
                                                    )
                                            )
                                        }
                                    }
                                ) {
                                    MineTab.entries.forEach { tab ->
                                        val isSelected = selectedTab == tab
                                        Tab(
                                            selected = isSelected,
                                            onClick = { viewModel.selectTab(tab) },
                                            text = {
                                                Text(
                                                    text = when(tab) {
                                                        MineTab.MOMENTS -> "动态"
                                                        MineTab.LIKED -> "赞过"
                                                        MineTab.COLLECTED -> "收藏"
                                                    },
                                                    style = if (isSelected) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleSmall,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            },
                                            selectedContentColor = MaterialTheme.colorScheme.primary,
                                            unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        if (selectedTab == MineTab.COLLECTED) {
                            item {
                                FolderList(
                                    folders = (state as? MineUiState.Success)?.folders ?: emptyList(),
                                    selectedFolderId = selectedFolderId,
                                    onFolderClick = { viewModel.selectFolder(it) },
                                    onCreateClick = { showCreateFolderDialog = true },
                                    onDeleteFolder = { viewModel.deleteFolder(it) }
                                )
                            }
                        }

                        val currentList = when(selectedTab) {
                            MineTab.MOMENTS -> state.moments
                            MineTab.LIKED -> state.likedMoments
                            MineTab.COLLECTED -> state.collectedMoments
                        }

                        if (currentList.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillParentMaxHeight(0.5f)
                                        .fillMaxWidth()
                                        .padding(top = 48.dp), 
                                    contentAlignment = Alignment.TopCenter
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Lucide.Inbox, 
                                            contentDescription = null, 
                                            modifier = Modifier.size(64.dp), 
                                            tint = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "这里什么都没有", 
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "去发点动态或者关注有趣的人吧", 
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                            }
                        } else {
                            itemsIndexed(currentList) { _, moment ->
                                PostCard(
                                    moment = moment,
                                    onCardClick = { onMomentClick(moment.id) },
                                    onLikeClick = { viewModel.toggleLike(moment.id, userId) },
                                    onBookmarkClick = { viewModel.toggleCollect(moment.id, userId) },
                                    onCommentClick = { onMomentClick(moment.id) },
                                    onLongClick = {
                                        if (selectedTab == MineTab.COLLECTED) {
                                            longPressedMomentId = moment.id
                                            showMoveDialog = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
                is MineUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        // 顶栏：样式完全对齐“探索”页面，且具有上下一致的模糊感
        MineTopBar(
            hazeState = hazeState, 
            onSettingsClick = onSettingsClick,
            themeMode = themeMode,
            onThemeToggle = {
                // Toggle between Light (1) and Dark (2). Default/System(0) treats as auto. 
                // Simple logic: if currently Dark (2) then Light(1), else Dark(2).
                // If System (0), we check system state but simple toggle forces manual mode. 
                // Let's say: if mode is 2 -> 1, else -> 2.
                val newMode = if (themeMode == 2) 1 else 2
                mainViewModel.setThemeMode(newMode)
            }
        )
        
        if (showCreateFolderDialog) {
            CreateFolderDialog(
                onDismiss = { showCreateFolderDialog = false },
                onConfirm = { name -> 
                    viewModel.createFolder(name)
                    showCreateFolderDialog = false
                }
            )
        }

        if (showMoveDialog && longPressedMomentId != null) {
            MoveToFolderDialog(
                folders = (uiState as? MineUiState.Success)?.folders ?: emptyList(),
                onDismiss = { showMoveDialog = false; longPressedMomentId = null },
                onSelectFolder = { folderId ->
                    viewModel.moveCollectionToFolder(longPressedMomentId!!, userId, folderId)
                    showMoveDialog = false
                    longPressedMomentId = null
                }
            )
        }
    }
}

@Composable
fun MineTopBar(
    hazeState: HazeState, 
    onSettingsClick: () -> Unit,
    themeMode: Int,
    onThemeToggle: () -> Unit
) {
    // ... icon logic ...
    
    val backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
    val dividerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    val contentColor = MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .hazeEffect(state = hazeState, style = HazeMaterials.ultraThin())
            .background(backgroundColor) 
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(56.dp)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onThemeToggle) {
                    Icon(
                        imageVector = if (themeMode == 2) Lucide.Sun else Lucide.Moon, 
                        contentDescription = "切换主题", 
                        modifier = Modifier.size(22.dp),
                        tint = contentColor
                    )
                }
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        imageVector = Lucide.Settings, 
                        contentDescription = null, 
                        modifier = Modifier.size(22.dp),
                        tint = contentColor
                    )
                }
            }
        }
        HorizontalDivider(thickness = 0.5.dp, color = dividerColor)
    }
}

@Composable
fun ProfileHeader(
    user: User, 
    followCounts: FollowCounts,
    mutualCount: Int,
    onStatClick: (String, String) -> Unit,
    onEditClick: () -> Unit
) {
    val context = LocalContext.current
    
    Column(modifier = Modifier.fillMaxWidth()) {
        // 封面高度调整为 240.dp (稍微减小，更紧凑)
        Box(modifier = Modifier.fillMaxWidth().height(240.dp)) {
            AsyncImage(
                model = NetworkModule.formatUrl(user.bgImage) ?: "https://picsum.photos/1000/500",
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // 渐变遮罩，增强文字可读性
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-40).dp)
                .padding(horizontal = 20.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 头像 (带边框和阴影)
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(4.dp, MaterialTheme.colorScheme.surface),
                        shadowElevation = 4.dp,
                        modifier = Modifier.size(90.dp)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(NetworkModule.formatUrl(user.avatar))
                                .decoderFactory(SvgDecoder.Factory())
                                .crossfade(true)
                                .build(),
                            contentDescription = "Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // 关注/粉丝数据 (移到头像右侧，更紧凑)
                    Row(
                        modifier = Modifier.padding(bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatItem(count = followCounts.followingCount, label = "关注") { onStatClick("following", "关注") }
                        Spacer(modifier = Modifier.width(24.dp))
                        StatItem(count = followCounts.followersCount, label = "粉丝") { onStatClick("followers", "粉丝") }
                        Spacer(modifier = Modifier.width(24.dp))
                        StatItem(count = mutualCount, label = "好友") { onStatClick("mutual", "好友") }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 昵称与编辑按钮
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user.nickname, 
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 24.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (!user.handle.isNullOrBlank()) {
                            Text(
                                text = "@${user.handle.replace("@", "")}", 
                                style = MaterialTheme.typography.bodyMedium, 
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                    
                    Button(
                        onClick = onEditClick,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("编辑资料", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Bio
                Text(
                    text = user.bio ?: "还没有写个性签名哦", 
                    style = MaterialTheme.typography.bodyMedium, 
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun StatItem(count: Int, label: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier.clickable(
            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
            indication = null, 
            onClick = onClick
        ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(), 
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold, 
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label, 
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.outline
        )
    }
}
