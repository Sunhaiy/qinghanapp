package com.example.laisheng.ui.features.message

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

@Composable
fun MessageScreen(hazeState: HazeState) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .hazeSource(state = hazeState),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "消息页面")
    }
}
