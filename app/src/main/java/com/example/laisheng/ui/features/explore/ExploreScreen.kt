package com.example.laisheng.ui.features.explore

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.laisheng.ui.composes.PostCard
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

@Composable
fun ExploreScreen(
    hazeState: HazeState,
    paddingValues: PaddingValues,
    viewModel: ExploreViewModel = viewModel()
) {
    // 1. 改名观察 moments
    val moments by viewModel.moments.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .hazeSource(state = hazeState)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = paddingValues
        ) {
            // 2. 传入 moment 数据
            items(moments) { moment ->
                PostCard(moment = moment)
            }
        }
    }
}