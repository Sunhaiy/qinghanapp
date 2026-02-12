package com.example.laisheng.ui.features.mine

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
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
import coil.request.ImageRequest
import com.example.laisheng.data.model.User
import com.example.laisheng.data.remote.NetworkModule
import com.example.laisheng.ui.components.PostCard
import com.example.laisheng.ui.theme.Dimens
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MessageCircle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    userId: String,
    currentUserId: String,
    onBack: () -> Unit,
    onChatClick: (String, String, String?) -> Unit,
    onMomentClick: (String) -> Unit,
    viewModel: UserProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(userId) {
        viewModel.loadProfile(userId, currentUserId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
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
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = Dimens.PaddingLarge)
                    ) {
                        item {
                            ProfileHeaderSection(
                                user = user,
                                isMe = user.id == currentUserId,
                                followCounts = state.followCounts,
                                onFollowClick = { viewModel.toggleFollow(user.id, currentUserId) },
                                onChatClick = { onChatClick(user.id, user.nickname, user.avatar) }
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
                                    onBookmarkClick = { viewModel.onBookmarkClick(currentUserId, moment.id) }
                                )
                                HorizontalDivider(thickness = Dimens.PaddingSmall, color = MaterialTheme.colorScheme.surfaceVariant)
                            }
                        }
                    }
                }
                is UserProfileUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error)
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
    followCounts: com.example.laisheng.data.model.FollowCounts, // Use fully qualified if needed or import
    onFollowClick: () -> Unit,
    onChatClick: () -> Unit
) {
    Column {
        Box(modifier = Modifier.fillMaxWidth().height(Dimens.BannerHeight)) {
            AsyncImage(
                model = NetworkModule.formatUrl(user.bgImage) ?: "https://picsum.photos/1000/500",
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, MaterialTheme.colorScheme.surface)))
            )
            
            // Avatar
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = Dimens.PaddingLarge, bottom = Dimens.PaddingMedium)
            ) {
                val context = LocalContext.current
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(NetworkModule.formatUrl(user.avatar))
                        .decoderFactory(coil.decode.SvgDecoder.Factory())
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop
                )
            }
        }
        
        Column(modifier = Modifier.padding(horizontal = Dimens.PaddingLarge)) {
            // Name and Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.nickname,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (user.handle.isNotEmpty()) {
                        Text(
                            text = if (user.handle.startsWith("@")) user.handle else "@${user.handle}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
                
                if (!isMe) {
                    Row {
                         IconButton(
                            onClick = onChatClick,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)
                                .size(40.dp)
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
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(text = if (user.isFollowed == true) "已关注" else "关注")
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(Dimens.PaddingMedium))
            
            // Bio
            if (!user.bio.isNullOrBlank()) {
                Text(
                    text = user.bio,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(Dimens.PaddingMedium))
            }
            
            // Stats
            Row(modifier = Modifier.fillMaxWidth()) {
                StatItem(count = followCounts.followersCount, label = "粉丝")
                Spacer(modifier = Modifier.width(Dimens.PaddingLarge))
                StatItem(count = followCounts.followingCount, label = "关注")
            }
            
            Spacer(modifier = Modifier.height(Dimens.PaddingLarge))
        }
    }
}

@Composable
fun StatItem(count: Int, label: String) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
    }
}
