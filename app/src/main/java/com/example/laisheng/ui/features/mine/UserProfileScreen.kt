package com.example.laisheng.ui.features.mine

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.laisheng.data.model.FollowCounts
import com.example.laisheng.data.model.User
import com.example.laisheng.data.remote.NetworkModule
import com.example.laisheng.ui.components.LaishengLoading
import com.example.laisheng.ui.components.PostCard
import com.example.laisheng.ui.components.UserAvatar
import com.example.laisheng.ui.features.mine.MoveToFolderDialog
import com.example.laisheng.ui.theme.AppIcon
import com.example.laisheng.ui.theme.AppIcons
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.HazeMaterials

@Composable
fun UserProfileScreen(
    userId: String,
    currentUserId: String,
    onBack: () -> Unit,
    onChatClick: (String, String, String?) -> Unit,
    onMomentClick: (String) -> Unit,
    onFollowClick: (String, String, String) -> Unit,
    viewModel: UserProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val folders by viewModel.folders.collectAsState()
    val hazeState = remember { HazeState() }
    val listState = rememberLazyListState()
    var movingMomentId by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val collapsed = remember(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 96
    }

    LaunchedEffect(userId, currentUserId) {
        viewModel.loadProfile(userId, currentUserId)
    }

    LaunchedEffect(viewModel) {
        viewModel.snackbarEvent.collect { event ->
            val result = snackbarHostState.showSnackbar(
                message = event.message,
                actionLabel = event.actionLabel,
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                movingMomentId = event.momentId
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets(0.dp)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            when (val state = uiState) {
                UserProfileUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        LaishengLoading()
                    }
                }

                is UserProfileUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                    }
                }

                is UserProfileUiState.Success -> {
                    val relationLabel = when {
                        state.relation.isMutual -> "互相关注"
                        state.relation.isFollowedBy -> "对方关注了你"
                        state.relation.isFollowing -> "已关注"
                        else -> "未关注"
                    }
                    val followButtonLabel = when {
                        state.relation.isMutual -> "已互关"
                        state.relation.isFollowing -> "已关注"
                        state.relation.isFollowedBy -> "回关"
                        else -> "关注"
                    }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .hazeSource(state = hazeState),
                        contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding() + 24.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        item {
                            VisitorProfileHeader(
                                user = state.user,
                                followCounts = state.followCounts,
                                relationLabel = relationLabel,
                                followButtonLabel = followButtonLabel,
                                onChatClick = { onChatClick(state.user.id, state.user.nickname, state.user.avatar) },
                                onFollowToggle = { viewModel.toggleFollow(state.user.id, currentUserId) },
                                onFollowersClick = { onFollowClick(state.user.id, "粉丝", "followers") },
                                onFollowingClick = { onFollowClick(state.user.id, "关注", "following") }
                            )
                        }

                        item {
                            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                                Text(
                                    text = "个人瞬间",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "查看 ${state.moments.size} 条公开动态",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (state.moments.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "这个用户还没有公开动态",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            items(state.moments, key = { it.id }) { moment ->
                                PostCard(
                                    moment = moment,
                                    onCardClick = { onMomentClick(moment.id) },
                                    onUserClick = {},
                                    onLikeClick = { viewModel.onLikeClick(currentUserId, moment.id) },
                                    onBookmarkClick = {
                                        viewModel.onBookmarkClick(currentUserId, moment.id)
                                    },
                                    onCommentClick = { onMomentClick(moment.id) }
                                )
                                HorizontalDivider(
                                    thickness = 0.5.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                                )
                            }
                        }
                    }

                    if (movingMomentId != null) {
                        MoveToFolderDialog(
                            folders = folders,
                            onDismiss = { movingMomentId = null },
                            onSelectFolder = { folderId ->
                                viewModel.confirmCollection(currentUserId, movingMomentId!!, folderId)
                                movingMomentId = null
                            }
                        )
                    }

                    VisitorTopBar(
                        hazeState = hazeState,
                        collapsed = collapsed,
                        title = state.user.nickname,
                        onBack = onBack
                    )
                    SnackbarHost(
                        hostState = snackbarHostState,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun VisitorTopBar(
    hazeState: HazeState,
    collapsed: Boolean,
    title: String,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .hazeEffect(state = hazeState, style = HazeMaterials.ultraThin())
            .background(MaterialTheme.colorScheme.surface.copy(alpha = if (collapsed) 0.92f else 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(56.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                onClick = onBack,
                shape = CircleShape,
                color = if (collapsed) MaterialTheme.colorScheme.surfaceVariant else Color.White.copy(alpha = 0.18f)
            ) {
                Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                    AppIcon(
                        glyph = AppIcons.ArrowLeft,
                        tint = if (collapsed) MaterialTheme.colorScheme.onSurface else Color.White,
                        size = 20.dp
                    )
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.alpha(if (collapsed) 1f else 0f)
            )
            Surface(
                shape = CircleShape,
                color = if (collapsed) MaterialTheme.colorScheme.surfaceVariant else Color.White.copy(alpha = 0.18f)
            ) {
                Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                    AppIcon(
                        glyph = AppIcons.More,
                        tint = if (collapsed) MaterialTheme.colorScheme.onSurface else Color.White,
                        size = 18.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun VisitorProfileHeader(
    user: User,
    followCounts: FollowCounts,
    relationLabel: String,
    followButtonLabel: String,
    onChatClick: () -> Unit,
    onFollowToggle: () -> Unit,
    onFollowersClick: () -> Unit,
    onFollowingClick: () -> Unit
) {
    val context = LocalContext.current
    val bgUrl = remember(user.bgImage) { NetworkModule.formatUrl(user.bgImage) }
    val cardColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)

    Box(modifier = Modifier.fillMaxWidth()) {
        if (!bgUrl.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(context).data(bgUrl).crossfade(true).build(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(248.dp),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(248.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.14f),
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                            )
                        )
                    )
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(248.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.08f),
                            Color.Transparent,
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
                        )
                    )
                )
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 146.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor)
        ) {
            Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 54.dp, bottom = 22.dp)) {
                Text(
                    text = user.nickname,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (user.handle.startsWith("@")) user.handle else "@${user.handle}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                if (!user.bio.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = user.bio,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    VisitorMetaChip(user.ipLocation ?: "未知地区")
                    VisitorMetaChip(relationLabel)
                }
                Spacer(modifier = Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onFollowToggle,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Text(followButtonLabel)
                    }
                    Button(
                        onClick = onChatClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        AppIcon(glyph = AppIcons.Mail, tint = MaterialTheme.colorScheme.onSecondaryContainer, size = 16.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("发消息")
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    VisitorStat("粉丝", followCounts.followersCount.toString(), onFollowersClick)
                    VisitorStat("关注", followCounts.followingCount.toString(), onFollowingClick)
                    VisitorStat("关系", relationLabel, {})
                }
            }
        }

        UserAvatar(
            avatar = user.avatar,
            modifier = Modifier
                .padding(start = 34.dp, top = 110.dp)
                .size(86.dp)
                .background(MaterialTheme.colorScheme.surface, CircleShape)
                .padding(4.dp)
        )
    }
}

@Composable
private fun VisitorMetaChip(text: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.78f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun VisitorStat(label: String, value: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
