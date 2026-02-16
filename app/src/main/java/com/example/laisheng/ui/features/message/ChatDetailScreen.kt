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
import com.example.laisheng.data.remote.NetworkModule
import com.example.laisheng.data.model.ChatMessage

import com.example.laisheng.ui.theme.Dimens

import com.example.laisheng.ui.components.LaishengLoading

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
            Surface(
                tonalElevation = 2.dp, 
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .navigationBarsPadding() // Handled by Scaffold usually if not in Surface, but good to be safe here if Surface is full width
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Input Text Field
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("说点什么吧...", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline) },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            disabledBorderColor = Color.Transparent,
                            cursorColor = MaterialTheme.colorScheme.primary
                        ),
                        textStyle = MaterialTheme.typography.bodyMedium,
                        maxLines = 4
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // Send Button
                    val isEnabled = text.isNotBlank()
                    IconButton(
                        onClick = {
                            viewModel.sendMessage(userId, otherId, text)
                            text = ""
                        },
                        enabled = isEnabled,
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = if (isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send, 
                            contentDescription = "Send", 
                            tint = if (isEnabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainerLowest) // Use theme color instead of hardcoded grey
        ) {
            when (val state = uiState) {
                is ChatDetailUiState.Loading -> {
                    LaishengLoading(modifier = Modifier.align(Alignment.Center))
                }
                is ChatDetailUiState.Success -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(Dimens.PaddingMedium),
                        verticalArrangement = Arrangement.spacedBy(Dimens.PaddingMedium)
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
                    Text(state.message, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp), // Reduce vertical padding for tighter group
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        // Timestamp (simplified logic: show for every message for now, or could use logic to show every 5 mins)
        // For now, let's just show it if it's long enough ago or simplified. 
        // Actually, let's add a small timestamp below the bubble or next to it? 
        // Let's just keep it simple but refine the bubble.

        Row(
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
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(Dimens.PaddingSmall))
            }

            Column(horizontalAlignment = if (isMe) Alignment.End else Alignment.Start) {
                Surface(
                    shape = RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (isMe) 18.dp else 4.dp,
                        bottomEnd = if (isMe) 4.dp else 18.dp
                    ),
                    color = if (isMe) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = if (isMe) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                    shadowElevation = 1.dp
                ) {
                    Text(
                        text = message.content.text ?: "",
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                            .widthIn(max = 280.dp), // Limit max width
                        fontSize = 16.sp,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                // Optional: Show time below bubble
                // Text(
                //     text = formatTime(message.createdAt), 
                //     style = MaterialTheme.typography.labelSmall, 
                //     color = MaterialTheme.colorScheme.outline,
                //     modifier = Modifier.padding(top = 2.dp)
                // )
            }

            if (isMe) {
                Spacer(modifier = Modifier.width(Dimens.PaddingSmall))
                AsyncImage(
                    model = avatarRequest,
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}
