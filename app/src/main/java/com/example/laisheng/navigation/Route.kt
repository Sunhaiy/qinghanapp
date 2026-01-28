package com.example.laisheng.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Place
import androidx.compose.ui.graphics.vector.ImageVector

// 定义路由路径和底部栏的配置
sealed class Route(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : Route(
        route = "home",
        title = "首页",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    )
    object Explore : Route(
        route = "explore",
        title = "探索",
        selectedIcon = Icons.Filled.Place,
        unselectedIcon = Icons.Outlined.Place
    )
    object Post : Route(
        route = "post",
        title = "发布",
        selectedIcon = Icons.Filled.AddCircle,
        unselectedIcon = Icons.Outlined.AddCircle
    )
    object Message : Route(
        route = "message",
        title = "消息",
        selectedIcon = Icons.Filled.Email,
        unselectedIcon = Icons.Outlined.Email
    )
    object Mine : Route(
        route = "mine",
        title = "我的",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )
}
