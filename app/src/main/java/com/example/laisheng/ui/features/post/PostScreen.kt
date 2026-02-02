package com.example.laisheng.ui.features.post

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostScreen(
    userId: String,
    onCancel: () -> Unit,
    onPostSuccess: () -> Unit,
    viewModel: PostViewModel = viewModel()
) {
    val context = LocalContext.current
    var content by remember { mutableStateOf("") }
    val selectedImageUris = remember { mutableStateListOf<Uri>() }
    
    // 音频相关状态
    var selectedVoiceUri by remember { mutableStateOf<Uri?>(null) }
    var voiceDuration by remember { mutableStateOf(0) }

    val uiState by viewModel.uiState.collectAsState()

    // 图片选择器
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(9),
        onResult = { uris -> selectedImageUris.addAll(uris) }
    )

    // 音频选择器 (暂时使用文件选择器模拟录音结果)
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> 
            selectedVoiceUri = uri 
            voiceDuration = 10 // 模拟时长，实际开发中建议从文件元数据获取
        }
    )

    LaunchedEffect(uiState) {
        if (uiState is PostUiState.Success) {
            onPostSuccess()
            viewModel.resetState()
        }
    }

    Surface(
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onCancel) {
                    Text("取消", color = Color.Gray)
                }
                Text("发布瞬间", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Button(
                    onClick = { 
                        viewModel.createMoment(
                            userId = userId, 
                            contentText = content, 
                            imageUris = selectedImageUris.toList(), 
                            voiceUri = selectedVoiceUri,
                            voiceDuration = voiceDuration,
                            context = context
                        ) 
                    },
                    enabled = (content.isNotBlank() || selectedImageUris.isNotEmpty() || selectedVoiceUri != null) && uiState !is PostUiState.Loading,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    if (uiState is PostUiState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("发布")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                placeholder = { Text("分享此刻的情绪与故事...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 预览区域 (图片和音频)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // 图片预览
                items(selectedImageUris) { uri ->
                    Box(modifier = Modifier.size(100.dp)) {
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { selectedImageUris.remove(uri) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(20.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(12.dp))
                        }
                    }
                }

                // 音频预览
                selectedVoiceUri?.let {
                    item {
                        Box(modifier = Modifier.size(100.dp), contentAlignment = Alignment.Center) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.PlayArrow, null)
                                    Text("${voiceDuration}\"", fontSize = 12.sp)
                                }
                            }
                            IconButton(
                                onClick = { selectedVoiceUri = null },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(20.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            ) {
                                Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(12.dp))
                            }
                        }
                    }
                }
                
                // 添加按钮区
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // 添加图片
                        if (selectedImageUris.size < 9) {
                            Surface(
                                onClick = { 
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    ) 
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(100.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                    Icon(Icons.Default.AddPhotoAlternate, null, tint = Color.Gray)
                                    Text("图片", fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                        }

                        // 添加音频 (模拟录音)
                        if (selectedVoiceUri == null) {
                            Surface(
                                onClick = { audioPickerLauncher.launch("audio/*") },
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(100.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                    Icon(Icons.Default.Mic, null, tint = Color.Gray)
                                    Text("语音", fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }

            if (uiState is PostUiState.Error) {
                Text(
                    text = (uiState as PostUiState.Error).message,
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }
}