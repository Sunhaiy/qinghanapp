package com.example.laisheng.ui.features.detail

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import coil.request.ImageRequest
import com.example.laisheng.data.remote.NetworkModule
import com.example.laisheng.data.model.Comment
import com.example.laisheng.ui.components.PostCard

import com.example.laisheng.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun MomentDetailScreen(
    momentId: String,
    userId: String,
    onBack: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    viewModel: MomentDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var commentText by remember { mutableStateOf("") }

    LaunchedEffect(momentId) {
        viewModel.loadMomentDetail(momentId, userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("瞬间详情", fontSize = 18.sp) },
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
                modifier = Modifier.navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = Dimens.PaddingMedium, vertical = Dimens.PaddingSmall)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        placeholder = { Text("说点什么吧...") },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent
                        )
                    )
                    IconButton(
                        onClick = {
                            viewModel.postComment(userId, momentId, commentText)
                            commentText = ""
                        },
                        enabled = commentText.isNotBlank()
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "发送", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (val state = uiState) {
                is MomentDetailUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is MomentDetailUiState.Success -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            with(sharedTransitionScope) {
                                PostCard(
                                    moment = state.moment,
                                    modifier = Modifier.sharedElement(
                                        rememberSharedContentState(key = "item-${state.moment.id}"),
                                        animatedVisibilityScope = animatedVisibilityScope
                                    ),
                                    onLikeClick = { viewModel.onLikeClick(userId, state.moment.id) },
                                    onBookmarkClick = { viewModel.onBookmarkClick(userId, state.moment.id) }
                                )
                            }
                        }

                        item {
                            HorizontalDivider(thickness = Dimens.PaddingSmall, color = MaterialTheme.colorScheme.surfaceVariant)
                            Text(
                                text = "评论 (${state.comments.size})",
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(Dimens.PaddingMedium)
                            )
                        }

                        items(state.comments) { comment ->
                            CommentItem(comment)
                        }
                        
                        if (state.comments.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                    Text("抢个沙发吧~", color = MaterialTheme.colorScheme.outline)
                                }
                            }
                        }
                    }
                }
                is MomentDetailUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
fun CommentItem(comment: Comment) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .padding(Dimens.PaddingMedium)
            .fillMaxWidth()
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(NetworkModule.formatUrl(comment.avatar))
                .decoderFactory(SvgDecoder.Factory())
                .build(),
            contentDescription = null,
            modifier = Modifier
                .size(36.dp) // 36dp is essentially AvatarSizeSmall (32) or Medium (42). Let's keep 36 or move to 32/40. 36 is fine as specific. Or use AvatarSizeSmall.
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop
        )
        
        Spacer(modifier = Modifier.width(Dimens.PaddingMedium)) // Was 12.dp. 16dp is PaddingMedium.
        
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = comment.nickname ?: "未知用户",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(Dimens.PaddingSmall))
                Text(
                    text = if (comment.createdAt.contains("T")) comment.createdAt.substringBefore("T") else comment.createdAt,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = comment.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}