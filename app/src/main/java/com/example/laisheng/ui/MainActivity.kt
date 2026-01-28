package com.example.laisheng.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.laisheng.navigation.Route
import com.example.laisheng.ui.features.explore.ExploreScreen
import com.example.laisheng.ui.features.home.HomeScreen
import com.example.laisheng.ui.features.message.MessageScreen
import com.example.laisheng.ui.features.mine.MineScreen
import com.example.laisheng.ui.features.post.PostScreen

import com.example.laisheng.ui.theme.LaishengTheme
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
@Composable
fun Laisheng() {
    val hazeState = rememberHazeState()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // 底部导航栏显示的项
    val items = listOf(
        Route.Home,
        Route.Explore,
        Route.Post, // 发布按钮放在中间
        Route.Message,
        Route.Mine
    )

    Scaffold(
        topBar = { 
            TopBar(
                hazeState = hazeState,
                currentRoute = currentRoute
            ) 
        },
        bottomBar = {
            BottomNavigation(
                hazeState = hazeState,
                navController = navController,
                items = items
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding()) {
            NavHost(
                navController = navController,
                startDestination = Route.Home.route
            ) {
                composable(Route.Home.route) {
                    HomeScreen(hazeState)
                }
                composable(Route.Explore.route) {
                    ExploreScreen(hazeState)
                }
                composable(Route.Post.route) {
                    PostScreen(hazeState)
                }
                composable(Route.Message.route) {
                    MessageScreen(hazeState)
                }
                composable(Route.Mine.route) {
                    MineScreen(hazeState)
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
    items: List<Route>
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
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
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                ) {
                    Icon(
                        imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                        contentDescription = screen.title,
                        tint = if (isSelected) Color.Black else Color.Gray,
                        modifier = if (isPostButton) Modifier.size(36.dp) else Modifier.size(24.dp) // 发布按钮稍微大一点
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
            Route.Post.route -> {
                Text(
                    text = "发布",
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
