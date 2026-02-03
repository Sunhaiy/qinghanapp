package com.example.laisheng.ui.features.message

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.example.laisheng.data.NetworkModule
import com.example.laisheng.data.model.ChatListItem
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageScreen(
    hazeState: HazeState,
    userId: String,
    paddingValues: PaddingValues,
    onChatClick: (String, String, String?) -> Unit, // 修正：增加 avatarUrl 参数
    viewModel: MessageViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            viewModel.loadChatList(userId)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
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
                        CircularProgressIndicator()
                    }
                }
                is MessageUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = paddingValues
                    ) {
                        if (state.chatList.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillParentMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("暂时没有新消息", color = Color.Gray)
                                }
                            }
                        } else {
                            items(state.chatList) { item ->
                                ChatItem(
                                    item = item,
                                    onClick = { 
                                        val targetId = item.userId
                                        if (!targetId.isNullOrEmpty()) {
                                            // 关键点：将头像 URL 也传给导航
                                            onChatClick(targetId, item.nickname ?: "聊天", item.avatar)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
                is MessageUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.message, color = Color.Red)
                    }
                }
            }
        }
    }
}

@Composable
fun ChatItem(
    item: ChatListItem,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(50.dp)) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(NetworkModule.formatUrl(item.avatar))
                        .decoderFactory(SvgDecoder.Factory())
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop
                )
                if (item.unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(Color.Red, CircleShape)
                            .align(Alignment.TopEnd)
                            .border(2.dp, Color.White, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.nickname ?: "未知用户",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatChatTime(item.lastTime),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.lastMessage ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
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
        if (isoString.contains("T")) {
            isoString.substring(11, 16)
        } else {
            isoString
        }
    } catch (_: Exception) {
        ""
    }
}
