package com.example.laisheng.ui.features.home

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.util.lerp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.laisheng.ui.components.LaishengLoading
import com.example.laisheng.ui.components.PostCard
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
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

    Scaffold(
        topBar = {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = {},
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("每日精选") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("关注") }
                )
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
            } else {
                if (selectedTab == 0) {
                    // Featured Moments (Horizontal Pager)
                    if (featuredMoments.isNotEmpty()) {
                        val pagerState = rememberPagerState(pageCount = { featuredMoments.size })
                        
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center
                        ) {
                            HorizontalPager(
                                state = pagerState,
                                contentPadding = PaddingValues(horizontal = 32.dp),
                                pageSize = PageSize.Fill,
                                pageSpacing = 16.dp,
                                modifier = Modifier.height(500.dp) // Fixed height for carousel
                            ) { page ->
                                val moment = featuredMoments[page]
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .graphicsLayer {
                                            // Calculate the absolute offset for the current page from the
                                            // scroll position. We use the absolute value which allows us to mirror
                                            // any effects for both directions
                                            val pageOffset = (
                                                (pagerState.currentPage - page) + pagerState
                                                .currentPageOffsetFraction
                                            ).absoluteValue
            
                                            // We animate the alpha, between 50% and 100%
                                            alpha = lerp(
                                                start = 0.5f,
                                                stop = 1f,
                                                fraction = 1f - pageOffset.coerceIn(0f, 1f)
                                            )
                                            
                                            // Scale Y to create a carousel effect
                                            scaleY = lerp(
                                                start = 0.85f,
                                                stop = 1f,
                                                fraction = 1f - pageOffset.coerceIn(0f, 1f)
                                            )
                                        },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                                ) {
                                    // Reuse PostCard but maybe wrap it to look better in a card
                                    // For now, putting PostCard directly inside might look okay, or we can customize
                                    with(sharedTransitionScope) {
                                        PostCard(
                                            moment = moment,
                                            modifier = Modifier.fillMaxWidth(), // Provide scope but no specific shared element modifier for now inside pager to avoid complexity
                                            onCardClick = { onMomentClick(moment.id) },
                                            onUserClick = { onUserClick(moment.userId) },
                                            onLikeClick = { /* simplified for now */ Toast.makeText(context, "请在详情页操作", Toast.LENGTH_SHORT).show() },
                                            onCommentClick = { onMomentClick(moment.id) },
                                            onBookmarkClick = { /* simplified */ }
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("暂无精选内容", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                } else {
                    // Following Moments (Vertical List)
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding() + 16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (followingMoments.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("还没有关注任何人，或关注的人没有动态", style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                        } else {
                            items(followingMoments) { moment ->
                                with(sharedTransitionScope) {
                                    PostCard(
                                        moment = moment,
                                        modifier = Modifier.sharedElement(
                                            rememberSharedContentState(key = "home-following-${moment.id}"),
                                            animatedVisibilityScope = animatedVisibilityScope
                                        ),
                                        onCardClick = { onMomentClick(moment.id) },
                                        onUserClick = { onUserClick(moment.userId) },
                                        onLikeClick = { /* simplified */ },
                                        onCommentClick = { onMomentClick(moment.id) },
                                        onBookmarkClick = { /* simplified */ }
                                    )
                                }
                                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                            }
                        }
                    }
                }
            }
        }
    }
}