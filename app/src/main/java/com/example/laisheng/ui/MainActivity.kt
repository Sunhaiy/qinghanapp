package com.example.laisheng.ui

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.composables.icons.lucide.*
import com.example.laisheng.data.remote.SocketManager
import com.example.laisheng.navigation.Route
import com.example.laisheng.ui.features.detail.MomentDetailScreen
import com.example.laisheng.ui.features.explore.ExploreScreen
import com.example.laisheng.ui.features.home.HomeScreen
import com.example.laisheng.ui.features.login.LoginScreen
import com.example.laisheng.ui.features.message.ChatDetailScreen
import com.example.laisheng.ui.features.message.MessageScreen
import com.example.laisheng.ui.features.mine.MineScreen
import com.example.laisheng.ui.features.mine.edit.EditProfileScreen
import com.example.laisheng.ui.features.mine.follows.FollowScreen
import com.example.laisheng.ui.features.post.PostScreen
import com.example.laisheng.ui.theme.LaishengTheme
import com.example.laisheng.util.UserPrefs
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { LaishengTheme { Laisheng() } }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Laisheng() {
    val context = LocalContext.current
    val userPrefs = remember { UserPrefs(context) }
    var loggedInUserId by remember { mutableStateOf(userPrefs.getUserId()) }

    LaunchedEffect(loggedInUserId) {
        loggedInUserId?.let { id -> SocketManager.connect(id) }
    }

    DisposableEffect(Unit) {
        onDispose { SocketManager.disconnect() }
    }

    if (loggedInUserId == null) {
        LoginScreen(onLoginSuccess = { userId ->
            userPrefs.saveUserId(userId)
            loggedInUserId = userId
        })
    } else {
        val userId = loggedInUserId ?: ""
        val hazeState = rememberHazeState()
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        var showPostDialog by remember { mutableStateOf(false) }

        if (showPostDialog) {
            Dialog(onDismissRequest = { showPostDialog = false }) {
                PostScreen(userId = userId, onCancel = { showPostDialog = false }, onPostSuccess = { showPostDialog = false })
            }
        }

        val items = listOf(Route.Home, Route.Explore, Route.Post, Route.Message, Route.Mine)

        Scaffold(
            topBar = {
                val isFullScreen = currentRoute?.startsWith("moment_detail") == true || 
                                 currentRoute?.startsWith("chat") == true ||
                                 currentRoute?.startsWith("follows") == true ||
                                 currentRoute?.startsWith("edit_profile") == true
                if (!isFullScreen) TopBar(hazeState, currentRoute)
            },
            bottomBar = {
                val isFullScreen = currentRoute?.startsWith("moment_detail") == true || 
                                 currentRoute?.startsWith("chat") == true ||
                                 currentRoute?.startsWith("follows") == true ||
                                 currentRoute?.startsWith("edit_profile") == true
                if (!isFullScreen) BottomNavigation(hazeState, navController, items) { showPostDialog = true }
            }
        ) { paddingValues ->
            SharedTransitionLayout {
                NavHost(navController = navController, startDestination = Route.Home.route) {
                    composable(Route.Home.route) { HomeScreen(hazeState, paddingValues) }
                    composable(Route.Explore.route) {
                        ExploreScreen(hazeState, paddingValues, userId, { id -> navController.navigate("moment_detail/$id") }, this@SharedTransitionLayout, this@composable)
                    }
                    composable(Route.Message.route) {
                        MessageScreen(hazeState, userId, paddingValues, { id, name, avatar ->
                            val encName = Uri.encode(name); val encAv = Uri.encode(avatar ?: "")
                            navController.navigate("chat/$id/$encName?avatar=$encAv")
                        })
                    }
                    composable(Route.Mine.route) {
                        MineScreen(
                            hazeState = hazeState, 
                            userId = userId, 
                            paddingValues = paddingValues, 
                            onFollowClick = { uid, title, type -> navController.navigate("follows/$uid/$title/$type") }, 
                            onEditClick = { nick, handle, bio, av, bg ->
                                val eN = Uri.encode(nick); val eH = Uri.encode(handle)
                                val eB = Uri.encode(bio ?: ""); val eA = Uri.encode(av ?: ""); val eG = Uri.encode(bg ?: "")
                                navController.navigate("edit_profile/$userId/$eH/$eN/$eB?avatar=$eA&bg=$eG")
                            }, 
                            onMomentClick = { id -> navController.navigate("moment_detail/$id") }
                        )
                    }
                    composable("moment_detail/{momentId}", listOf(navArgument("momentId") { type = NavType.StringType })) { backStackEntry ->
                        val id = backStackEntry.arguments?.getString("momentId") ?: ""
                        MomentDetailScreen(id, userId, { navController.popBackStack() }, this@SharedTransitionLayout, this@composable)
                    }
                    composable("chat/{otherId}/{nickname}?avatar={avatar}", listOf(
                        navArgument("otherId") { type = NavType.StringType },
                        navArgument("nickname") { type = NavType.StringType },
                        navArgument("avatar") { type = NavType.StringType; nullable = true; defaultValue = "" }
                    )) { backStackEntry ->
                        val id = backStackEntry.arguments?.getString("otherId") ?: ""
                        val name = Uri.decode(backStackEntry.arguments?.getString("nickname") ?: "聊天")
                        val av = Uri.decode(backStackEntry.arguments?.getString("avatar") ?: "")
                        ChatDetailScreen(userId, id, name, av, { navController.popBackStack() })
                    }
                    composable("follows/{userId}/{title}/{type}", listOf(
                        navArgument("userId") { type = NavType.StringType },
                        navArgument("title") { type = NavType.StringType },
                        navArgument("type") { type = NavType.StringType }
                    )) { backStackEntry ->
                        val uid = backStackEntry.arguments?.getString("userId") ?: ""
                        val tit = backStackEntry.arguments?.getString("title") ?: "列表"
                        val typ = backStackEntry.arguments?.getString("type") ?: ""
                        FollowScreen(uid, tit, typ, { navController.popBackStack() })
                    }
                    composable("edit_profile/{userId}/{handle}/{nickname}/{bio}?avatar={avatar}&bg={bg}", listOf(
                        navArgument("userId") { type = NavType.StringType },
                        navArgument("handle") { type = NavType.StringType },
                        navArgument("nickname") { type = NavType.StringType },
                        navArgument("bio") { type = NavType.StringType },
                        navArgument("avatar") { type = NavType.StringType; nullable = true },
                        navArgument("bg") { type = NavType.StringType; nullable = true }
                    )) { backStackEntry ->
                        EditProfileScreen(
                            userId = backStackEntry.arguments?.getString("userId") ?: "",
                            handle = Uri.decode(backStackEntry.arguments?.getString("handle") ?: ""),
                            initialNickname = Uri.decode(backStackEntry.arguments?.getString("nickname") ?: ""),
                            initialBio = Uri.decode(backStackEntry.arguments?.getString("bio") ?: ""),
                            initialAvatar = Uri.decode(backStackEntry.arguments?.getString("avatar") ?: ""),
                            initialBgImage = Uri.decode(backStackEntry.arguments?.getString("bg") ?: ""),
                            onBack = { navController.popBackStack() },
                            onSaveSuccess = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavigation(hazeState: HazeState, navController: NavController, items: List<Route>, onPostClick: () -> Unit) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val primaryColor = MaterialTheme.colorScheme.primary
    val baseColor = Color(0xFF444444)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .hazeEffect(state = hazeState, style = HazeMaterials.ultraThin())
            .background(Color.White.copy(alpha = 0.8f))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
    ) {
        Column {
            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
            Row(
                modifier = Modifier.fillMaxWidth().height(60.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    val isPost = screen is Route.Post
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()

                    // 利落缩放动画
                    val animatedScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.12f else 1f,
                        animationSpec = if (isSelected) keyframes {
                            durationMillis = 150
                            1.3f at 50
                            1.12f at 150
                        } else tween(100)
                    )

                    // 核心：对角线填充进度
                    val fillFraction by animateFloatAsState(
                        targetValue = if (isSelected) 1f else 0f,
                        animationSpec = tween(300, easing = FastOutSlowInEasing)
                    )

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.weight(1f).clickable(interactionSource = interactionSource, indication = null) {
                            if (isPost) onPostClick() else navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true; restoreState = true
                            }
                        }
                    ) {
                        if (isPost) {
                            val postScale by animateFloatAsState(if (isPressed) 0.9f else 1f)
                            Surface(
                                modifier = Modifier.size(42.dp).scale(postScale).border(0.5.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                                shape = CircleShape, color = primaryColor, shadowElevation = 10.dp
                            ) {
                                Icon(imageVector = Lucide.Plus, contentDescription = null, tint = Color.White, modifier = Modifier.padding(10.dp))
                            }
                        } else {
                            val (unselectedIcon, selectedIcon) = when(screen) {
                                Route.Home -> Lucide.House to Lucide.House
                                Route.Explore -> Lucide.Compass to Lucide.Compass
                                Route.Message -> Lucide.MessageCircle to Lucide.MessageCircle
                                Route.Mine -> Lucide.User to Lucide.User
                                else -> Lucide.Circle to Lucide.Circle
                            }

                            // 这里的 Lucide 图标通常是线框的。为了实现“填充”效果，
                            // 我们在选中状态下通过背景色和 BlendMode 来模拟填充感。
                            // 但用户想要真正的“Filled”图标。Lucide 库本身大部分是线框风格。
                            // 如果要实现真正的填充动画，我们需要在底层放一个 Outlined 图标，
                            // 在顶层放一个被裁剪的 Filled 图标。
                            
                            Box(contentAlignment = Alignment.Center) {
                                // 底层线框图标
                                Icon(
                                    imageVector = screen.unselectedIcon,
                                    contentDescription = null,
                                    tint = baseColor,
                                    modifier = Modifier.size(24.dp).scale(animatedScale)
                                )
                                
                                // 顶层填充图标（带动画裁剪）
                                Icon(
                                    imageVector = screen.selectedIcon,
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .scale(animatedScale)
                                        .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                                        .drawWithCache {
                                            onDrawWithContent {
                                                if (fillFraction > 0f) {
                                                    // 蒙版式对角线填充裁剪
                                                    val brush = Brush.linearGradient(
                                                        0.0f to Color.Black,
                                                        fillFraction to Color.Black,
                                                        fillFraction + 0.01f to Color.Transparent,
                                                        start = Offset(0f, size.height),
                                                        end = Offset(size.width, 0f)
                                                    )
                                                    drawContent()
                                                    drawRect(brush, blendMode = BlendMode.DstIn)
                                                }
                                            }
                                        }
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.navigationBarsPadding())
        }
    }
}

@Composable
fun TopBar(hazeState: HazeState, currentRoute: String?) {
    val itemsList = listOf(Route.Home, Route.Explore, Route.Post, Route.Message, Route.Mine)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .hazeEffect(state = hazeState, style = HazeMaterials.ultraThin())
            .background(Color.White.copy(alpha = 0.8f))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().height(56.dp).padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            when (currentRoute) {
                Route.Home.route -> Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "来声", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                    IconButton(onClick = {}) { Icon(imageVector = Lucide.Bell, contentDescription = null) }
                }
                Route.Explore.route -> Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "探索", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                    IconButton(onClick = {}) { Icon(imageVector = Lucide.Search, contentDescription = null) }
                }
                else -> {
                    val title = itemsList.find { it.route == currentRoute }?.title ?: "来声"
                    Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
        HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
    }
}
