package com.example.laisheng.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import com.composables.icons.lucide.*

sealed class Route(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : Route(
        route = "home",
        title = "首页",
        selectedIcon = Lucide.House,
        unselectedIcon = Lucide.House
    )
    object Explore : Route(
        route = "explore",
        title = "探索",
        selectedIcon = Lucide.Compass,
        unselectedIcon = Lucide.Compass
    )
    object Post : Route(
        route = "post",
        title = "发布",
        selectedIcon = Lucide.Plus,
        unselectedIcon = Lucide.Plus
    )
    object Message : Route(
        route = "message",
        title = "消息",
        selectedIcon = Lucide.MessageCircle,
        unselectedIcon = Lucide.MessageCircle
    )
    object Mine : Route(
        route = "mine",
        title = "我的",
        selectedIcon = Lucide.User,
        unselectedIcon = Lucide.User
    )
}
