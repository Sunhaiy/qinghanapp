package com.example.laisheng.ui.features.home

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laisheng.data.NetworkModule
import com.example.laisheng.data.model.Quote
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    // 简单的状态管理
    private val _quoteState = mutableStateOf<Quote?>(null)
    val quoteState: State<Quote?> = _quoteState

    init {
        fetchQuote()
    }

    fun fetchQuote() {
        viewModelScope.launch {
            try {
                // 调用网络请求
                val quote = NetworkModule.apiService.getRandomQuote()
                _quoteState.value = quote
            } catch (e: Exception) {
                e.printStackTrace()
                _quoteState.value = Quote("加载失败: ${e.message}", "系统")
            }
        }
    }
}