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
    onFollowClick: (String, String, String) -> Unit,
    onEditClick: (String, String, String, String?, String?) -> Unit,
    onMomentClick: (String) -> Unit, // 新增：点击动态的回调
    viewModel: MineViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()

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

                        item {
                            SecondaryTabRow(
                                selectedTabIndex = selectedTab.ordinal,
                                containerColor = Color.Transparent,
                                divider = {}
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
                                                fontSize = 15.sp,
                                                fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    )
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
                                Box(
                                    modifier = Modifier.fillParentMaxHeight(0.5f).fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("这里空空如也~", color = Color.Gray)
                                }
                            }
                        } else {
                            itemsIndexed(currentList) { index, moment ->
                                PostCard(
                                    moment = moment,
                                    onCardClick = { onMomentClick(moment.id) }, // 接通点击跳转
                                    onCommentClick = { onMomentClick(moment.id) } // 点击评论按钮也跳转
                                )
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
fun ProfileHeader(
    user: User, 
    followCounts: FollowCounts,
    mutualCount: Int,
    onStatClick: (String, String) -> Unit,
    onEditClick: () -> Unit
) {
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
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f))))
            )
            
            SmallFloatingActionButton(
                onClick = onEditClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 48.dp, end = 16.dp),
                containerColor = Color.White.copy(alpha = 0.7f),
                contentColor = Color.Black
            ) {
                Text("编辑", fontSize = 12.sp)
            }
        }

        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            Column {
                Spacer(modifier = Modifier.height(40.dp))
                Text(text = user.nickname, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(text = user.handle, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                
                Row(modifier = Modifier.padding(vertical = 12.dp)) {
                    StatItem(count = followCounts.followingCount, label = "关注") { onStatClick("following", "我的关注") }
                    Spacer(modifier = Modifier.width(20.dp))
                    StatItem(count = followCounts.followersCount, label = "粉丝") { onStatClick("followers", "我的粉丝") }
                    Spacer(modifier = Modifier.width(20.dp))
                    StatItem(count = mutualCount, label = "好友") { onStatClick("mutual", "互关好友") }
                }

                Text(text = user.bio ?: "这个用户很懒，什么都没写", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))
            }

            Surface(
                modifier = Modifier.offset(y = (-50).dp).size(90.dp).border(3.dp, Color.White, CircleShape),
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
        HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.3f))
    }
}

@Composable
fun StatItem(count: Int, label: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(text = count.toString(), fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, color = Color.Gray, fontSize = 14.sp)
    }
}
