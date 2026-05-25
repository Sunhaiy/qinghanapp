package com.example.laisheng.ui.features.mine

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.laisheng.data.model.HistoryItem
import com.example.laisheng.ui.components.LaishengLoading
import com.example.laisheng.ui.theme.AppIcon
import com.example.laisheng.ui.theme.AppIcons
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.HazeMaterials

@Composable
fun HistoryScreen(
    hazeState: HazeState,
    onBack: () -> Unit,
    onMomentClick: (String) -> Unit,
    viewModel: HistoryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    LaunchedEffect(Unit) {
        viewModel.message.collect { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    LaishengLoading(strokeWidth = 2.dp)
                }

                uiState.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(uiState.error ?: "加载失败", color = MaterialTheme.colorScheme.error)
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .hazeSource(state = hazeState),
                        contentPadding = PaddingValues(
                            top = 104.dp,
                            bottom = paddingValues.calculateBottomPadding() + 24.dp,
                            start = 16.dp,
                            end = 16.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Card(
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f)
                                )
                            ) {
                                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("浏览记录", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                                    Text(
                                        text = "当前保留 ${uiState.membership?.historyRetentionDays ?: 30} 天，单次最多加载 ${uiState.membership?.maxPageSize ?: 20} 条。",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (uiState.items.isNotEmpty()) {
                                        Button(onClick = { viewModel.clearAll() }) {
                                            Text("清空全部")
                                        }
                                    }
                                }
                            }
                        }

                        if (uiState.items.isEmpty()) {
                            item {
                                Card(
                                    shape = RoundedCornerShape(24.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 36.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("还没有浏览记录")
                                    }
                                }
                            }
                        } else {
                            items(uiState.items, key = { it.id }) { item ->
                                HistoryCard(
                                    item = item,
                                    onOpen = { onMomentClick(item.momentId) },
                                    onDelete = { viewModel.delete(item.momentId) }
                                )
                            }
                        }
                    }
                }
            }

            HistoryTopBar(hazeState = hazeState, onBack = onBack)
        }
    }
}

@Composable
private fun HistoryCard(
    item: HistoryItem,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpen)
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = item.content.text ?: "无文字内容",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "@${item.handle ?: item.nickname ?: "用户"}",
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "最后浏览 ${item.lastViewedAt}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("浏览 ${item.viewCount}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("点赞 ${item.likesCount}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("评论 ${item.commentsCount}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(onClick = onDelete) {
                Text("删除记录")
            }
        }
    }
}

@Composable
private fun HistoryTopBar(
    hazeState: HazeState,
    onBack: () -> Unit
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)) {
                Box(modifier = Modifier.clickable(onClick = onBack).padding(10.dp)) {
                    AppIcon(glyph = AppIcons.ArrowLeft, size = 20.dp)
                }
            }
            Text(
                text = "浏览记录",
                modifier = Modifier.padding(start = 14.dp),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold)
            )
        }
        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    }
}
