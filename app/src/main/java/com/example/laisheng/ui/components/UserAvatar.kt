package com.example.laisheng.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.example.laisheng.data.remote.NetworkModule
import com.example.laisheng.ui.theme.AppIcon
import com.example.laisheng.ui.theme.AppIcons

@Composable
fun UserAvatar(
    avatar: String?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    val context = LocalContext.current
    val avatarUrl = remember(avatar) { NetworkModule.formatUrl(avatar) }

    if (avatarUrl.isNullOrBlank()) {
        AvatarPlaceholder(modifier = modifier, contentDescription = contentDescription)
        return
    }

    SubcomposeAsyncImage(
        model = ImageRequest.Builder(context)
            .data(avatarUrl)
            .decoderFactory(SvgDecoder.Factory())
            .crossfade(true)
            .build(),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Crop,
        loading = {
            AvatarPlaceholder(modifier = Modifier.fillMaxSize(), contentDescription = contentDescription)
        },
        error = {
            AvatarPlaceholder(modifier = Modifier.fillMaxSize(), contentDescription = contentDescription)
        },
        success = {
            SubcomposeAsyncImageContent(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        }
    )
}

@Composable
private fun AvatarPlaceholder(
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.9f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        AppIcon(
            glyph = AppIcons.UserOutline,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
            size = 22.dp
        )
    }
}
