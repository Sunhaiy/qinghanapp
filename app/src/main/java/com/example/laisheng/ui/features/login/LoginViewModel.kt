package com.example.laisheng.ui.features.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laisheng.data.NetworkModule
import com.example.laisheng.data.model.User
import com.example.laisheng.data.repository.MomentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val user: User) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

class LoginViewModel : ViewModel() {
    private val repository = MomentRepository(NetworkModule.apiService)

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun login(handle: String) {
        if (handle.isBlank()) {
            _uiState.value = LoginUiState.Error("请输入 Handle")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            val user = repository.login(handle)
            if (user != null) {
                _uiState.value = LoginUiState.Success(user)
            } else {
                _uiState.value = LoginUiState.Error("登录失败，用户不存在")
            }
        }
    }
}