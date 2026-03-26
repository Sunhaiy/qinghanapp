package com.example.laisheng.ui.features.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.laisheng.data.local.UserPrefs
import com.example.laisheng.data.model.User
import com.example.laisheng.data.model.toUserOrNull
import com.example.laisheng.data.remote.AuthSession
import com.example.laisheng.data.remote.NetworkModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val user: User) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val userPrefs = UserPrefs(application)

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun login(handle: String, password: String) {
        submitAuthRequest(
            request = mapOf(
                "handle" to handle.trim().removePrefix("@"),
                "password" to password
            ),
            isRegister = false
        )
    }

    fun register(handle: String, password: String, nickname: String, avatar: String) {
        submitAuthRequest(
            request = mapOf(
                "handle" to handle.trim().removePrefix("@"),
                "password" to password,
                "nickname" to nickname,
                "avatar" to avatar
            ),
            isRegister = true
        )
    }

    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }

    private fun submitAuthRequest(
        request: Map<String, String>,
        isRegister: Boolean
    ) {
        val handle = request["handle"].orEmpty()
        val password = request["password"].orEmpty()
        val nickname = request["nickname"].orEmpty()

        if (handle.isBlank() || password.isBlank() || (isRegister && nickname.isBlank())) {
            _uiState.value = LoginUiState.Error(
                if (isRegister) "请填写完整注册信息" else "请输入账号和密码"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            try {
                val response = if (isRegister) {
                    NetworkModule.apiService.register(request)
                } else {
                    NetworkModule.apiService.login(request)
                }
                val token = response.token
                val user = response.user ?: response.toUserOrNull()
                if (token.isNullOrBlank() || user == null) {
                    _uiState.value = LoginUiState.Error("服务端返回的登录信息不完整")
                    return@launch
                }

                AuthSession.updateToken(token)
                userPrefs.saveAuth(token, user)
                _uiState.value = LoginUiState.Success(user)
            } catch (e: HttpException) {
                _uiState.value = LoginUiState.Error(
                    if (isRegister) "注册失败，请检查 handle 是否已存在" else "登录失败，请检查账号或密码"
                )
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error(
                    e.message ?: if (isRegister) "注册失败" else "登录失败"
                )
            }
        }
    }
}
