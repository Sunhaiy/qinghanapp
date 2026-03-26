package com.example.laisheng.ui.features.home

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.laisheng.data.model.Moment
import com.example.laisheng.data.remote.NetworkModule
import com.example.laisheng.ui.components.LaishengLoading
import com.example.laisheng.ui.components.PostCard
import com.example.laisheng.ui.components.UserAvatar
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlin.math.absoluteValue

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HomeScreen(
    hazeState: HazeState,
    paddingValues: PaddingValues,
    userId: String,
    onMomentClick: (String) -> Unit,
    onUserClick: (String) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    viewModel: HomeViewModel = viewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val featuredMoments by viewModel.featuredMoments.collectAsState()
    val followingMoments by viewModel.followingMoments.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) viewModel.refresh()
    }

    Scaffold(
        topBar = {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                divider = {},
                indicator = { positions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(positions[selectedTab]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("灵感精选") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("关注动态") })
            }
        },
        contentWindowInsets = WindowInsets(0.dp)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .hazeSource(state = hazeState)
        ) {
            if (isRefreshing && featuredMoments.isEmpty() && followingMoments.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    LaishengLoading()
                }
            } else if (selectedTab == 0) {
                HomeFeaturedFeed(
                    featuredMoments = featuredMoments,
                    followingMoments = followingMoments,
                    bottomPadding = paddingValues.calculateBottomPadding(),
                    onMomentClick = onMomentClick,
                    onUserClick = onUserClick,
                    onLikeHint = {
                        Toast.makeText(context, "详情页里可以完成互动操作", Toast.LENGTH_SHORT).show()
                    },
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope
                )
            } else {
                FollowingFeed(
                    moments = followingMoments,
                    bottomPadding = paddingValues.calculateBottomPadding(),
                    onMomentClick = onMomentClick,
                    onUserClick = onUserClick,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun HomeFeaturedFeed(
    featuredMoments: List<Moment>,
    followingMoments: List<Moment>,
    bottomPadding: androidx.compose.ui.unit.Dp,
    onMomentClick: (String) -> Unit,
    onUserClick: (String) -> Unit,
    onLikeHint: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = bottomPadding + 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "今日灵感",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "横向滑动查看精选瞬间，像翻一组有氛围的故事卡片。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        item {
            if (featuredMoments.isEmpty()) {
                EmptyFeaturedState()
            } else {
                val pagerState = rememberPagerState(pageCount = { featuredMoments.size })
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    HorizontalPager(
                        state = pagerState,
                        contentPadding = PaddingValues(horizontal = 28.dp),
                        pageSpacing = 16.dp,
                        pageSize = PageSize.Fill,
                        modifier = Modifier.height(420.dp)
                    ) { page ->
                        val moment = featuredMoments[page]
                        val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
                        FeaturedMomentCard(
                            moment = moment,
                            modifier = Modifier.graphicsLayer {
                                alpha = androidx.compose.ui.util.lerp(0.72f, 1f, 1f - pageOffset.coerceIn(0f, 1f))
                                scaleY = androidx.compose.ui.util.lerp(0.92f, 1f, 1f - pageOffset.coerceIn(0f, 1f))
                                scaleX = androidx.compose.ui.util.lerp(0.96f, 1f, 1f - pageOffset.coerceIn(0f, 1f))
                            },
                            onMomentClick = { onMomentClick(moment.id) },
                            onUserClick = { onUserClick(moment.userId) }
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(featuredMoments.size.coerceAtMost(6)) { index ->
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .size(if (pagerState.currentPage == index) 20.dp else 6.dp, 6.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(
                                        if (pagerState.currentPage == index) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outlineVariant
                                    )
                            )
                        }
                    }
                }
            }
        }

        if (followingMoments.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "继续浏览",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "下面是来自你关注用户的最新动态。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            items(followingMoments.take(4)) { moment ->
                with(sharedTransitionScope) {
                    PostCard(
                        moment = moment,
                        modifier = Modifier.padding(horizontal = 4.dp),
                        onCardClick = { onMomentClick(moment.id) },
                        onUserClick = { onUserClick(moment.userId) },
                        onLikeClick = onLikeHint,
                        onCommentClick = { onMomentClick(moment.id) },
                        onBookmarkClick = onLikeHint
                    )
                }
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun FollowingFeed(
    moments: List<Moment>,
    bottomPadding: androidx.compose.ui.unit.Dp,
    onMomentClick: (String) -> Unit,
    onUserClick: (String) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = bottomPadding + 16.dp)
    ) {
        if (moments.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillParentMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "还没有关注动态，先去探索页逛逛吧。",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(moments) { moment ->
                with(sharedTransitionScope) {
                    PostCard(
                        moment = moment,
                        modifier = Modifier.sharedElement(
                            rememberSharedContentState(key = "home-following-${moment.id}"),
                            animatedVisibilityScope = animatedVisibilityScope
                        ),
                        onCardClick = { onMomentClick(moment.id) },
                        onUserClick = { onUserClick(moment.userId) },
                        onCommentClick = { onMomentClick(moment.id) }
                    )
                }
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                )
            }
        }
    }
}

@Composable
private fun FeaturedMomentCard(
    moment: Moment,
    modifier: Modifier = Modifier,
    onMomentClick: () -> Unit,
    onUserClick: () -> Unit
) {
    val context = LocalContext.current
    val imageUrl = remember(moment.content.attachments) {
        moment.content.attachments
            ?.firstOrNull { it.type == "image" }
            ?.url
            ?.let(NetworkModule::formatUrl)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onMomentClick),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (!imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(context).data(imageUrl).crossfade(true).build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.22f),
                                    MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        )
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.08f),
                                Color.Black.copy(alpha = 0.12f),
                                Color.Black.copy(alpha = 0.72f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(22.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color.White.copy(alpha = 0.16f))
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text(text = "精选瞬间", style = MaterialTheme.typography.labelLarge, color = Color.White)
                    }
                    Text(
                        text = formatMomentDate(moment.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.88f)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable(onClick = onUserClick)
                    ) {
                        UserAvatar(
                            avatar = moment.avatar,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = moment.nickname ?: "未知用户",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = (moment.handle ?: "").ifBlank { "点击查看资料" },
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = moment.content.text?.takeIf { it.isNotBlank() } ?: "这一刻没有文字，但有氛围。",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FeaturedStatPill("${moment.likesCount} 喜欢")
                        FeaturedStatPill("${moment.commentsCount} 评论")
                        moment.ipLocation?.takeIf { it.isNotBlank() }?.let { FeaturedStatPill(it) }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeaturedStatPill(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color.White.copy(alpha = 0.14f))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.labelMedium, color = Color.White)
    }
}

@Composable
private fun EmptyFeaturedState() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "还没有精选内容",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "稍后再来，这里会出现更有故事感的横向卡片。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun formatMomentDate(isoString: String): String {
    return if (isoString.length >= 10) isoString.take(10) else isoString
}
