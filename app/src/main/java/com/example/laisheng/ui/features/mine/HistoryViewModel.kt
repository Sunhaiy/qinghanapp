package com.example.laisheng.ui.features.mine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laisheng.data.model.HistoryItem
import com.example.laisheng.data.model.MembershipStatus
import com.example.laisheng.data.remote.NetworkModule
import com.example.laisheng.data.repository.MomentRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HistoryUiState(
    val loading: Boolean = true,
    val membership: MembershipStatus? = null,
    val items: List<HistoryItem> = emptyList(),
    val error: String? = null
)

class HistoryViewModel : ViewModel() {
    private val repository = MomentRepository(NetworkModule.apiService)

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState = _uiState.asStateFlow()

    private val _message = MutableSharedFlow<String>()
    val message = _message.asSharedFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            try {
                val membership = repository.getMembershipStatus()
                val limit = membership?.maxPageSize ?: 20
                val history = repository.getHistory(limit = limit)
                _uiState.value = HistoryUiState(
                    loading = false,
                    membership = membership,
                    items = history?.data.orEmpty()
                )
            } catch (e: Exception) {
                _uiState.value = HistoryUiState(
                    loading = false,
                    error = e.message ?: "加载浏览记录失败"
                )
            }
        }
    }

    fun delete(momentId: String) {
        viewModelScope.launch {
            if (repository.deleteHistory(momentId)) {
                _uiState.value = _uiState.value.copy(
                    items = _uiState.value.items.filterNot { it.momentId == momentId }
                )
                _message.emit("已删除这条记录")
            } else {
                _message.emit("删除失败")
            }
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            if (repository.clearHistory()) {
                _uiState.value = _uiState.value.copy(items = emptyList())
                _message.emit("浏览记录已清空")
            } else {
                _message.emit("清空失败")
            }
        }
    }
}
