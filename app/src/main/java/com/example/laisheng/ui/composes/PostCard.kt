package com.example.laisheng.ui.composes

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.laisheng.data.NetworkModule
import com.example.laisheng.data.model.Attachment
import com.example.laisheng.data.model.Moment

@Composable
fun PostCard(
    moment: Moment,
    modifier: Modifier = Modifier,
    onCardClick: () -> Unit = {},
    onMoreClick: () -> Unit = {},
    onLikeClick: () -> Unit = {},
    onCommentClick: () -> Unit = {},
    onBookmarkClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val nickname = moment.nickname ?: "未知用户"
    val handle = moment.handle ?: ""
    
    val avatarUrl = remember(moment.avatar) { 
        NetworkModule.formatUrl(moment.avatar) 
    }
    
    val avatarLetter = if (nickname.isNotEmpty()) nickname.take(1).uppercase() else "?"

    val processedAttachments = remember(moment.content.attachments) {
        moment.content.attachments?.map {
            it.copy(url = NetworkModule.formatUrl(it.url) ?: it.url)
        } ?: emptyList()
    }

    val hasText = !moment.content.text.isNullOrBlank()
    val hasAttachments = processedAttachments.isNotEmpty()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(44.dp)) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(avatarUrl)
                            .decoderFactory(SvgDecoder.Factory())
                            .crossfade(true)
                            .build(),
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentScale = ContentScale.Crop
                    )
                    if (avatarUrl.isNullOrEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = avatarLetter,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = nickname,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
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
            IconButton(onClick = onMoreClick, modifier = Modifier.size(32.dp)) {
                Icon(Lucide.Ellipsis, "更多", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(16.dp))
            }
        }

        if (hasText) {
            Spacer(modifier = Modifier.height(12.dp))
            moment.content.text?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp, letterSpacing = 0.5.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (hasAttachments) {
            Spacer(modifier = Modifier.height(12.dp))
            MediaGallery(processedAttachments)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatTime(moment.createdAt),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                PostActionItem(
                    icon = if (moment.isLiked) Icons.Filled.Favorite else Lucide.Heart,
                    count = moment.likesCount,
                    contentDescription = "点赞",
                    isActive = moment.isLiked,
                    activeColor = Color(0xFFE91E63),
                    onClick = onLikeClick
                )
                Spacer(modifier = Modifier.width(8.dp))
                PostActionItem(
                    icon = Lucide.MessageCircle,
                    count = moment.commentsCount,
                    contentDescription = "评论",
                    onClick = onCommentClick
                )
                Spacer(modifier = Modifier.width(8.dp))
                PostActionItem(
                    icon = if (moment.isCollected) Icons.Filled.Star else Lucide.Star,
                    contentDescription = "收藏",
                    isActive = moment.isCollected,
                    activeColor = Color(0xFFFFC107),
                    onClick = onBookmarkClick,
                    showCount = false
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    }
}

@Composable
fun MediaGallery(attachments: List<Attachment>) {
    val context = LocalContext.current
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(attachments) { attachment ->
            when (attachment.type) {
                "image" -> {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(attachment.url)
                            .decoderFactory(SvgDecoder.Factory())
                            .crossfade(true)
                            .build(),
                        contentDescription = "图片",
                        modifier = Modifier
                            .size(160.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.LightGray),
                        contentScale = ContentScale.Crop
                    )
                }
                "voice" -> {
                    VoicePlayerTag(url = attachment.url, duration = attachment.duration ?: 0)
                }
            }
        }
    }
}

@Composable
fun VoicePlayerTag(url: String, duration: Int) {
    var isPlaying by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier
            .height(40.dp)
            .width(140.dp)
            .clickable {
                try {
                    if (isPlaying) {
                        mediaPlayer?.pause()
                        isPlaying = false
                    } else {
                        if (mediaPlayer == null) {
                            mediaPlayer = MediaPlayer().apply {
                                setAudioAttributes(
                                    AudioAttributes.Builder()
                                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                        .setUsage(AudioAttributes.USAGE_MEDIA)
                                        .build()
                                )
                                setDataSource(url)
                                setOnPreparedListener { 
                                    it.start()
                                    isPlaying = true 
                                }
                                setOnCompletionListener { 
                                    isPlaying = false 
                                }
                                setOnErrorListener { _, _, _ ->
                                    isPlaying = false
                                    true
                                }
                                prepareAsync()
                            }
                        } else {
                            mediaPlayer?.start()
                            isPlaying = true
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    isPlaying = false
                }
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Icon(
                imageVector = if (isPlaying) Lucide.Pause else Lucide.Play,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("${duration}\"", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                repeat(5) {
                    Box(
                        Modifier
                            .width(2.dp)
                            .height(if (isPlaying) (8..16).random().dp else 10.dp)
                            .background(if (isPlaying) MaterialTheme.colorScheme.primary else Color.Gray)
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
            modifier = Modifier.size(18.dp).scale(scale)
        )
        if (showCount && count > 0) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 12.sp,
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
