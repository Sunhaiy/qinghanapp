package com.example.laisheng.ui.components

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Ellipsis
import com.composables.icons.lucide.Heart
import com.composables.icons.lucide.MessageCircle
import com.composables.icons.lucide.Pause
import com.composables.icons.lucide.Play
import com.composables.icons.lucide.Star
import com.example.laisheng.data.model.Attachment
import com.example.laisheng.data.model.Moment
import com.example.laisheng.data.remote.NetworkModule
import com.example.laisheng.ui.theme.Dimens

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PostCard(
    moment: Moment,
    modifier: Modifier = Modifier,
    onCardClick: () -> Unit = {},
    onMoreClick: () -> Unit = {},
    onLikeClick: () -> Unit = {},
    onCommentClick: () -> Unit = {},
    onBookmarkClick: () -> Unit = {},
    onUserClick: (String) -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    val nickname = moment.nickname ?: "未知用户"
    val handle = moment.handle ?: ""

    val imageAttachments = remember(moment.content.attachments) {
        moment.content.attachments
            ?.filter { it.type == "image" }
            ?.map { it.copy(url = NetworkModule.formatUrl(it.url) ?: it.url) }
            .orEmpty()
    }
    val voiceAttachment = remember(moment.content.attachments) {
        moment.content.attachments
            ?.firstOrNull { it.type == "voice" }
            ?.let { attachment ->
                attachment.copy(url = NetworkModule.formatUrl(attachment.url) ?: attachment.url)
            }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onCardClick, onLongClick = onLongClick)
            .padding(all = Dimens.PaddingMedium)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onUserClick(moment.userId) }
            ) {
                UserAvatar(
                    avatar = moment.avatar,
                    modifier = Modifier
                        .size(Dimens.AvatarSizeMedium)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
                Spacer(modifier = Modifier.width(Dimens.PaddingSmall))
                Column {
                    Text(
                        text = nickname,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    if (handle.isNotEmpty()) {
                        Text(
                            text = handle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
            IconButton(onClick = onMoreClick, modifier = Modifier.size(Dimens.IconSizeLarge)) {
                Icon(
                    imageVector = Lucide.Ellipsis,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(Dimens.IconSizeSmall)
                )
            }
        }

        if (!moment.content.text.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(Dimens.PaddingSmall))
            Text(
                text = moment.content.text.orEmpty(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        voiceAttachment?.let { voice ->
            Spacer(modifier = Modifier.height(Dimens.PaddingSmall))
            VoicePlayerTag(url = voice.url, duration = voice.duration ?: 0)
        }

        if (imageAttachments.isNotEmpty()) {
            Spacer(modifier = Modifier.height(Dimens.PaddingSmall))
            ImageGrid(imageAttachments)
        }

        Spacer(modifier = Modifier.height(Dimens.PaddingMedium))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    formatTime(moment.createdAt),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )
                moment.ipLocation?.let { ip ->
                    Spacer(modifier = Modifier.width(Dimens.PaddingSmall))
                    Text(
                        "· $ip",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
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
    }
}

@Composable
fun ImageGrid(images: List<Attachment>) {
    when (images.size) {
        1 -> {
            AsyncImage(
                model = images.first().url,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(Dimens.CornerRadiusMedium))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop
            )
        }

        else -> {
            val columns = if (images.size == 2 || images.size == 4) 2 else 3
            val rows = (images.size + columns - 1) / columns
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(rows) { rowIndex ->
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        repeat(columns) { columnIndex ->
                            val index = rowIndex * columns + columnIndex
                            if (index < images.size) {
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
    var isPrepared by remember(url) { mutableStateOf(false) }

    val mediaPlayer = remember(url) {
        MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setDataSource(url)
            setOnPreparedListener { isPrepared = true }
            setOnCompletionListener { isPlaying = false }
            prepareAsync()
        }
    }

    DisposableEffect(mediaPlayer) {
        onDispose {
            runCatching { mediaPlayer.stop() }
            runCatching { mediaPlayer.release() }
            isPrepared = false
            isPlaying = false
        }
    }

    Surface(
        shape = RoundedCornerShape(Dimens.CornerRadiusLarge),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        modifier = Modifier
            .height(44.dp)
            .width(160.dp)
            .clickable {
                if (!isPrepared) return@clickable
                if (isPlaying) {
                    runCatching { mediaPlayer.pause() }
                    isPlaying = false
                } else {
                    runCatching { mediaPlayer.start() }
                    isPlaying = true
                }
            }
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
            Text(
                text = "${duration}\"",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.weight(1f))
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
    val tint by animateColorAsState(
        targetValue = if (isActive) activeColor else MaterialTheme.colorScheme.outline,
        label = "post_action_tint"
    )
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "post_action_scale"
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
            modifier = Modifier
                .size(Dimens.IconSizeSmall)
                .scale(scale)
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

private fun formatTime(isoString: String): String =
    try {
        if (isoString.length >= 10) isoString.take(10) else isoString
    } catch (_: Exception) {
        "刚刚"
    }
