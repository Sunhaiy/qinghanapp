package com.example.laisheng.ui.features.mine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laisheng.data.model.MembershipOrder
import com.example.laisheng.data.model.MembershipPlan
import com.example.laisheng.data.model.MembershipStatus
import com.example.laisheng.data.remote.NetworkModule
import com.example.laisheng.data.repository.MomentRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MembershipUiState(
    val loading: Boolean = true,
    val activatingPlanCode: String? = null,
    val status: MembershipStatus? = null,
    val plans: List<MembershipPlan> = emptyList(),
    val orders: List<MembershipOrder> = emptyList(),
    val error: String? = null
)

class MembershipViewModel : ViewModel() {
    private val repository = MomentRepository(NetworkModule.apiService)

    private val _uiState = MutableStateFlow(MembershipUiState())
    val uiState = _uiState.asStateFlow()

    private val _message = MutableSharedFlow<String>()
    val message = _message.asSharedFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            try {
                _uiState.value = MembershipUiState(
                    loading = false,
                    status = repository.getMembershipStatus(),
                    plans = repository.getMembershipPlans(),
                    orders = repository.getMembershipOrders()
                )
            } catch (e: Exception) {
                _uiState.value = MembershipUiState(
                    loading = false,
                    error = e.message ?: "加载会员信息失败"
                )
            }
        }
    }

    fun activate(planCode: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(activatingPlanCode = planCode)
            val status = repository.activateMembership(planCode)
            if (status != null) {
                _message.emit("会员状态已更新")
                load()
            } else {
                _uiState.value = _uiState.value.copy(activatingPlanCode = null)
                _message.emit("开通会员失败")
            }
        }
    }
}
