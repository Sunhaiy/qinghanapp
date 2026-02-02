package com.example.laisheng.ui.features.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laisheng.data.NetworkModule
import com.example.laisheng.data.model.User
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
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun login(handle: String, password: String) {
        if (handle.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState.Error("请输入 Handle 和密码")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            try {
                val response = NetworkModule.apiService.login(
                    mapOf("handle" to handle, "password" to password)
                )
                _uiState.value = LoginUiState.Success(response)
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error("登录失败：账号或密码错误")
            }
        }
    }

    fun register(handle: String, password: String, nickname: String, avatar: String) {
        if (handle.isBlank() || password.isBlank() || nickname.isBlank()) {
            _uiState.value = LoginUiState.Error("请填写完整注册信息")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            try {
                val response = NetworkModule.apiService.register(
                    mapOf(
                        "handle" to handle,
                        "password" to password,
                        "nickname" to nickname,
                        "avatar" to avatar
                    )
                )
                _uiState.value = LoginUiState.Success(response)
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error("注册失败：该 Handle 可能已被占用")
            }
        }
    }

    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}