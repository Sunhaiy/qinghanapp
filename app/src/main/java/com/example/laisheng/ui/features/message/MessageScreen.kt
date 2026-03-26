package com.example.laisheng.ui.features.message

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.laisheng.data.model.ChatListItem
import com.example.laisheng.ui.components.LaishengLoading
import com.example.laisheng.ui.components.UserAvatar
import com.example.laisheng.ui.theme.AppIcon
import com.example.laisheng.ui.theme.AppIcons
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageScreen(
    hazeState: HazeState,
    userId: String,
    paddingValues: PaddingValues,
    onChatClick: (String, String, String?) -> Unit,
    viewModel: MessageViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) viewModel.loadChatList(userId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .hazeSource(state = hazeState)
    ) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.loadChatList(userId) },
            modifier = Modifier.fillMaxSize()
        ) {
            when (val state = uiState) {
                is MessageUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        LaishengLoading()
                    }
                }

                is MessageUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                    }
                }

                is MessageUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = paddingValues.calculateTopPadding() + 8.dp,
                            bottom = paddingValues.calculateBottomPadding() + 18.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            MessageHeaderCard(userCount = state.chatList.size)
                        }

                        if (state.chatList.isNotEmpty()) {
                            item {
                                Column(modifier = Modifier.padding(horizontal = 4.dp)) {
                                    Text(
                                        text = "互相关注",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "默认优先展示和你双向关注的用户，点开就能直接聊天。",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        if (state.chatList.isEmpty()) {
                            item {
                                EmptyMessageState()
                            }
                        } else {
                            items(state.chatList) { item ->
                                ChatItem(
                                    item = item,
                                    onClick = {
                                        val targetId = item.userId
                                        if (!targetId.isNullOrEmpty()) {
                                            onChatClick(targetId, item.nickname ?: "聊天", item.avatar)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageHeaderCard(userCount: Int) {
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                AppIcon(glyph = AppIcons.Message, tint = MaterialTheme.colorScheme.primary, size = 20.dp)
            }
            Column(modifier = Modifier.padding(start = 14.dp)) {
                Text("消息", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                Text(
                    text = "当前有 $userCount 位可直接联系的用户",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptyMessageState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 120.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AppIcon(glyph = AppIcons.Message, tint = MaterialTheme.colorScheme.outline, size = 28.dp)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                "还没有可聊天的用户",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                "当出现互相关注或新会话后，会显示在这里。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ChatItem(
    item: ChatListItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(54.dp)) {
                UserAvatar(
                    avatar = item.avatar,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
                if (item.unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(MaterialTheme.colorScheme.error, CircleShape)
                            .align(Alignment.TopEnd)
                            .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.unreadCount.coerceAtMost(9).toString(),
                            color = MaterialTheme.colorScheme.onError,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.size(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.nickname ?: "未知用户",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = formatChatTime(item.lastTime),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.lastMessage ?: "开始聊天吧",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

fun formatChatTime(isoString: String?): String {
    if (isoString.isNullOrEmpty()) return ""
    return try {
        if (isoString.contains("T")) isoString.substring(11, 16) else isoString
    } catch (_: Exception) {
        ""
    }
}
