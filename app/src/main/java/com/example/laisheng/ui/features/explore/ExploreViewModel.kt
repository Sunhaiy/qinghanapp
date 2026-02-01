package com.example.laisheng.ui.features.explore

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laisheng.data.NetworkModule
import com.example.laisheng.data.model.Moment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExploreViewModel : ViewModel() {

    private val _moments = MutableStateFlow<List<Moment>>(emptyList())
    val moments: StateFlow<List<Moment>> = _moments.asStateFlow()

    init {
        fetchMoments()
    }

    private fun fetchMoments() {
        viewModelScope.launch {
            try {
                // 现在 apiService.getMoments() 返回的是 MomentResponse 对象
                val response = NetworkModule.apiService.getMoments()

                // 取出其中的 data 列表赋值给 UI 状态
                _moments.value = response.data

            } catch (e: Exception) {
                Log.e("ExploreViewModel", "Error fetching moments", e)
            }
        }
    }
}