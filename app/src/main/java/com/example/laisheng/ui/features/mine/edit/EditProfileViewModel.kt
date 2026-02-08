package com.example.laisheng.ui.features.mine.edit

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laisheng.data.NetworkModule
import com.example.laisheng.data.repository.MomentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

sealed class EditUiState {
    object Idle : EditUiState()
    object Loading : EditUiState()
    object Success : EditUiState()
    data class Error(val message: String) : EditUiState()
}

class EditProfileViewModel : ViewModel() {
    private val repository = MomentRepository(NetworkModule.apiService)

    private val _uiState = MutableStateFlow<EditUiState>(EditUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun updateProfile(
        userId: String,
        nickname: String,
        bio: String,
        newAvatarUri: Uri?,
        newBgUri: Uri?,
        currentAvatar: String?, // 传入当前头像作为兜底
        currentBg: String?,     // 传入当前背景作为兜底
        context: Context
    ) {
        viewModelScope.launch {
            _uiState.value = EditUiState.Loading
            try {
                val updates = mutableMapOf<String, String?>()
                updates["nickname"] = nickname
                updates["bio"] = bio
                
                // 默认使用当前已有的 URL，防止被后端清空
                updates["avatar"] = currentAvatar
                updates["bg_image"] = currentBg

                // 1. 如果选了新头像，上传并覆盖
                newAvatarUri?.let { uri ->
                    val file = uriToFile(context, uri, "avatar")
                    file?.let {
                        val body = MultipartBody.Part.createFormData("file", it.name, it.asRequestBody("image/*".toMediaTypeOrNull()))
                        val res = repository.uploadFile(body)
                        if (res != null) updates["avatar"] = res.url
                    }
                }

                // 2. 如果选了新背景，上传并覆盖
                newBgUri?.let { uri ->
                    val file = uriToFile(context, uri, "bg")
                    file?.let {
                        val body = MultipartBody.Part.createFormData("file", it.name, it.asRequestBody("image/*".toMediaTypeOrNull()))
                        val res = repository.uploadFile(body)
                        if (res != null) updates["bg_image"] = res.url
                    }
                }

                // 3. 提交完整更新
                repository.updateProfile(userId, updates)
                _uiState.value = EditUiState.Success
            } catch (e: Exception) {
                _uiState.value = EditUiState.Error(e.message ?: "更新失败")
            }
        }
    }

    private fun uriToFile(context: Context, uri: Uri, prefix: String): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val file = File(context.cacheDir, "${prefix}_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            file
        } catch (e: Exception) { null }
    }

    fun resetState() { _uiState.value = EditUiState.Idle }
}