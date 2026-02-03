package com.example.laisheng.ui

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
                        ExploreScreen(
                            hazeState = hazeState,
                            paddingValues = paddingValues,
                            userId = userId,
                            onMomentClick = { id -> navController.navigate("moment_detail/$id") },
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedVisibilityScope = this@composable
                        )
                    }

                    composable(Route.Message.route) {
                        MessageScreen(
                            hazeState = hazeState,
                            userId = userId,
                            paddingValues = paddingValues,
                            onChatClick = { id, name, avatar ->
                                val encName = Uri.encode(name)
                                val encAv = Uri.encode(avatar ?: "")
                                navController.navigate("chat/$id/$encName?avatar=$encAv")
                            }
                        )
                    }

                    composable(Route.Mine.route) {
                        MineScreen(
                            hazeState = hazeState,
                            userId = userId,
                            paddingValues = paddingValues,
                            onFollowClick = { uid, title, type -> 
                                navController.navigate("follows/$uid/$title/$type") 
                            },
                            onEditClick = { nick, handle, bio, av, bg ->
                                val eN = Uri.encode(nick)
                                val eH = Uri.encode(handle)
                                val eB = Uri.encode(bio ?: "")
                                val eA = Uri.encode(av ?: "")
                                val eG = Uri.encode(bg ?: "")
                                navController.navigate("edit_profile/$userId/$eH/$eN/$eB?avatar=$eA&bg=$eG")
                            }
                        )
                    }
                    
                    composable(
                        route = "moment_detail/{momentId}",
                        arguments = listOf(navArgument("momentId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val id = backStackEntry.arguments?.getString("momentId") ?: ""
                        MomentDetailScreen(id, userId, { navController.popBackStack() }, this@SharedTransitionLayout, this@composable)
                    }

                    composable(
                        route = "chat/{otherId}/{nickname}?avatar={avatar}",
                        arguments = listOf(
                            navArgument("otherId") { type = NavType.StringType },
                            navArgument("nickname") { type = NavType.StringType },
                            navArgument("avatar") { type = NavType.StringType; nullable = true; defaultValue = "" }
                        )
                    ) { backStackEntry ->
                        val id = backStackEntry.arguments?.getString("otherId") ?: ""
                        val name = Uri.decode(backStackEntry.arguments?.getString("nickname") ?: "")
                        val av = Uri.decode(backStackEntry.arguments?.getString("avatar") ?: "")
                        ChatDetailScreen(userId, id, name, av, { navController.popBackStack() })
                    }

                    composable(
                        route = "follows/{userId}/{title}/{type}",
                        arguments = listOf(
                            navArgument("userId") { type = NavType.StringType },
                            navArgument("title") { type = NavType.StringType },
                            navArgument("type") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val uid = backStackEntry.arguments?.getString("userId") ?: ""
                        val tit = backStackEntry.arguments?.getString("title") ?: "列表"
                        val type = backStackEntry.arguments?.getString("type") ?: "followers"
                        FollowScreen(uid, tit, type, { navController.popBackStack() })
                    }

                    composable(
                        route = "edit_profile/{userId}/{handle}/{nickname}/{bio}?avatar={avatar}&bg={bg}",
                        arguments = listOf(
                            navArgument("userId") { type = NavType.StringType },
                            navArgument("handle") { type = NavType.StringType },
                            navArgument("nickname") { type = NavType.StringType },
                            navArgument("bio") { type = NavType.StringType },
                            navArgument("avatar") { type = NavType.StringType; nullable = true },
                            navArgument("bg") { type = NavType.StringType; nullable = true }
                        )
                    ) { backStackEntry ->
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
    Box(modifier = Modifier.fillMaxWidth().height(80.dp).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = {}).hazeEffect(state = hazeState, style = HazeMaterials.ultraThin()).navigationBarsPadding()) {
        Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            items.forEach { screen ->
                val isSelected = currentRoute == screen.route; val isPost = screen is Route.Post
                Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(1f).clickable { if (isPost) onPostClick() else navController.navigate(screen.route) { popUpTo(navController.graph.findStartDestination().id) { saveState = true }; launchSingleTop = true; restoreState = true } }) {
                    Icon(imageVector = if (isSelected && !isPost) screen.selectedIcon else screen.unselectedIcon, contentDescription = screen.title, tint = if (isSelected && !isPost) Color.Black else Color.Gray, modifier = if (isPost) Modifier.size(36.dp) else Modifier.size(24.dp))
                }
            }
        }
    }
}

@Composable
fun TopBar(hazeState: HazeState, currentRoute: String?) {
    Box(modifier = Modifier.fillMaxWidth().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = {}).hazeEffect(state = hazeState, style = HazeMaterials.ultraThin()).statusBarsPadding().height(56.dp).padding(horizontal = 16.dp), contentAlignment = Alignment.CenterStart) {
        when (currentRoute) {
            Route.Home.route -> Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text("来声", fontSize = 20.sp, fontWeight = FontWeight.Bold); Icon(Icons.Default.Notifications, null) }
            Route.Explore.route -> Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text("探索", fontSize = 20.sp, fontWeight = FontWeight.Bold); Icon(Icons.Default.Search, null) }
            Route.Message.route -> Text("消息", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Route.Mine.route -> Text("我的", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            else -> Text("来声", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}
