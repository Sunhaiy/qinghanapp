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
import com.example.laisheng.data.NetworkModule

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
                        IconButton(onClick = {
                            // 修正：将 initialAvatar 和 initialBgImage 传进去作为兜底，防止被清空
                            viewModel.updateProfile(userId, nickname, bio, avatarUri, bgUri, initialAvatar, initialBgImage, context)
                        }) {
                            Icon(Icons.Default.Check, contentDescription = "保存", tint = MaterialTheme.colorScheme.primary)
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
            // 背景图编辑区
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { bgLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
            ) {
                AsyncImage(
                    model = bgUri ?: NetworkModule.formatUrl(initialBgImage),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White)
                }
            }

            // 头像编辑区
            Box(modifier = Modifier.padding(horizontal = 16.dp).offset(y = (-40).dp)) {
                Surface(
                    modifier = Modifier
                        .size(80.dp)
                        .border(3.dp, Color.White, CircleShape)
                        .clickable { avatarLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(avatarUri ?: NetworkModule.formatUrl(initialAvatar))
                                .decoderFactory(SvgDecoder.Factory())
                                .build(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(text = "数字身份", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text(text = handle, style = MaterialTheme.typography.bodyLarge, color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp, color = Color.LightGray)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = nickname, onValueChange = { nickname = it }, label = { Text("昵称") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp))
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = bio, onValueChange = { bio = it }, label = { Text("个人简介") }, modifier = Modifier.fillMaxWidth(), minLines = 3, shape = RoundedCornerShape(12.dp))
                if (uiState is EditUiState.Error) {
                    Text(text = (uiState as EditUiState.Error).message, color = Color.Red, modifier = Modifier.padding(top = 16.dp))
                }
            }
        }
    }
}
