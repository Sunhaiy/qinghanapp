package com.example.laisheng.ui.navigation

import com.example.laisheng.ui.theme.AppIcons

sealed class Route(
    val route: String,
    val title: String,
    val selectedIcon: String,
    val unselectedIcon: String
) {
    object Home : Route("home", "首页", AppIcons.Home, AppIcons.Home)
    object Explore : Route("explore", "探索", AppIcons.Explore, AppIcons.Explore)
    object Post : Route("post", "发布", AppIcons.Add, AppIcons.Add)
    object Message : Route("message", "消息", AppIcons.Message, AppIcons.Message)
    object Mine : Route("mine", "我的", AppIcons.User, AppIcons.User)
    object Search : Route("search", "搜索", AppIcons.Search, AppIcons.Search)
    object Settings : Route("settings", "设置", AppIcons.Settings, AppIcons.Settings)
}
