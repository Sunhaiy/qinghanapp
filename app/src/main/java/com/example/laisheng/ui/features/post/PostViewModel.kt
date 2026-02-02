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

    fun createMoment(
        userId: String,
        contentText: String,
        imageUris: List<Uri>,
        voiceUri: Uri?,
        voiceDuration: Int,
        context: Context
    ) {
        viewModelScope.launch {
            _uiState.value = PostUiState.Loading
            try {
                val attachments = mutableListOf<Attachment>()

                // 1. 循环上传所有图片
                imageUris.forEach { uri ->
                    val file = uriToFile(context, uri, "image")
                    if (file != null) {
                        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                        val uploadResponse = NetworkModule.apiService.uploadFile(body)
                        attachments.add(Attachment(type = "image", url = uploadResponse.url))
                    }
                }

                // 2. 上传音频
                voiceUri?.let { uri ->
                    val file = uriToFile(context, uri, "audio")
                    if (file != null) {
                        val requestFile = file.asRequestBody("audio/*".toMediaTypeOrNull())
                        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                        val uploadResponse = NetworkModule.apiService.uploadFile(body)
                        attachments.add(
                            Attachment(
                                type = "voice",
                                url = uploadResponse.url,
                                duration = voiceDuration
                            )
                        )
                    }
                }

                // 3. 构造 content 对象并判定类型
                val type = when {
                    attachments.any { it.type == "image" } && attachments.any { it.type == "voice" } -> "mixed"
                    attachments.any { it.type == "voice" } -> "voice"
                    attachments.any { it.type == "image" } -> "image"
                    else -> "text"
                }

                val content = MomentContent(
                    text = contentText,
                    type = type,
                    attachments = if (attachments.isNotEmpty()) attachments else null
                )

                // 4. 调用发布接口
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

    private fun uriToFile(context: Context, uri: Uri, prefix: String): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val extension = if (prefix == "image") "jpg" else "mp3"
            val file = File(context.cacheDir, "${prefix}_${System.currentTimeMillis()}.$extension")
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