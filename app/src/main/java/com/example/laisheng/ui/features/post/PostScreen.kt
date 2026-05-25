package com.example.laisheng.ui.features.post

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.composables.icons.lucide.*
import java.io.File

import com.example.laisheng.ui.theme.Dimens

import com.example.laisheng.ui.components.LaishengLoading

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
    
    var selectedVoiceUri by remember { mutableStateOf<Uri?>(null) }
    var voiceDuration by remember { mutableIntStateOf(0) }
    var isRecording by remember { mutableStateOf(false) }
    var recordStartTime by remember { mutableLongStateOf(0L) }

    val uiState by viewModel.uiState.collectAsState()
    val recorder = remember { AudioRecorderHelper(context) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "需要麦克风权限才能录音", Toast.LENGTH_SHORT).show()
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(9),
        onResult = { uris -> selectedImageUris.addAll(uris) }
    )

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> 
            if (uri != null) {
                selectedVoiceUri = uri 
                voiceDuration = 10 
            }
        }
    )

    LaunchedEffect(uiState) {
        if (uiState is PostUiState.Success) {
            onPostSuccess()
            viewModel.resetState()
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding() // 核心：让内容随输入法推起
                .padding(horizontal = Dimens.PaddingLarge, vertical = Dimens.PaddingMedium)
        ) {
            // 顶部导航
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onCancel) { 
                    Text("取消", color = MaterialTheme.colorScheme.outline, fontSize = 16.sp) 
                }
                Text("瞬间", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                Button(
                    onClick = { 
                        viewModel.createMoment(content, selectedImageUris.toList(), selectedVoiceUri, voiceDuration, context) 
                    },
                    enabled = (content.isNotBlank() || selectedImageUris.isNotEmpty() || selectedVoiceUri != null) && uiState !is PostUiState.Loading,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp)
                ) {
                    if (uiState is PostUiState.Loading) {
                        LaishengLoading(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("发布", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 内容输入区域
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                placeholder = { 
                    Text("这一刻你想说什么...", color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)) 
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    disabledBorderColor = Color.Transparent,
                    errorBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp)
            )

            // 预览区域 (图片 & 语音)
            if (selectedImageUris.isNotEmpty() || selectedVoiceUri != null) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    items(selectedImageUris) { uri ->
                        Box(modifier = Modifier.size(90.dp)) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(uri)
                                    .decoderFactory(SvgDecoder.Factory())
                                    .crossfade(true)
                                    .build(),
                                contentDescription = null, 
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant), 
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = { selectedImageUris.remove(uri) },
                                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(24.dp).background(Color.Black.copy(alpha = 0.4f), CircleShape)
                            ) { Icon(Lucide.X, null, tint = Color.White, modifier = Modifier.size(14.dp)) }
                        }
                    }

                    selectedVoiceUri?.let {
                        item {
                            Box(modifier = Modifier.size(90.dp), contentAlignment = Alignment.Center) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp), 
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), 
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                        Icon(Lucide.Play, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("${voiceDuration}\"", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    }
                                }
                                IconButton(
                                    onClick = { selectedVoiceUri = null }, 
                                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(24.dp).background(Color.Black.copy(alpha = 0.4f), CircleShape)
                                ) { Icon(Lucide.X, null, tint = Color.White, modifier = Modifier.size(14.dp)) }
                            }
                        }
                    }
                }
            }

            // 功能操作区
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (selectedImageUris.size < 9) {
                        FunctionButton(icon = Lucide.ImagePlus, text = "选图") {
                            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                    }

                    if (selectedVoiceUri == null) {
                        FunctionButton(icon = Lucide.FileUp, text = "传音") {
                            audioPickerLauncher.launch("audio/*")
                        }
                    }
                }

                if (selectedVoiceUri == null) {
                    val scale by animateFloatAsState(if (isRecording) 1.03f else 1f, label = "")
                    val color by animateColorAsState(
                        if (isRecording) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        label = ""
                    )

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = color,
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp)
                            .scale(scale)
                            .pointerInput(Unit) {
                                awaitPointerEventScope {
                                    while (true) {
                                        awaitFirstDown()
                                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        } else {
                                            isRecording = true
                                            recordStartTime = System.currentTimeMillis()
                                            val file = File(context.cacheDir, "recorded_audio.m4a")
                                            recorder.start(file)
                                            
                                            waitForUpOrCancellation()
                                            
                                            val duration = ((System.currentTimeMillis() - recordStartTime) / 1000).toInt()
                                            recorder.stop()
                                            if (duration >= 1) {
                                                selectedVoiceUri = Uri.fromFile(file)
                                                voiceDuration = duration
                                            }
                                            isRecording = false
                                        }
                                    }
                                }
                            }
                    ) {
                        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Lucide.Mic, 
                                null, 
                                tint = if (isRecording) Color.Red else MaterialTheme.colorScheme.onSecondaryContainer, 
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isRecording) "松开结束" else "按住录音", 
                                fontSize = 14.sp, 
                                fontWeight = FontWeight.Bold,
                                color = if (isRecording) Color.Red else MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FunctionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector, 
    text: String, 
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick, 
        shape = RoundedCornerShape(16.dp), 
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f), 
        modifier = Modifier.size(height = 64.dp, width = 64.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally, 
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(2.dp))
            Text(text, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight.Medium)
        }
    }
}

class AudioRecorderHelper(private val context: Context) {
    private var recorder: MediaRecorder? = null
    fun start(outputFile: File) {
        try {
            if (outputFile.exists()) outputFile.delete()
            recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(context) else MediaRecorder(context)
            recorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }
        } catch (e: Exception) { e.printStackTrace() }
    }
    fun stop() {
        try { recorder?.stop() } catch (e: Exception) { e.printStackTrace() } finally { recorder?.release(); recorder = null }
    }
}
