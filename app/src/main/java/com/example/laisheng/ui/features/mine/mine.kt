package com.example.laisheng.ui.features.mine

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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
import com.example.laisheng.data.NetworkModule
import com.example.laisheng.data.model.FollowCounts
import com.example.laisheng.data.model.User
import com.example.laisheng.ui.composes.PostCard
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.hazeSource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MineScreen(
    hazeState: HazeState,
    userId: String,
    paddingValues: PaddingValues,
    onFollowClick: (String, String, String) -> Unit,
    onEditClick: (String, String, String, String?, String?) -> Unit,
    onMomentClick: (String) -> Unit,
    onSettingsClick: () -> Unit = {},
    viewModel: MineViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()

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
                        CircularProgressIndicator(strokeWidth = 2.dp)
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
                                    onEditClick(state.user.nickname, state.user.handle, state.user.bio ?: "", state.user.avatar, state.user.bgImage)
                                }
                            )
                        }

                        stickyHeader {
                            Surface(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                SecondaryTabRow(
                                    selectedTabIndex = selectedTab.ordinal,
                                    containerColor = Color.Transparent,
                                    divider = { HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)) }
                                ) {
                                    MineTab.entries.forEach { tab ->
                                        Tab(
                                            selected = selectedTab == tab,
                                            onClick = { viewModel.selectTab(tab) },
                                            text = {
                                                Text(
                                                    text = when(tab) {
                                                        MineTab.MOMENTS -> "动态"
                                                        MineTab.LIKED -> "赞过"
                                                        MineTab.COLLECTED -> "收藏"
                                                    },
                                                    fontSize = 14.sp,
                                                    fontWeight = if (selectedTab == tab) FontWeight.ExtraBold else FontWeight.Medium
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        val currentList = when(selectedTab) {
                            MineTab.MOMENTS -> state.moments
                            MineTab.LIKED -> state.likedMoments
                            MineTab.COLLECTED -> state.collectedMoments
                        }

                        if (currentList.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillParentMaxHeight(0.4f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Lucide.Inbox, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("暂无内容", color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
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
                                    onCommentClick = { onMomentClick(moment.id) }
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
        MineTopBar(hazeState, onSettingsClick)
    }
}

@Composable
fun MineTopBar(hazeState: HazeState, onSettingsClick: () -> Unit) {
    var isDarkMode by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .hazeEffect(state = hazeState, style = HazeMaterials.ultraThin())
            .background(Color.White.copy(alpha = 0.8f)) // 与 BottomNavigation 和 TopBar 保持相同的透明度
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(56.dp)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            // 设置和夜间按钮样式：去掉了圆底，直接使用标准 IconButton 样式与探索页面对齐
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { isDarkMode = !isDarkMode }) {
                    Icon(
                        imageVector = if (isDarkMode) Lucide.Sun else Lucide.Moon, 
                        contentDescription = null, 
                        modifier = Modifier.size(22.dp)
                    )
                }
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        imageVector = Lucide.Settings, 
                        contentDescription = null, 
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
        HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
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
        // 封面高度调整为 280.dp
        Box(modifier = Modifier.fillMaxWidth().height(280.dp)) {
            AsyncImage(
                model = NetworkModule.formatUrl(user.bgImage) ?: "https://picsum.photos/1000/500",
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.3f)))))
        }

        Box(modifier = Modifier.padding(horizontal = 20.dp)) {
            Column {
                Spacer(modifier = Modifier.height(55.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = user.nickname, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 22.sp), modifier = Modifier.weight(1f))
                    OutlinedButton(
                        onClick = onEditClick, 
                        shape = RoundedCornerShape(12.dp), 
                        modifier = Modifier.height(36.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        Text("编辑资料", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
                // 修复：严谨处理 handle，防止出现两个 @
                Text(
                    text = "@${user.handle?.replace("@", "") ?: ""}", 
                    style = MaterialTheme.typography.bodyMedium, 
                    color = MaterialTheme.colorScheme.outline
                )
                
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = user.bio ?: "还没有写个性签名哦", 
                    style = MaterialTheme.typography.bodyMedium, 
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )

                Row(modifier = Modifier.padding(vertical = 18.dp)) {
                    StatItem(count = followCounts.followingCount, label = "关注") { onStatClick("following", "关注") }
                    Spacer(modifier = Modifier.width(28.dp))
                    StatItem(count = followCounts.followersCount, label = "粉丝") { onStatClick("followers", "粉丝") }
                    Spacer(modifier = Modifier.width(28.dp))
                    StatItem(count = mutualCount, label = "好友") { onStatClick("mutual", "好友") }
                }
            }

            // 头像位置优化
            Surface(
                modifier = Modifier.offset(y = (-50).dp).size(100.dp).border(4.dp, MaterialTheme.colorScheme.surface, CircleShape),
                shape = CircleShape,
                tonalElevation = 4.dp
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context).data(NetworkModule.formatUrl(user.avatar)).decoderFactory(SvgDecoder.Factory()).crossfade(true).build(),
                    contentDescription = "Avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
fun StatItem(count: Int, label: String, onClick: () -> Unit) {
    Column(modifier = Modifier.clickable(interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }, indication = null, onClick = onClick)) {
        Text(text = count.toString(), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
        Text(text = label, color = MaterialTheme.colorScheme.outline, fontSize = 12.sp)
    }
}
