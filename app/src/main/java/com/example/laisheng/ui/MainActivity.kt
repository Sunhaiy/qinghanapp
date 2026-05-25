package com.example.laisheng.ui

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.laisheng.data.local.UserPrefs
import com.example.laisheng.data.local.MessageLocalStore
import com.example.laisheng.data.remote.AuthSession
import com.example.laisheng.data.remote.NetworkModule
import com.example.laisheng.data.remote.SocketManager
import com.example.laisheng.ui.features.detail.MomentDetailScreen
import com.example.laisheng.ui.features.explore.ExploreScreen
import com.example.laisheng.ui.features.home.HomeScreen
import com.example.laisheng.ui.features.login.LoginScreen
import com.example.laisheng.ui.features.message.ChatDetailScreen
import com.example.laisheng.ui.features.message.MessageScreen
import com.example.laisheng.ui.features.mine.HistoryScreen
import com.example.laisheng.ui.features.mine.MembershipScreen
import com.example.laisheng.ui.features.mine.MineScreen
import com.example.laisheng.ui.features.mine.MineTab
import com.example.laisheng.ui.features.mine.MineTabDetailScreen
import com.example.laisheng.ui.features.mine.UserProfileScreen
import com.example.laisheng.ui.features.mine.edit.EditProfileScreen
import com.example.laisheng.ui.features.mine.follows.FollowScreen
import com.example.laisheng.ui.features.notification.NotificationScreen
import com.example.laisheng.ui.features.notification.NotificationViewModel
import com.example.laisheng.ui.features.post.PostScreen
import com.example.laisheng.ui.features.settings.SettingsScreen
import com.example.laisheng.ui.navigation.Route
import com.example.laisheng.ui.components.LaishengLoading
import com.example.laisheng.ui.theme.AppIcon
import com.example.laisheng.ui.theme.AppIcons
import com.example.laisheng.ui.theme.LaishengTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val userPrefs = UserPrefs(this)
        MessageLocalStore.init(this)
        AuthSession.updateToken(userPrefs.getToken())
        val mainViewModel = MainViewModel(userPrefs)

        setContent {
            val themeMode by mainViewModel.themeMode.collectAsState()
            LaishengTheme(themeMode = themeMode) {
                Laisheng(mainViewModel)
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Laisheng(mainViewModel: MainViewModel) {
    val context = LocalContext.current
    val userPrefs = remember { UserPrefs(context) }
    val notificationViewModel: NotificationViewModel = viewModel()
    var isCheckingSession by remember { mutableStateOf(true) }
    var loggedInUserId by remember {
        mutableStateOf(userPrefs.getUserId().takeIf { !userPrefs.getToken().isNullOrBlank() })
    }

    SideEffect {
        AuthSession.updateToken(userPrefs.getToken())
    }

    LaunchedEffect(Unit) {
        val savedToken = userPrefs.getToken()
        if (savedToken.isNullOrBlank()) {
            AuthSession.clear()
            loggedInUserId = null
            isCheckingSession = false
            return@LaunchedEffect
        }

        AuthSession.updateToken(savedToken)
        val currentUser = runCatching { NetworkModule.apiService.getMe() }.getOrNull()
        if (currentUser == null) {
            AuthSession.clear()
            userPrefs.clearAuth()
            loggedInUserId = null
        } else {
            userPrefs.saveAuth(savedToken, currentUser)
            loggedInUserId = currentUser.id
        }
        isCheckingSession = false
    }

    LaunchedEffect(loggedInUserId) {
        if (isCheckingSession) return@LaunchedEffect
        loggedInUserId?.let { id ->
            SocketManager.connect(id)
            runCatching { NetworkModule.apiService.heartbeat() }
        } ?: SocketManager.disconnect()
    }

    DisposableEffect(Unit) {
        onDispose { SocketManager.disconnect() }
    }

    if (isCheckingSession) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            LaishengLoading()
        }
        return
    }

    if (loggedInUserId == null) {
        LoginScreen(onLoginSuccess = { userId ->
            AuthSession.updateToken(userPrefs.getToken())
            loggedInUserId = userId
        })
        return
    }

    val userId = loggedInUserId.orEmpty()
    val hazeState = rememberHazeState()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val unreadNotificationCount by notificationViewModel.unreadCount.collectAsState()
    val items = listOf(Route.Home, Route.Explore, Route.Post, Route.Message, Route.Mine)

    LaunchedEffect(loggedInUserId, currentRoute) {
        if (loggedInUserId != null) {
            notificationViewModel.refreshUnreadCount()
        }
    }

    Scaffold(
        topBar = {
            if (!isFullScreenRoute(currentRoute) && currentRoute != Route.Mine.route) {
                TopBar(
                    hazeState = hazeState,
                    currentRoute = currentRoute,
                    unreadNotificationCount = unreadNotificationCount,
                    onSearchClick = { navController.navigate(Route.Search.route) },
                    onNotificationClick = { navController.navigate(Route.Message.route) }
                )
            }
        },
        bottomBar = {
            if (!isFullScreenRoute(currentRoute)) {
                BottomNavigation(hazeState = hazeState, navController = navController, items = items)
            }
        }
    ) { paddingValues ->
        SharedTransitionLayout {
            NavHost(navController = navController, startDestination = Route.Home.route) {
                composable(Route.Home.route) {
                    HomeScreen(
                        hazeState = hazeState,
                        paddingValues = paddingValues,
                        userId = userId,
                        onMomentClick = { id -> navController.navigate("moment_detail/$id") },
                        onUserClick = { uid -> navController.navigate("user_profile/$uid") },
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this@composable
                    )
                }

                composable(Route.Explore.route) {
                    ExploreScreen(
                        hazeState,
                        paddingValues,
                        userId,
                        { id -> navController.navigate("moment_detail/$id") },
                        { uid -> navController.navigate("user_profile/$uid") },
                        this@SharedTransitionLayout,
                        this@composable
                    )
                }

                composable(Route.Search.route) {
                    com.example.laisheng.ui.features.explore.search.SearchScreen(
                        userId = userId,
                        onBackClick = { navController.popBackStack() },
                        onMomentClick = { id -> navController.navigate("moment_detail/$id") },
                        onUserClick = { uid -> navController.navigate("user_profile/$uid") },
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this@composable
                    )
                }

                composable(Route.Post.route) {
                    PostScreen(
                        userId = userId,
                        onCancel = { navController.popBackStack() },
                        onPostSuccess = { navController.popBackStack() }
                    )
                }

                composable(Route.Message.route) {
                    MessageScreen(
                        hazeState = hazeState,
                        userId = userId,
                        paddingValues = paddingValues,
                        onNotificationCenterClick = { navController.navigate("notifications") },
                        onChatClick = { id, name, avatar ->
                            val encName = Uri.encode(name)
                            val encAvatar = Uri.encode(avatar ?: "")
                            navController.navigate("chat/$id/$encName?avatar=$encAvatar")
                        },
                        onUserClick = { uid -> navController.navigate("user_profile/$uid") }
                    )
                }

                composable(Route.Mine.route) {
                    MineScreen(
                        hazeState = hazeState,
                        userId = userId,
                        paddingValues = paddingValues,
                        onFollowClick = { uid, title, type ->
                            val encodedTitle = Uri.encode(title)
                            navController.navigate("follows/$uid/$encodedTitle/$type")
                        },
                        onEditClick = { nick, handle, bio, av, bg, lastUpdate ->
                            val eN = Uri.encode(nick)
                            val eH = Uri.encode(handle)
                            val eB = Uri.encode(bio)
                            val eA = Uri.encode(av)
                            val eG = Uri.encode(bg)
                            val eL = Uri.encode(lastUpdate)
                            navController.navigate("edit_profile/$userId/$eH/$eN/$eB?avatar=$eA&bg=$eG&lastUpdate=$eL")
                        },
                        onMomentClick = { id -> navController.navigate("moment_detail/$id") },
                        onSettingsClick = { navController.navigate(Route.Settings.route) },
                        onOpenMoments = { navController.navigate("mine_tab/moments") },
                        onOpenLikes = { navController.navigate("mine_tab/likes") },
                        onOpenCollections = { navController.navigate("mine_tab/collections") },
                        onOpenHistory = { navController.navigate("history") },
                        onOpenMembership = { navController.navigate("membership") },
                        mainViewModel = mainViewModel
                    )
                }

                composable(
                    route = "mine_tab/{tab}",
                    arguments = listOf(navArgument("tab") { type = NavType.StringType })
                ) { backStackEntry ->
                    val tabArg = backStackEntry.arguments?.getString("tab") ?: "moments"
                    val tab = when (tabArg) {
                        "likes" -> MineTab.LIKED
                        "collections" -> MineTab.COLLECTED
                        else -> MineTab.MOMENTS
                    }
                    val title = when (tab) {
                        MineTab.MOMENTS -> "我的发布"
                        MineTab.LIKED -> "我的喜欢"
                        MineTab.COLLECTED -> "收藏夹"
                    }
                    MineTabDetailScreen(
                        hazeState = hazeState,
                        userId = userId,
                        title = title,
                        tab = tab,
                        onBack = { navController.popBackStack() },
                        onMomentClick = { id -> navController.navigate("moment_detail/$id") },
                        onUserClick = { uid -> navController.navigate("user_profile/$uid") }
                    )
                }

                composable(
                    route = "moment_detail/{momentId}",
                    arguments = listOf(navArgument("momentId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val momentId = backStackEntry.arguments?.getString("momentId").orEmpty()
                    MomentDetailScreen(
                        momentId,
                        userId,
                        { navController.popBackStack() },
                        { uid -> navController.navigate("user_profile/$uid") },
                        this@SharedTransitionLayout,
                        this@composable
                    )
                }

                composable(
                    route = "chat/{otherId}/{nickname}?avatar={avatar}",
                    arguments = listOf(
                        navArgument("otherId") { type = NavType.StringType },
                        navArgument("nickname") { type = NavType.StringType },
                        navArgument("avatar") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = ""
                        }
                    )
                ) { backStackEntry ->
                    val otherId = backStackEntry.arguments?.getString("otherId").orEmpty()
                    val nickname = Uri.decode(backStackEntry.arguments?.getString("nickname") ?: "聊天")
                    val avatar = Uri.decode(backStackEntry.arguments?.getString("avatar").orEmpty())
                    ChatDetailScreen(userId, otherId, nickname, avatar, { navController.popBackStack() })
                }

                composable(
                    route = "follows/{userId}/{title}/{type}",
                    arguments = listOf(
                        navArgument("userId") { type = NavType.StringType },
                        navArgument("title") { type = NavType.StringType },
                        navArgument("type") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    FollowScreen(
                        userId = backStackEntry.arguments?.getString("userId").orEmpty(),
                        title = backStackEntry.arguments?.getString("title") ?: "列表",
                        type = backStackEntry.arguments?.getString("type").orEmpty(),
                        onBack = { navController.popBackStack() },
                        onUserClick = { targetUid -> navController.navigate("user_profile/$targetUid") }
                    )
                }

                composable(
                    route = "edit_profile/{userId}/{handle}/{nickname}/{bio}?avatar={avatar}&bg={bg}&lastUpdate={lastUpdate}",
                    arguments = listOf(
                        navArgument("userId") { type = NavType.StringType },
                        navArgument("handle") { type = NavType.StringType },
                        navArgument("nickname") { type = NavType.StringType },
                        navArgument("bio") { type = NavType.StringType },
                        navArgument("avatar") { type = NavType.StringType; nullable = true },
                        navArgument("bg") { type = NavType.StringType; nullable = true },
                        navArgument("lastUpdate") { type = NavType.StringType; nullable = true }
                    )
                ) { backStackEntry ->
                    EditProfileScreen(
                        userId = backStackEntry.arguments?.getString("userId").orEmpty(),
                        handle = Uri.decode(backStackEntry.arguments?.getString("handle").orEmpty()),
                        initialNickname = Uri.decode(backStackEntry.arguments?.getString("nickname").orEmpty()),
                        initialBio = Uri.decode(backStackEntry.arguments?.getString("bio").orEmpty()),
                        initialAvatar = Uri.decode(backStackEntry.arguments?.getString("avatar").orEmpty()),
                        initialBgImage = Uri.decode(backStackEntry.arguments?.getString("bg").orEmpty()),
                        handleLastUpdatedAt = Uri.decode(backStackEntry.arguments?.getString("lastUpdate").orEmpty()),
                        onBack = { navController.popBackStack() },
                        onSaveSuccess = { navController.popBackStack() }
                    )
                }

                composable(
                    route = "user_profile/{userId}",
                    arguments = listOf(navArgument("userId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val targetUid = backStackEntry.arguments?.getString("userId").orEmpty()
                    UserProfileScreen(
                        userId = targetUid,
                        currentUserId = userId,
                        onBack = { navController.popBackStack() },
                        onChatClick = { id, name, avatar ->
                            val encName = Uri.encode(name)
                            val encAvatar = Uri.encode(avatar ?: "")
                            navController.navigate("chat/$id/$encName?avatar=$encAvatar")
                        },
                        onMomentClick = { id -> navController.navigate("moment_detail/$id") },
                        onFollowClick = { uid, title, type ->
                            val encodedTitle = Uri.encode(title)
                            navController.navigate("follows/$uid/$encodedTitle/$type")
                        }
                    )
                }

                composable(Route.Settings.route) {
                    SettingsScreen(
                        onBack = { navController.popBackStack() },
                        onLogout = {
                            AuthSession.clear()
                            userPrefs.clearAuth()
                            loggedInUserId = null
                        },
                        mainViewModel = mainViewModel
                    )
                }

                composable("membership") {
                    MembershipScreen(hazeState = hazeState, onBack = { navController.popBackStack() })
                }

                composable("history") {
                    HistoryScreen(
                        hazeState = hazeState,
                        onBack = { navController.popBackStack() },
                        onMomentClick = { id -> navController.navigate("moment_detail/$id") }
                    )
                }

                composable("notifications") {
                    NotificationScreen(
                        hazeState = hazeState,
                        onBack = { navController.popBackStack() },
                        onMomentClick = { id -> navController.navigate("moment_detail/$id") },
                        onUserClick = { uid -> navController.navigate("user_profile/$uid") }
                    )
                }
            }
        }
    }
}

private fun isFullScreenRoute(route: String?): Boolean =
    route?.startsWith("moment_detail") == true ||
        route?.startsWith("chat") == true ||
        route?.startsWith("follows") == true ||
        route?.startsWith("edit_profile") == true ||
        route?.startsWith("mine_tab") == true ||
        route?.startsWith("user_profile") == true ||
        route == "membership" ||
        route == "history" ||
        route == "notifications" ||
        route == Route.Settings.route ||
        route == Route.Post.route ||
        route == Route.Search.route

@Composable
fun BottomNavigation(hazeState: HazeState, navController: NavController, items: List<Route>) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val primaryColor = MaterialTheme.colorScheme.primary
    val baseColor = MaterialTheme.colorScheme.onSurfaceVariant
    val backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
    val dividerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .hazeEffect(state = hazeState, style = HazeMaterials.ultraThin())
            .background(backgroundColor)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
    ) {
        Column {
            HorizontalDivider(thickness = 0.5.dp, color = dividerColor)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    val isPost = screen is Route.Post

                    val iconScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.15f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                    val iconColor by animateColorAsState(
                        targetValue = if (isSelected) primaryColor else baseColor,
                        animationSpec = tween(150)
                    )

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                    ) {
                        if (isPost) {
                            Surface(
                                modifier = Modifier.size(40.dp),
                                shape = androidx.compose.foundation.shape.CircleShape,
                                color = primaryColor,
                                shadowElevation = 4.dp
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    AppIcon(
                                        glyph = AppIcons.Add,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        size = 22.dp
                                    )
                                }
                            }
                        } else {
                            AppIcon(
                                glyph = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                tint = iconColor,
                                modifier = Modifier.scale(iconScale),
                                size = 24.dp
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.navigationBarsPadding())
        }
    }
}

@Composable
fun TopBar(
    hazeState: HazeState,
    currentRoute: String?,
    unreadNotificationCount: Int,
    onSearchClick: () -> Unit,
    onNotificationClick: () -> Unit
) {
    val itemsList = listOf(Route.Home, Route.Explore, Route.Post, Route.Message, Route.Mine)
    val backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
    val dividerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    val contentColor = MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .hazeEffect(state = hazeState, style = HazeMaterials.ultraThin())
            .background(backgroundColor)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            when (currentRoute) {
                Route.Home.route -> Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("来声", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = contentColor)
                    Box(contentAlignment = Alignment.TopEnd) {
                        IconButton(onClick = onNotificationClick) {
                            AppIcon(glyph = AppIcons.Bell, tint = contentColor, size = 22.dp)
                        }
                        if (unreadNotificationCount > 0) {
                            Surface(
                                shape = androidx.compose.foundation.shape.CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 10.dp, end = 8.dp)
                            ) {
                                Text(
                                    text = unreadNotificationCount.coerceAtMost(99).toString(),
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Route.Explore.route -> Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("探索", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = contentColor)
                    IconButton(onClick = onSearchClick) {
                        AppIcon(glyph = AppIcons.Search, tint = contentColor, size = 22.dp)
                    }
                }

                else -> {
                    val title = itemsList.find { it.route == currentRoute }?.title ?: "来声"
                    Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = contentColor)
                }
            }
        }
        HorizontalDivider(thickness = 0.5.dp, color = dividerColor)
    }
}
