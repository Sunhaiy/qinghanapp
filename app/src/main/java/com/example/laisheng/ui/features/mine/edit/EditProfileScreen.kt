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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    userId: String,
    handle: String,
    initialNickname: String,
    initialBio: String,
    initialAvatar: String?,
    initialBgImage: String?,
    onBack: () -> Unit,
    onSaveSuccess: () -> Unit,
    viewModel: EditProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var nickname by remember { mutableStateOf(initialNickname) }
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
                title = { Text("编辑资料", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (uiState is EditUiState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        TextButton(onClick = {
                            viewModel.updateProfile(userId, nickname, bio, avatarUri, bgUri, initialAvatar, initialBgImage, context)
                        }) {
                            Text("保存", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // 背景 & 头像 编辑区域
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp) // 增加高度以容纳头像 + 提示
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
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)), 
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White)
                            Text("点击更换背景", color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }

                // 头像 (Overlapping)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 24.dp, bottom = 0.dp) // Align slightly above bottom of outer box
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
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.3f)), 
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 表单区域
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                // Handle (Read Only)
                Text(text = "数字身份", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text(
                    text = handle, 
                    style = MaterialTheme.typography.bodyLarge, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant, 
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )
                
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(24.dp))

                // Nickname
                OutlinedTextField(
                    value = nickname, 
                    onValueChange = { if (it.length <= 20) nickname = it }, 
                    label = { Text("昵称") }, 
                    modifier = Modifier.fillMaxWidth(), 
                    singleLine = true, 
                    shape = RoundedCornerShape(12.dp),
                    supportingText = { Text("${nickname.length}/20") }
                )
                
                Spacer(modifier = Modifier.height(24.dp))

                // Bio
                OutlinedTextField(
                    value = bio, 
                    onValueChange = { if (it.length <= 100) bio = it }, 
                    label = { Text("个人简介") }, 
                    modifier = Modifier.fillMaxWidth(), 
                    minLines = 3, 
                    maxLines = 5,
                    shape = RoundedCornerShape(12.dp),
                    supportingText = { Text("${bio.length}/100") }
                )

                if (uiState is EditUiState.Error) {
                    Text(
                        text = (uiState as EditUiState.Error).message, 
                        color = MaterialTheme.colorScheme.error, 
                        modifier = Modifier.padding(top = 16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(50.dp))
        }
    }
}
