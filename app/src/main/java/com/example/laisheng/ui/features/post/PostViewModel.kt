package com.example.laisheng.ui.features.post

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laisheng.data.NetworkModule
import com.example.laisheng.data.model.Attachment
import com.example.laisheng.data.model.CreateMomentRequest
import com.example.laisheng.data.model.MomentContent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

sealed class PostUiState {
    object Idle : PostUiState()
    object Loading : PostUiState()
    object Success : PostUiState()
    data class Error(val message: String) : PostUiState()
}

class PostViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<PostUiState>(PostUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun createMoment(userId: String, contentText: String, imageUri: Uri?, context: Context) {
        viewModelScope.launch {
            _uiState.value = PostUiState.Loading
            try {
                var uploadedUrl: String? = null

                // 1. 如果有图片，先上传
                imageUri?.let { uri ->
                    val file = uriToFile(context, uri)
                    if (file != null) {
                        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                        val uploadResponse = NetworkModule.apiService.uploadFile(body)
                        uploadedUrl = uploadResponse.url
                    }
                }

                // 2. 构造符合 JSONB 结构的 content 对象
                val attachments = if (uploadedUrl != null) {
                    listOf(Attachment(type = "image", url = uploadedUrl!!))
                } else null

                val content = MomentContent(
                    text = contentText,
                    type = if (uploadedUrl != null) "image" else "text",
                    attachments = attachments
                )

                // 3. 调用发布接口
                NetworkModule.apiService.createMoment(
                    CreateMomentRequest(userId = userId, content = content)
                )

                _uiState.value = PostUiState.Success
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = PostUiState.Error("发布失败: ${e.message}")
            }
        }
    }

    private fun uriToFile(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val file = File(context.cacheDir, "upload_image_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            file
        } catch (e: Exception) {
            null
        }
    }

    fun resetState() {
        _uiState.value = PostUiState.Idle
    }
}