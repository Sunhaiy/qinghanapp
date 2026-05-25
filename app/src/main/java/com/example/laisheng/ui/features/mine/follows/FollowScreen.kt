package com.example.laisheng.ui.features.mine.follows

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.laisheng.ui.components.LaishengLoading
import com.example.laisheng.ui.components.UserItem
import com.example.laisheng.ui.theme.AppIcon
import com.example.laisheng.ui.theme.AppIcons
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.HazeMaterials

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowScreen(
    userId: String,
    title: String,
    type: String,
    onBack: () -> Unit,
    onUserClick: (String) -> Unit,
    viewModel: FollowViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val hazeState = remember { HazeState() }
    var keyword by remember { mutableStateOf("") }

    LaunchedEffect(userId, type) {
        when (type) {
            "self_followers" -> viewModel.loadSelfFollowers()
            "self_following" -> viewModel.loadSelfFollowing()
            "followers" -> viewModel.loadFollowers(userId)
            "following" -> viewModel.loadFollowing(userId)
            "mutual" -> viewModel.loadMutual(userId)
        }
    }

    Scaffold(
        topBar = {
            FollowTopBar(hazeState = hazeState, title = title, onBack = onBack)
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    when (type) {
                        "self_followers" -> viewModel.loadSelfFollowers()
                        "self_following" -> viewModel.loadSelfFollowing()
                        "followers" -> viewModel.loadFollowers(userId)
                        "following" -> viewModel.loadFollowing(userId)
                        "mutual" -> viewModel.loadMutual(userId)
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) {
                when (val state = uiState) {
                    is FollowUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            LaishengLoading()
                        }
                    }

                    is FollowUiState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(state.message, color = MaterialTheme.colorScheme.error)
                        }
                    }

                    is FollowUiState.Success -> {
                        val filteredUsers = state.users.filter { user ->
                            keyword.isBlank() ||
                                user.nickname.contains(keyword, ignoreCase = true) ||
                                user.handle.contains(keyword, ignoreCase = true)
                        }

                        if (state.users.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("暂无数据", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                item {
                                    OutlinedTextField(
                                        value = keyword,
                                        onValueChange = { keyword = it },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        placeholder = { Text("搜索昵称或账号") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(22.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                                        )
                                    )
                                }

                                items(filteredUsers, key = { it.id }) { user ->
                                    UserItem(user = user, onUserClick = { onUserClick(user.id) })
                                }

                                if (filteredUsers.isEmpty()) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 72.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("没有匹配结果", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FollowTopBar(
    hazeState: HazeState,
    title: String,
    onBack: () -> Unit
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .hazeEffect(state = hazeState, style = HazeMaterials.ultraThin())
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.84f))
            .padding(top = 40.dp, start = 12.dp, end = 12.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
        ) {
            IconButton(onClick = onBack) {
                AppIcon(glyph = AppIcons.ArrowLeft, tint = MaterialTheme.colorScheme.onSurface, size = 20.dp)
            }
        }
        Text(
            text = title,
            modifier = Modifier.padding(start = 10.dp),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            fontSize = 22.sp
        )
    }
}
