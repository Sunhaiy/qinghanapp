package com.example.laisheng.ui.theme

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object AppIcons {
    const val Home = "\uee2b"
    const val Explore = "\uebc4"
    const val Add = "\uea13"
    const val Bell = "\uef9a"
    const val Search = "\uf0d1"
    const val Message = "\ueb4d"
    const val User = "\uf264"
    const val UserOutline = "\uf256"
    const val Settings = "\uf0ee"
    const val More = "\uef77"
    const val Heart = "\uee0f"
    const val HeartFill = "\uee0e"
    const val Star = "\uf18b"
    const val StarFill = "\uf186"
    const val Bookmark = "\ueae5"
    const val Play = "\uf00b"
    const val Pause = "\uefd8"
    const val Palette = "\uefc5"
    const val Info = "\uee59"
    const val Warning = "\ueca1"
    const val ArrowLeft = "\uea60"
    const val ArrowRight = "\uf493"
    const val Send = "\uf0d9"
    const val Close = "\ueb99"
    const val Camera = "\ueb31"
    const val ImageAdd = "\uee47"
    const val Upload = "\uf24e"
    const val Mic = "\uef50"
    const val Eye = "\uecb5"
    const val EyeOff = "\uecb7"
    const val Moon = "\uef6f"
    const val Sun = "\uf1bf"
    const val Edit = "\uec86"
    const val List = "\uf44b"
    const val Mail = "\ueef6"
    const val Logout = "\ueeda"
}

@Composable
fun AppIcon(
    glyph: String,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    size: Dp = 20.dp
) {
    Text(
        text = glyph,
        modifier = modifier,
        color = tint,
        style = TextStyle(
            fontFamily = RemixIconFontFamily,
            fontSize = size.value.sp,
            lineHeight = size.value.sp
        )
    )
}
