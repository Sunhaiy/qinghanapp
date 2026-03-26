package com.example.laisheng.ui.features.mine

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.example.laisheng.data.model.Moment
import com.example.laisheng.data.model.User
import com.example.laisheng.data.remote.NetworkModule
import com.example.laisheng.ui.MainViewModel
import com.example.laisheng.ui.components.LaishengLoading
import com.example.laisheng.ui.components.UserAvatar
import com.example.laisheng.ui.theme.AppIcon
import com.example.laisheng.ui.theme.AppIcons
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.HazeMaterials

@Composable
fun MineScreen(
    hazeState: HazeState,
    userId: String,
    paddingValues: PaddingValues,
    onFollowClick: (String, String, String) -> Unit,
    onEditClick: (String, String, String, String?, String?, String?) -> Unit,
    onMomentClick: (String) -> Unit,
    onSettingsClick: () -> Unit = {},
    onOpenMoments: () -> Unit,
    onOpenLikes: () -> Unit,
    onOpenCollections: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenMembership: () -> Unit,
    viewModel: MineViewModel = viewModel(),
    mainViewModel: MainViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val themeMode by mainViewModel.themeMode.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) viewModel.loadData(userId)
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
            MineUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    LaishengLoading(strokeWidth = 2.dp)
                }
            }

            is MineUiState.Error -> {
                MineErrorState(message = state.message, onRetry = { viewModel.loadData(userId) })
            }

            is MineUiState.Success -> {
                Scaffold(
                    containerColor = Color.Transparent,
                    contentWindowInsets = WindowInsets(0.dp),
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .hazeSource(state = hazeState),
                        contentPadding = PaddingValues(
                            top = innerPadding.calculateTopPadding() + 104.dp,
                            bottom = paddingValues.calculateBottomPadding() + 28.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            MineHeroSection(
                                user = state.user,
                                onEditClick = {
                                    onEditClick(
                                        state.user.nickname,
                                        state.user.handle,
                                        state.user.bio.orEmpty(),
                                        state.user.avatar,
                                        state.user.bgImage,
                                        state.user.handleLastUpdatedAt
                                    )
                                }
                            )
                        }

                        item {
                            MineStatsSection(
                                publishedCount = state.moments.size,
                                followersCount = state.followCounts.followersCount,
                                followingCount = state.followCounts.followingCount,
                                collectionsCount = state.collectedMoments.size,
                                onFollowersClick = { onFollowClick(userId, "粉丝", "followers") },
                                onFollowingClick = { onFollowClick(userId, "关注", "following") }
                            )
                        }

                        item {
                            MinePrimaryActions(
                                momentsCount = state.moments.size,
                                likedCount = state.likedMoments.size,
                                collectedCount = state.collectedMoments.size,
                                onOpenMoments = onOpenMoments,
                                onOpenLikes = onOpenLikes,
                                onOpenCollections = onOpenCollections,
                                onEditClick = {
                                    onEditClick(
                                        state.user.nickname,
                                        state.user.handle,
                                        state.user.bio.orEmpty(),
                                        state.user.avatar,
                                        state.user.bgImage,
                                        state.user.handleLastUpdatedAt
                                    )
                                }
                            )
                        }

                        item {
                            MineFutureActions(
                                onOpenHistory = onOpenHistory,
                                onOpenMembership = onOpenMembership
                            )
                        }

                        item {
                            MinePreviewSection(
                                title = "最近发布",
                                moments = state.moments.take(3),
                                emptyText = "还没有发布内容",
                                onMomentClick = onMomentClick
                            )
                        }
                    }
                }

                MinePinnedTopBar(
                    hazeState = hazeState,
                    themeMode = themeMode,
                    onThemeToggle = {
                        val newMode = if (themeMode == 2) 1 else 2
                        mainViewModel.setThemeMode(newMode)
                    },
                    onSettingsClick = onSettingsClick
                )
            }
        }
    }
}

@Composable
private fun MinePinnedTopBar(
    hazeState: HazeState,
    themeMode: Int,
    onThemeToggle: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .hazeEffect(state = hazeState, style = HazeMaterials.ultraThin())
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.82f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "我的",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TopActionChip(
                    icon = if (themeMode == 2) AppIcons.Sun else AppIcons.Moon,
                    onClick = onThemeToggle
                )
                TopActionChip(icon = AppIcons.Settings, onClick = onSettingsClick)
            }
        }
        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    }
}

@Composable
private fun TopActionChip(icon: String, onClick: () -> Unit) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f)
    ) {
        Box(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            AppIcon(glyph = icon, tint = MaterialTheme.colorScheme.onSurface, size = 18.dp)
        }
    }
}

@Composable
private fun MineHeroSection(
    user: User,
    onEditClick: () -> Unit
) {
    val context = LocalContext.current
    val backgroundUrl = NetworkModule.formatUrl(user.bgImage)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp)
        ) {
            if (backgroundUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(backgroundUrl)
                        .decoderFactory(SvgDecoder.Factory())
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                DefaultTextureBackground()
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.12f),
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.42f),
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 22.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                UserAvatar(
                    avatar = user.avatar,
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(84.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = user.nickname,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 29.sp
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "@${user.handle.removePrefix("@")}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                if (!user.bio.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = user.bio,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    user.ipLocation?.takeIf { it.isNotBlank() }?.let { MetaPill(it) }
                    MetaPill("资料已同步")
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onEditClick,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onSurface,
                        contentColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    AppIcon(glyph = AppIcons.Edit, tint = MaterialTheme.colorScheme.surface, size = 16.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("编辑资料")
                }
            }
        }
    }
}

@Composable
private fun DefaultTextureBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFF3F0E9),
                        Color(0xFFE9EDF8),
                        Color(0xFFF5F4EF)
                    )
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stripeColor = Color.White.copy(alpha = 0.28f)
            var x = -size.height
            while (x < size.width * 1.5f) {
                drawLine(
                    color = stripeColor,
                    start = Offset(x, 0f),
                    end = Offset(x + size.height, size.height),
                    strokeWidth = 18f
                )
                x += 80f
            }
        }
    }
}

@Composable
private fun MineStatsSection(
    publishedCount: Int,
    followersCount: Int,
    followingCount: Int,
    collectionsCount: Int,
    onFollowersClick: () -> Unit,
    onFollowingClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatCell(value = publishedCount.toString(), label = "发布", modifier = Modifier.weight(1f))
            StatCell(value = followersCount.toString(), label = "粉丝", modifier = Modifier.weight(1f).clickable(onClick = onFollowersClick))
            StatCell(value = followingCount.toString(), label = "关注", modifier = Modifier.weight(1f).clickable(onClick = onFollowingClick))
            StatCell(value = collectionsCount.toString(), label = "收藏", modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatCell(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun MinePrimaryActions(
    momentsCount: Int,
    likedCount: Int,
    collectedCount: Int,
    onOpenMoments: () -> Unit,
    onOpenLikes: () -> Unit,
    onOpenCollections: () -> Unit,
    onEditClick: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("常用入口", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ActionCard(Modifier.weight(1f), "我的发布", momentsCount.toString(), AppIcons.List, onOpenMoments)
            ActionCard(Modifier.weight(1f), "我的喜欢", likedCount.toString(), AppIcons.Heart, onOpenLikes)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ActionCard(Modifier.weight(1f), "收藏夹", collectedCount.toString(), AppIcons.Bookmark, onOpenCollections)
            ActionCard(Modifier.weight(1f), "编辑资料", "管理", AppIcons.Edit, onEditClick)
        }
    }
}

@Composable
private fun MineFutureActions(
    onOpenHistory: () -> Unit,
    onOpenMembership: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("更多功能", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ActionCard(Modifier.weight(1f), "足迹", "记录", AppIcons.Explore, onOpenHistory)
            PlaceholderCard(Modifier.weight(1f), "桌面小组件", "预留", AppIcons.Home)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ActionCard(Modifier.weight(1f), "会员", "套餐", AppIcons.Star, onOpenMembership)
            PlaceholderCard(Modifier.weight(1f), "更多功能", "后续", AppIcons.More)
        }
    }
}

@Composable
private fun ActionCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)),
                contentAlignment = Alignment.Center
            ) {
                AppIcon(glyph = icon, tint = MaterialTheme.colorScheme.onSurface, size = 18.dp)
            }
            Spacer(modifier = Modifier.height(18.dp))
            Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PlaceholderCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                AppIcon(glyph = icon, tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 18.dp)
            }
            Spacer(modifier = Modifier.height(18.dp))
            Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun MinePreviewSection(
    title: String,
    moments: List<Moment>,
    emptyText: String,
    onMomentClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "点击内容可进入详情页。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(14.dp))

            if (moments.isEmpty()) {
                Text(emptyText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                moments.forEachIndexed { index, moment ->
                    PreviewRow(moment = moment, onClick = { onMomentClick(moment.id) })
                    if (index != moments.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PreviewRow(moment: Moment, onClick: () -> Unit) {
    val previewText = buildString {
        append(moment.content.text?.takeIf { it.isNotBlank() } ?: "暂无正文")
        if (!moment.content.attachments.isNullOrEmpty()) append(" · 含附件")
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(previewText, style = MaterialTheme.typography.bodyLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = listOfNotNull(moment.userIpLocation, moment.ipLocation).firstOrNull() ?: "点击查看详情",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        AppIcon(glyph = AppIcons.ArrowRight, tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 18.dp)
    }
}

@Composable
private fun MetaPill(text: String) {
    AssistChip(
        onClick = {},
        label = { Text(text) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.86f),
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = null
    )
}

@Composable
private fun MineErrorState(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("页面加载失败", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(8.dp))
            Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) { Text("重新加载") }
        }
    }
}
