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
import com.example.laisheng.data.model.Post
import com.example.laisheng.ui.theme.LaishengTheme

@Composable
fun PostCard(
    post: Post,
    modifier: Modifier = Modifier,
    onCardClick: () -> Unit = {},
    onMoreClick: () -> Unit = {},
    onLikeClick: () -> Unit = {},
    onCommentClick: () -> Unit = {},
    onBookmarkClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // --- 用户信息栏 ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 头像占位符：显示首字母
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = post.username.take(1).uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = post.username,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = post.handle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            IconButton(onClick = onMoreClick) {
                Icon(
                    imageVector = Icons.Default.MoreHoriz,
                    contentDescription = "更多",
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- 正文内容 ---
        Text(
            text = post.content,
            style = MaterialTheme.typography.bodyLarge.copy(
                lineHeight = 24.sp,
                letterSpacing = 0.5.sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 装饰性小条 (保留原设计风格)
        Box(
            modifier = Modifier
                .width(32.dp)
                .height(4.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // --- 底部交互栏 ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = post.timestamp,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                PostActionItem(
                    icon = Icons.Outlined.FavoriteBorder,
                    count = post.likeCount,
                    contentDescription = "点赞",
                    onClick = onLikeClick
                )

                Spacer(modifier = Modifier.width(16.dp))

                PostActionItem(
                    icon = Icons.Outlined.ChatBubbleOutline,
                    count = post.commentCount,
                    contentDescription = "评论",
                    onClick = onCommentClick
                )

                Spacer(modifier = Modifier.width(16.dp))

                IconButton(
                    onClick = onBookmarkClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.BookmarkBorder,
                        contentDescription = "收藏",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    }
}

/**
 * 提取重复的互动按钮组件
 */
@Composable
private fun PostActionItem(
    icon: ImageVector,
    count: Int,
    contentDescription: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(20.dp)
        )
        if (count > 0) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PostCardPreview() {
    LaishengTheme {
        PostCard(
            post = Post(
                id = 1,
                username = "测试用户",
                handle = "@test_user",
                content = "这是一条优化后的卡片测试内容，看起来比之前更专业了。支持多行显示且具有良好的行间距。",
                timestamp = "10分钟前",
                likeCount = 128,
                commentCount = 64
            )
        )
    }
}