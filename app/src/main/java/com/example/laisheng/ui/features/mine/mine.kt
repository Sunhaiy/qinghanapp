package com.example.laisheng.ui.features.mine

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.laisheng.data.NetworkModule
import com.example.laisheng.data.model.FollowCounts
import com.example.laisheng.data.model.User
import com.example.laisheng.ui.composes.PostCard
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MineScreen(
    hazeState: HazeState,
    userId: String,
    paddingValues: PaddingValues,
    viewModel: MineViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            viewModel.loadData(userId)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .hazeSource(state = hazeState)
    ) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.loadData(userId) },
            modifier = Modifier.fillMaxSize()
        ) {
            when (val state = uiState) {
                is MineUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is MineUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = paddingValues
                    ) {
                        item {
                            ProfileHeader(state.user, state.followCounts)
                        }

                        item {
                            Text(
                                text = "我的动态",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(16.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        itemsIndexed(state.moments) { index, moment ->
                            PostCard(
                                moment = moment,
                                onLikeClick = { /* logic handled in Card if needed */ },
                                onBookmarkClick = { /* logic handled in Card if needed */ }
                            )
                            
                            if (index >= state.moments.size - 2) {
                                LaunchedEffect(state.moments.size) {
                                    viewModel.loadNextPage(userId)
                                }
                            }
                        }
                        
                        if (state.moments.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(64.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("还没有发布过动态哦", color = Color.Gray)
                                }
                            }
                        }
                    }
                }
                is MineUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message, color = Color.Red)
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileHeader(user: User, followCounts: FollowCounts) {
    val context = LocalContext.current
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
            AsyncImage(
                model = NetworkModule.formatUrl(user.bgImage) ?: "https://picsum.photos/1000/500",
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f))
                        )
                    )
            )
        }

        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            Column {
                Spacer(modifier = Modifier.height(40.dp))
                Text(
                    text = user.nickname,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = user.handle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                
                // 新增：关注和粉丝数显示
                Row(modifier = Modifier.padding(vertical = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = followCounts.followingCount.toString(), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "关注", color = Color.Gray, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = followCounts.followersCount.toString(), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "粉丝", color = Color.Gray, fontSize = 14.sp)
                    }
                }

                Text(
                    text = user.bio ?: "这个用户很懒，什么都没写",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Surface(
                modifier = Modifier
                    .offset(y = (-50).dp)
                    .size(100.dp)
                    .border(4.dp, Color.White, CircleShape),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(NetworkModule.formatUrl(user.avatar))
                        .decoderFactory(SvgDecoder.Factory())
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
        HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
    }
}
