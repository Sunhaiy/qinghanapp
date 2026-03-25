package com.example.laisheng.ui.navigation

import com.example.laisheng.ui.theme.AppIcons

sealed class Route(
    val route: String,
    val title: String,
    val selectedIcon: String,
    val unselectedIcon: String
) {
    object Home : Route(
        route = "home",
        title = "首页",
        selectedIcon = AppIcons.Home,
        unselectedIcon = AppIcons.Home
    )

    object Explore : Route(
        route = "explore",
        title = "探索",
        selectedIcon = AppIcons.Explore,
        unselectedIcon = AppIcons.Explore
    )

    object Post : Route(
        route = "post",
        title = "发布",
        selectedIcon = AppIcons.Add,
        unselectedIcon = AppIcons.Add
    )

    object Message : Route(
        route = "message",
        title = "消息",
        selectedIcon = AppIcons.Message,
        unselectedIcon = AppIcons.Message
    )

    object Mine : Route(
        route = "mine",
        title = "我的",
        selectedIcon = AppIcons.User,
        unselectedIcon = AppIcons.User
    )

    object Search : Route(
        route = "search",
        title = "搜索",
        selectedIcon = AppIcons.Search,
        unselectedIcon = AppIcons.Search
    )

    object Settings : Route(
        route = "settings",
        title = "设置",
        selectedIcon = AppIcons.Settings,
        unselectedIcon = AppIcons.Settings
    )
}
