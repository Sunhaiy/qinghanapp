package com.example.laisheng.ui.features.mine.follows

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.example.laisheng.data.remote.NetworkModule
import com.example.laisheng.data.model.User

import com.example.laisheng.ui.components.LaishengLoading
import com.example.laisheng.ui.components.UserItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowScreen(
    userId: String,
    title: String,
    type: String, // "followers", "following", "mutual"
    onBack: () -> Unit,
    onUserClick: (String) -> Unit, // Callback
    viewModel: FollowViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    LaunchedEffect(userId, type) {
        when (type) {
            "followers" -> viewModel.loadFollowers(userId)
            "following" -> viewModel.loadFollowing(userId)
            "mutual" -> viewModel.loadMutual(userId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { 
                    when (type) {
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
                    is FollowUiState.Success -> {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            if (state.users.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier.fillParentMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("暂无数据", color = Color.Gray)
                                    }
                                }
                            } else {
                                items(state.users) { user ->
                                    UserItem(user, onUserClick = { onUserClick(user.id) })
                                }
                            }
                        }
                    }
                    is FollowUiState.Error -> {
                        Text(state.message, color = Color.Red, modifier = Modifier.align(Alignment.Center))
                    }
                }
            }
        }
    }
}

