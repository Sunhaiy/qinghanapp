package com.example.laisheng.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.laisheng.navigation.Route
import com.example.laisheng.ui.features.detail.MomentDetailScreen
import com.example.laisheng.ui.features.explore.ExploreScreen
import com.example.laisheng.ui.features.home.HomeScreen
import com.example.laisheng.ui.features.login.LoginScreen
import com.example.laisheng.ui.features.message.MessageScreen
import com.example.laisheng.ui.features.mine.MineScreen
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
        setContent {
            LaishengTheme {
                Laisheng()
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Laisheng() {
    val context = LocalContext.current
    val userPrefs = remember { UserPrefs(context) }
    
    var loggedInUserId by remember { mutableStateOf(userPrefs.getUserId()) }

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
                PostScreen(
                    userId = userId,
                    onCancel = { showPostDialog = false },
                    onPostSuccess = { showPostDialog = false }
                )
            }
        }

        val items = listOf(
            Route.Home,
            Route.Explore,
            Route.Post,
            Route.Message,
            Route.Mine
        )

        Scaffold(
            topBar = {
                if (currentRoute != "moment_detail/{momentId}") {
                    TopBar(
                        hazeState = hazeState,
                        currentRoute = currentRoute
                    )
                }
            },
            bottomBar = {
                if (currentRoute != "moment_detail/{momentId}") {
                    BottomNavigation(
                        hazeState = hazeState,
                        navController = navController,
                        items = items,
                        onPostClick = { showPostDialog = true }
                    )
                }
            }
        ) { paddingValues ->
            // 使用 SharedTransitionLayout 开启共享元素过渡
            SharedTransitionLayout {
                NavHost(
                    navController = navController,
                    startDestination = Route.Home.route
                ) {
                    composable(Route.Home.route) {
                        HomeScreen(hazeState, paddingValues)
                    }
                    composable(Route.Explore.route) {
                        ExploreScreen(
                            hazeState = hazeState,
                            paddingValues = paddingValues,
                            userId = userId,
                            onMomentClick = { momentId ->
                                navController.navigate("moment_detail/$momentId")
                            },
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedVisibilityScope = this@composable
                        )
                    }
                    composable(Route.Message.route) {
                        Box(modifier = Modifier.padding(paddingValues)) {
                            MessageScreen(hazeState)
                        }
                    }
                    composable(Route.Mine.route) {
                        MineScreen(
                            hazeState = hazeState,
                            userId = userId,
                            paddingValues = paddingValues
                        )
                    }
                    
                    composable(
                        route = "moment_detail/{momentId}",
                        arguments = listOf(navArgument("momentId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val momentId = backStackEntry.arguments?.getString("momentId") ?: ""
                        MomentDetailScreen(
                            momentId = momentId,
                            userId = userId,
                            onBack = { navController.popBackStack() },
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedVisibilityScope = this@composable
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavigation(
    modifier: Modifier = Modifier,
    hazeState: HazeState,
    navController: NavController,
    items: List<Route>,
    onPostClick: () -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
            .hazeEffect(state = hazeState, style = HazeMaterials.ultraThin())
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { screen ->
                val isSelected = currentRoute == screen.route
                val isPostButton = screen is Route.Post

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            if (isPostButton) {
                                onPostClick()
                            } else {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                ) {
                    Icon(
                        imageVector = if (isSelected && !isPostButton) screen.selectedIcon else screen.unselectedIcon,
                        contentDescription = screen.title,
                        tint = if (isSelected && !isPostButton) Color.Black else Color.Gray,
                        modifier = if (isPostButton) Modifier.size(36.dp) else Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TopBar(
    modifier: Modifier = Modifier,
    hazeState: HazeState,
    currentRoute: String?
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
            .hazeEffect(state = hazeState, style = HazeMaterials.ultraThin())
            .statusBarsPadding()
            .height(56.dp)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        when (currentRoute) {
            Route.Home.route -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "来声",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = Color.Black
                    )
                }
            }
            Route.Explore.route -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "探索",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.Black
                    )
                }
            }
            Route.Message.route -> {
                Text(
                    text = "消息",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
            Route.Mine.route -> {
                Text(
                    text = "我的",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
            else -> {
                Text(
                    text = "来声",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}
