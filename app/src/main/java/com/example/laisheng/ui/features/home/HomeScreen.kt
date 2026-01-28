package com.example.laisheng.ui.features.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import androidx.lifecycle.viewmodel.compose.viewModel // 需要这个依赖，或者直接传参

@Composable
fun HomeScreen(
    hazeState: HazeState,
    viewModel: HomeViewModel = viewModel() // 获取 ViewModel
) {
    val quote = viewModel.quoteState.value

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .hazeSource(state = hazeState),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Text(text = "今日名言", style = MaterialTheme.typography.headlineMedium)
        }

        item {
            if (quote != null) {
                Card(modifier = Modifier.padding(top = 16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = quote.quote)
                        Text(
                            text = "- ${quote.author}",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            } else {
                Text("加载中...")
            }
        }

        // 添加刷新按钮
        item {
            Button(onClick = { viewModel.fetchQuote() }) {
                Text("换一句")
            }
        }
    }
}