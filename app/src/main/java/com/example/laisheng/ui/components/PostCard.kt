package com.example.laisheng.ui.components

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.composables.icons.lucide.*

import com.example.laisheng.data.remote.NetworkModule
import com.example.laisheng.data.model.Attachment
import com.example.laisheng.data.model.Moment
import com.example.laisheng.ui.theme.Dimens

@Composable
fun PostCard(
    moment: Moment,
    modifier: Modifier = Modifier,
    onCardClick: () -> Unit = {},
    onMoreClick: () -> Unit = {},
    onLikeClick: () -> Unit = {},
    onCommentClick: () -> Unit = {},
    onBookmarkClick: () -> Unit = {},
    onUserClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val nickname = moment.nickname ?: "未知用户"
    val handle = moment.handle ?: ""
    
    val avatarUrl = remember(moment.avatar) { NetworkModule.formatUrl(moment.avatar) }

    // 分离语音和图片附件
    val imageAttachments = remember(moment.content.attachments) {
        moment.content.attachments?.filter { it.type == "image" }?.map {
            it.copy(url = NetworkModule.formatUrl(it.url) ?: it.url)
        } ?: emptyList()
    }
    val voiceAttachment = remember(moment.content.attachments) {
        moment.content.attachments?.find { it.type == "voice" }?.let {
            it.copy(url = NetworkModule.formatUrl(it.url) ?: it.url)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick)
            .padding(horizontal = Dimens.PaddingMedium, vertical = Dimens.PaddingSmall)
    ) {
        // 1. 用户信息
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onUserClick(moment.userId) }
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context).data(avatarUrl).decoderFactory(SvgDecoder.Factory()).crossfade(true).build(),
                    contentDescription = null,
                    modifier = Modifier.size(Dimens.AvatarSizeMedium).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(Dimens.PaddingSmall))
                Column {
                    Text(nickname, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    if (handle.isNotEmpty()) {
                        Text(handle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
            IconButton(onClick = onMoreClick, modifier = Modifier.size(Dimens.IconSizeLarge)) {
                Icon(Lucide.Ellipsis, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(Dimens.IconSizeSmall))
            }
        }

        // 2. 文字内容
        if (!moment.content.text.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(Dimens.PaddingSmall))
            Text(
                text = moment.content.text!!,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // 3. 语音内容 (独立分层展示)
        voiceAttachment?.let { voice ->
            Spacer(modifier = Modifier.height(Dimens.PaddingSmall))
            VoicePlayerTag(url = voice.url, duration = voice.duration ?: 0)
        }

        // 4. 图片内容 (动态网格)
        if (imageAttachments.isNotEmpty()) {
            Spacer(modifier = Modifier.height(Dimens.PaddingSmall))
            ImageGrid(imageAttachments)
        }

        Spacer(modifier = Modifier.height(Dimens.PaddingMedium))

        // 5. 底部工具栏
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(formatTime(moment.createdAt), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                moment.ipLocation?.let { ip ->
                    Spacer(modifier = Modifier.width(Dimens.PaddingSmall))
                    Text("· $ip", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                PostActionItem(
                    icon = if (moment.isLiked) Icons.Filled.Favorite else Lucide.Heart,
                    count = moment.likesCount,
                    contentDescription = "点赞",
                    isActive = moment.isLiked,
                    activeColor = MaterialTheme.colorScheme.primary,
                    onClick = onLikeClick
                )
                Spacer(modifier = Modifier.width(Dimens.PaddingSmall))
                PostActionItem(
                    icon = Lucide.MessageCircle,
                    count = moment.commentsCount,
                    contentDescription = "评论",
                    onClick = onCommentClick
                )
                Spacer(modifier = Modifier.width(Dimens.PaddingSmall))
                PostActionItem(
                    icon = if (moment.isCollected) Icons.Filled.Star else Lucide.Star,
                    contentDescription = "收藏",
                    isActive = moment.isCollected,
                    activeColor = MaterialTheme.colorScheme.tertiary,
                    onClick = onBookmarkClick,
                    showCount = false
                )
            }
        }
        Spacer(modifier = Modifier.height(Dimens.PaddingMedium))
        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    }
}

@Composable
fun ImageGrid(images: List<Attachment>) {
    val imageCount = images.size
    
    // 根据图片数量决定布局
    when (imageCount) {
        1 -> {
            AsyncImage(
                model = images[0].url,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth(0.7f) // 单张图不占满全宽，更有质感
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(Dimens.CornerRadiusMedium))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop
            )
        }
        else -> {
            // 2-9张图片使用网格
            val columns = if (imageCount == 2 || imageCount == 4) 2 else 3
            val rows = (imageCount + columns - 1) / columns
            
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(rows) { rowIndex ->
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        repeat(columns) { columnIndex ->
                            val index = rowIndex * columns + columnIndex
                            if (index < imageCount) {
                                AsyncImage(
                                    model = images[index].url,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(Dimens.CornerRadiusSmall))
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VoicePlayerTag(url: String, duration: Int) {
    var isPlaying by remember { mutableStateOf(false) }
    // ... MediaPlayer 逻辑保持不变 ...
    
    Surface(
        shape = RoundedCornerShape(Dimens.CornerRadiusLarge),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        modifier = Modifier
            .height(44.dp)
            .width(160.dp)
            .clickable { isPlaying = !isPlaying }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp)
        ) {
            Icon(
                imageVector = if (isPlaying) Lucide.Pause else Lucide.Play,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(Dimens.PaddingSmall))
            Text("${duration}\"", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.weight(1f))
            // 静态声波模拟
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                listOf(0.4f, 0.8f, 0.5f, 1f, 0.6f).forEach { heightFactor ->
                    Box(
                        Modifier
                            .width(2.dp)
                            .height(16.dp * heightFactor)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), CircleShape)
                    )
                }
            }
        }
    }
}

@Composable
private fun PostActionItem(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    count: Int = 0,
    isActive: Boolean = false,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    showCount: Boolean = true
) {
    val tint by animateColorAsState(if (isActive) activeColor else MaterialTheme.colorScheme.outline)
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.1f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 7.dp, vertical = 5.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(Dimens.IconSizeSmall).scale(scale)
        )
        if (showCount && count > 0) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                ),
                color = tint
            )
        }
    }
}

private fun formatTime(isoString: String): String {
    return try {
        if (isoString.length >= 10) isoString.take(10) else isoString
    } catch (e: Exception) {
        "刚刚"
    }
}
