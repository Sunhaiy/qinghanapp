package com.example.laisheng.ui.features.mine.edit

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
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
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.example.laisheng.data.remote.NetworkModule
import androidx.compose.animation.AnimatedVisibility
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Info

import com.example.laisheng.ui.components.LaishengLoading

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    userId: String,
    handle: String,
    initialNickname: String,
    initialBio: String,
    initialAvatar: String?,
    initialBgImage: String?,
    handleLastUpdatedAt: String?,
    onBack: () -> Unit,
    onSaveSuccess: () -> Unit,
    viewModel: EditProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var nickname by remember { mutableStateOf(initialNickname) }
    var currentHandle by remember { mutableStateOf(handle.removePrefix("@")) }
    var bio by remember { mutableStateOf(initialBio) }
    var avatarUri by remember { mutableStateOf<Uri?>(null) }
    var bgUri by remember { mutableStateOf<Uri?>(null) }

    val avatarLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> avatarUri = uri }
    )

    val bgLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> bgUri = uri }
    )

    LaunchedEffect(uiState) {
        if (uiState is EditUiState.Success) {
            onSaveSuccess()
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("编辑资料", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (uiState is EditUiState.Loading) {
                        LaishengLoading(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        TextButton(
                            onClick = {
                                val finalHandle = if (currentHandle.startsWith("@")) currentHandle else "@$currentHandle"
                                viewModel.updateProfile(userId, nickname, finalHandle, bio, avatarUri, bgUri, initialAvatar, initialBgImage, context)
                            },
                            enabled = nickname.isNotBlank()
                        ) {
                            Text("保存", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // 1. 顶部背景与头像区域
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                // 背景图
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { bgLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                ) {
                    AsyncImage(
                        model = bgUri ?: NetworkModule.formatUrl(initialBgImage),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // 背景图编辑提示
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .padding(8.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "更换背景", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }

                // 头像
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .border(4.dp, MaterialTheme.colorScheme.surface, CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { avatarLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(avatarUri ?: NetworkModule.formatUrl(initialAvatar))
                                .decoderFactory(SvgDecoder.Factory())
                                .build(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // 头像编辑遮罩 (仅在点击时或设计需要时显示，这里简化为常驻小图标指示)
                    }
                    // 头像编辑图标
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 4.dp, y = (-4).dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                            .padding(6.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "更换头像", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(14.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. 表单区域
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                
                // 计算 Handle 是否可编辑
                val canEditHandle = remember(handleLastUpdatedAt) {
                     if (handleLastUpdatedAt.isNullOrEmpty()) return@remember true
                     try {
                         val format = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
                         val date = format.parse(handleLastUpdatedAt.substring(0, 19))
                         val diff = System.currentTimeMillis() - (date?.time ?: 0)
                         val days = diff / (1000 * 60 * 60 * 24)
                         days > 30
                     } catch (e: Exception) { true }
                }
                
                val remainingDays = remember(handleLastUpdatedAt) {
                    if (handleLastUpdatedAt.isNullOrEmpty()) return@remember 0
                    try {
                         val format = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
                         val date = format.parse(handleLastUpdatedAt.substring(0, 19))
                        val diff = System.currentTimeMillis() - (date?.time ?: 0)
                        val days = diff / (1000 * 60 * 60 * 24)
                        if (days <= 30) (30 - days).toInt() else 0
                    } catch (e: Exception) { 0 }
                }

                // 昵称
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { if (it.length <= 20) nickname = it },
                    label = { Text("昵称") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    ),
                    trailingIcon = {
                        Text(
                            "${nickname.length}/20",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 数字身份 (Handle)
                OutlinedTextField(
                    value = currentHandle,
                    onValueChange = { if (it.length <= 20) currentHandle = it },
                    label = { Text("数字身份") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = canEditHandle,
                    shape = RoundedCornerShape(12.dp),
                    prefix = { Text("@", color = MaterialTheme.colorScheme.primary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                )

                // Handle 修改限制提示
                AnimatedVisibility(visible = !canEditHandle) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Lucide.Info, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "距离下次修改还需 ${remainingDays} 天 (30天内仅可修改一次)",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                
                if (canEditHandle) {
                    Text(
                        "30天内仅可修改一次",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(top = 6.dp, start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 个人简介
                OutlinedTextField(
                    value = bio,
                    onValueChange = { if (it.length <= 100) bio = it },
                    label = { Text("个人简介") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    maxLines = 6,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    ),
                    supportingText = {
                        Text(
                            "${bio.length}/100",
                            modifier = Modifier.fillMaxWidth(),
                             textAlign = androidx.compose.ui.text.style.TextAlign.End
                        )
                    }
                )

                if (uiState is EditUiState.Error) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = (uiState as EditUiState.Error).message,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(50.dp))
        }
    }
}
