package com.example.laisheng.ui.composes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.laisheng.data.model.Moment
import com.example.laisheng.ui.theme.LaishengTheme

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
    // --- 修改点：直接从 moment 取值，不需要 moment.user ---
    val nickname = moment.nickname ?: "未知用户"
    val handle = moment.handle ?: ""
    val content = moment.content ?: ""

    val avatarLetter = if (nickname.isNotEmpty()) nickname.take(1).uppercase() else "?"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // ... (中间的 UI 代码完全不用动，省略以节省空间) ...
        // ... 保持 Row, Column, Text 等布局代码不变 ...

        // --- 用户信息栏 (这里不用改，因为上面已经定义了 nickname 变量) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = avatarLetter,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
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
            IconButton(onClick = onMoreClick) {
                Icon(Icons.Default.MoreHoriz, "更多", tint = MaterialTheme.colorScheme.outline)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- 正文 ---
        Text(
            text = content,
            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp, letterSpacing = 0.5.sp),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 装饰条
        Box(
            modifier = Modifier.width(32.dp).height(4.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // --- 底部栏 ---
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
                PostActionItem(Icons.Outlined.FavoriteBorder, moment.likesCount, "点赞", onLikeClick)
                Spacer(modifier = Modifier.width(16.dp))
                PostActionItem(Icons.Outlined.ChatBubbleOutline, moment.commentsCount, "评论", onCommentClick)
                Spacer(modifier = Modifier.width(16.dp))
                Icon(Icons.Outlined.BookmarkBorder, "收藏", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(24.dp).clickable(onClick = onBookmarkClick).padding(2.dp))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    }
}

// 辅助组件和函数保持不变
@Composable
private fun PostActionItem(icon: ImageVector, count: Int, contentDescription: String, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clip(CircleShape).clickable(onClick = onClick).padding(4.dp)) {
        Icon(icon, contentDescription, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(20.dp))
        if (count > 0) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(count.toString(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
        }
    }
}

private fun formatTime(isoString: String): String {
    return try { if (isoString.length >= 10) isoString.substring(0, 10) else isoString } catch (e: Exception) { "刚刚" }
}

// --- 修改点：预览也要更新，去掉嵌套的 User ---
@Preview(showBackground = true)
@Composable
fun PostCardPreview() {
    LaishengTheme {
        val mockMoment = Moment(
            id = "1",
            userId = "u1",
            content = "这是一条测试内容，现在直接读取扁平数据。",
            likesCount = 128,
            commentsCount = 64,
            createdAt = "2023-10-27T10:00:00Z",

            // 直接赋值，不再需要 User(...)
            nickname = "Lisa Wong",
            handle = "@lisa_art",
            avatar = "https://..."
        )
        PostCard(moment = mockMoment)
    }
}