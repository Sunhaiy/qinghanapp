package com.example.laisheng.ui.features.mine

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.laisheng.data.model.User
import com.example.laisheng.data.remote.NetworkModule
import com.example.laisheng.ui.components.PostCard
import com.example.laisheng.ui.theme.Dimens
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MessageCircle
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.HazeMaterials

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    userId: String,
    currentUserId: String,
    onBack: () -> Unit,
    onChatClick: (String, String, String?) -> Unit,
    onMomentClick: (String) -> Unit,
    onFollowClick: (String, String, String) -> Unit, // userId, title, type
    viewModel: UserProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val hazeState = remember { HazeState() }
    
    // Scroll state for dynamic top bar
    val listState = rememberLazyListState()
    val density = LocalDensity.current
    val thresholdPx = with(density) { 200.dp.toPx() } // Start transitions after banner
    
    val scrollOffset by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex > 0) thresholdPx 
            else listState.firstVisibleItemScrollOffset.toFloat()
        }
    }
    
    // Calculate animation fraction (0f to 1f)
    val expandFraction by remember {
        derivedStateOf {
            (scrollOffset / thresholdPx).coerceIn(0f, 1f)
        }
    }
    
    val showTopBarContent by remember { derivedStateOf { expandFraction > 0.8f } }

    LaunchedEffect(userId) {
        viewModel.loadProfile(userId, currentUserId)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            when (val state = uiState) {
                is UserProfileUiState.Loading -> {
                     Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is UserProfileUiState.Success -> {
                    val user = state.user
                    
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize().hazeSource(state = hazeState),
                        contentPadding = PaddingValues(bottom = Dimens.PaddingLarge)
                    ) {
                        item {
                            ProfileHeaderSection(
                                user = user,
                                isMe = user.id == currentUserId,
                                followCounts = state.followCounts,
                                onFollowClick = { viewModel.toggleFollow(user.id, currentUserId) },
                                onChatClick = { onChatClick(user.id, user.nickname, user.avatar) },
                                onStatClick = { type, title -> onFollowClick(user.id, title, type) }
                            )
                        }
                        
                        item {
                            HorizontalDivider(
                                thickness = 8.dp, 
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        }

                        if (state.moments.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(Dimens.PaddingLarge),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("还没有任何动态", color = MaterialTheme.colorScheme.outline)
                                }
                            }
                        } else {
                            items(state.moments) { moment ->
                                PostCard(
                                    moment = moment,
                                    onCardClick = { onMomentClick(moment.id) },
                                    onUserClick = { /* Already on profile */ },
                                    onLikeClick = { viewModel.onLikeClick(currentUserId, moment.id) },
                                    onBookmarkClick = { viewModel.onBookmarkClick(currentUserId, moment.id) },
                                    onCommentClick = { onMomentClick(moment.id) }
                                )
                                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                            }
                        }
                    }
                    
                    // --- Dynamic Top Bar ---
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp) // Covers status bar + toolbar area roughly
                    ) {
                        // 1. Dynamic Background Layer (Fades in)
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .alpha(expandFraction)
                                .hazeEffect(state = hazeState, style = HazeMaterials.regular())
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)) 
                        )
                        
                        // 2. Content Layer (Always visible, items change color)
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .statusBarsPadding()
                                .padding(horizontal = 4.dp), // Adjust padding
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Back Button
                            IconButton(onClick = onBack) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack, 
                                    contentDescription = "返回",
                                    // Transition from White (on image) to OnSurface (on background)
                                    tint = androidx.compose.ui.graphics.lerp(
                                        Color.White, 
                                        MaterialTheme.colorScheme.onSurface, 
                                        expandFraction
                                    )
                                )
                            }
                            
                            // Title (Fades in)
                            Text(
                                text = user.nickname,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.alpha(expandFraction),
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // Right Actions
                            Row {
                                if (showTopBarContent) {
                                    IconButton(onClick = { /* TODO: More actions */ }) {
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = "更多",
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                } else {
                                     // Placeholder to balance the row if needed, or just empty
                                     Spacer(modifier = Modifier.size(48.dp))
                                }
                            }
                        }
                        
                        // Divider (Optional, fades in)
                        if (expandFraction > 0.9f) {
                             HorizontalDivider(
                                modifier = Modifier.align(Alignment.BottomCenter),
                                thickness = 0.5.dp, 
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
                is UserProfileUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error)
                         // Still show back button on error
                        IconButton(
                            onClick = onBack,
                             modifier = Modifier
                                .align(Alignment.TopStart)
                                .statusBarsPadding()
                                .padding(16.dp)
                                .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                        ) {
                             Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileHeaderSection(
    user: User,
    isMe: Boolean,
    followCounts: com.example.laisheng.data.model.FollowCounts,
    onFollowClick: () -> Unit,
    onChatClick: () -> Unit,
    onStatClick: (String, String) -> Unit // type, title
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // 1. Banner & Avatar Container
        Box(modifier = Modifier.fillMaxWidth()) {
            // Banner
            AsyncImage(
                model = NetworkModule.formatUrl(user.bgImage) ?: "https://picsum.photos/1000/500",
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )
            
            // Avatar (Overlapping)
            Box(
                modifier = Modifier
                    .padding(top = 160.dp, start = Dimens.PaddingLarge) // 200 - 40 (half avatar) = 160 start
                    .size(80.dp)
                    .clip(CircleShape)
                    .border(4.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                 AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(NetworkModule.formatUrl(user.avatar))
                        .decoderFactory(coil.decode.SvgDecoder.Factory())
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
        
        // 2. Info Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.PaddingLarge)
                .padding(top = Dimens.PaddingSmall) // Space after avatar area
        ) {
            // Actions (Right aligned, same row as Avatar roughly)
            if (!isMe) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-40).dp), // Move up to align with avatar bottom area
                    horizontalArrangement = Arrangement.End
                ) {
                     IconButton(
                        onClick = onChatClick,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)
                            .size(40.dp)
                            .border(1.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    ) {
                        Icon(Icons.Default.Email, contentDescription = "私信", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                    
                    Spacer(modifier = Modifier.width(Dimens.PaddingMedium))

                    Button(
                        onClick = onFollowClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (user.isFollowed == true) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
                            contentColor = if (user.isFollowed == true) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.surface, RoundedCornerShape(50))
                    ) {
                        Text(text = if (user.isFollowed == true) "已关注" else "关注")
                    }
                }
            } else {
                 // Placeholder to keep spacing if needed, or just spacer
                 Spacer(modifier = Modifier.height(10.dp))
            }

            // Name & Handle
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = user.nickname,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                // IP Badge
                user.ipLocation?.let { ip ->
                    Spacer(modifier = Modifier.width(Dimens.PaddingSmall))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = ip,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            if (user.handle.isNotEmpty()) {
                Text(
                    text = if (user.handle.startsWith("@")) user.handle else "@${user.handle}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            
            Spacer(modifier = Modifier.height(Dimens.PaddingMedium))
            
            // Bio
            if (!user.bio.isNullOrBlank()) {
                Text(
                    text = user.bio,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(Dimens.PaddingMedium))
            }
            
            // Stats
            Row(modifier = Modifier.fillMaxWidth()) {
                ProfileStatItem(count = followCounts.followersCount, label = "粉丝") { onStatClick("followers", "粉丝") }
                Spacer(modifier = Modifier.width(Dimens.PaddingLarge))
                ProfileStatItem(count = followCounts.followingCount, label = "关注") { onStatClick("following", "关注") }
            }
            
            Spacer(modifier = Modifier.height(Dimens.PaddingLarge))
        }
    }
}

@Composable
private fun ProfileStatItem(count: Int, label: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.clickable(
            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
    }
}
