package com.example.laisheng.ui.features.message

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.laisheng.data.NetworkModule
import com.example.laisheng.data.model.ChatMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    userId: String,
    otherId: String,
    otherNickname: String,
    otherAvatarUrl: String?,
    onBack: () -> Unit,
    viewModel: ChatDetailViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val myAvatar by viewModel.myAvatar.collectAsState()
    var text by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // 预先处理好格式化后的 URL，避免在列表渲染时重复计算
    val formattedOtherAvatar = remember(otherAvatarUrl) { NetworkModule.formatUrl(otherAvatarUrl) }
    val formattedMyAvatar = remember(myAvatar) { NetworkModule.formatUrl(myAvatar) }

    LaunchedEffect(otherId) {
        viewModel.loadHistory(userId, otherId)
    }

    LaunchedEffect(uiState) {
        if (uiState is ChatDetailUiState.Success) {
            val messages = (uiState as ChatDetailUiState.Success).messages
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(otherNickname, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 8.dp, modifier = Modifier.navigationBarsPadding()) {
                Row(
                    modifier = Modifier.padding(8.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = text,
                        onValueChange = { text = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("说点什么吧...") },
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent
                        )
                    )
                    IconButton(
                        onClick = {
                            viewModel.sendMessage(userId, otherId, text)
                            text = ""
                        },
                        enabled = text.isNotBlank()
                    ) {
                        Icon(Icons.Default.Send, null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
        ) {
            when (val state = uiState) {
                is ChatDetailUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is ChatDetailUiState.Success -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(state.messages) { message ->
                            MessageBubble(
                                message = message, 
                                isMe = message.senderId == userId,
                                otherAvatar = formattedOtherAvatar,
                                myAvatar = formattedMyAvatar
                            )
                        }
                    }
                }
                is ChatDetailUiState.Error -> {
                    Text(state.message, color = Color.Red, modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage, isMe: Boolean, otherAvatar: String?, myAvatar: String?) {
    val context = LocalContext.current
    
    // 统一配置 ImageRequest，开启最高级别的缓存策略
    val avatarRequest = remember(if (isMe) myAvatar else otherAvatar) {
        ImageRequest.Builder(context)
            .data(if (isMe) myAvatar else otherAvatar)
            .decoderFactory(SvgDecoder.Factory())
            .crossfade(true)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .build()
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isMe) {
            AsyncImage(
                model = avatarRequest,
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray.copy(alpha = 0.3f)), // 增加淡灰色占位背景
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isMe) 16.dp else 2.dp,
                bottomEnd = if (isMe) 2.dp else 16.dp
            ),
            color = if (isMe) Color(0xFF95EC69) else Color.White,
            contentColor = Color.Black
        ) {
            Text(
                text = message.content.text ?: "",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                fontSize = 16.sp
            )
        }

        if (isMe) {
            Spacer(modifier = Modifier.width(8.dp))
            AsyncImage(
                model = avatarRequest,
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray.copy(alpha = 0.3f)), // 增加淡灰色占位背景
                contentScale = ContentScale.Crop
            )
        }
    }
}
