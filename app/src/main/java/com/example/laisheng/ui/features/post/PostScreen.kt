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
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import java.io.File

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
            selectedVoiceUri = uri 
            voiceDuration = 10 
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
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onCancel) { Text("取消", color = Color.Gray) }
                Text("发布瞬间", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Button(
                    onClick = { 
                        viewModel.createMoment(userId, content, selectedImageUris.toList(), selectedVoiceUri, voiceDuration, context) 
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
                modifier = Modifier.fillMaxWidth().height(120.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                items(selectedImageUris) { uri ->
                    Box(modifier = Modifier.size(100.dp)) {
                        AsyncImage(model = uri, contentDescription = null, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                        IconButton(
                            onClick = { selectedImageUris.remove(uri) },
                            modifier = Modifier.align(Alignment.TopEnd).size(20.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) { Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(12.dp)) }
                    }
                }

                selectedVoiceUri?.let {
                    item {
                        Box(modifier = Modifier.size(100.dp), contentAlignment = Alignment.Center) {
                            Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.fillMaxSize()) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                    Icon(Icons.Default.PlayArrow, null)
                                    Text("${voiceDuration}\"", fontSize = 12.sp)
                                }
                            }
                            IconButton(onClick = { selectedVoiceUri = null }, modifier = Modifier.align(Alignment.TopEnd).size(20.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)) {
                                Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(12.dp))
                            }
                        }
                    }
                }
                
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (selectedImageUris.size < 9) {
                            FunctionButton(
                                icon = Icons.Default.AddPhotoAlternate, 
                                text = "图片",
                                enabled = !isRecording 
                            ) {
                                photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                        }

                        if (selectedVoiceUri == null) {
                            FunctionButton(
                                icon = Icons.Default.UploadFile, 
                                text = "传语音",
                                enabled = !isRecording 
                            ) {
                                audioPickerLauncher.launch("audio/*")
                            }
                        }

                        if (selectedVoiceUri == null) {
                            val scale by animateFloatAsState(if (isRecording) 1.1f else 1f, label = "")
                            val color by animateColorAsState(
                                if (isRecording) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant,
                                label = ""
                            )

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = color,
                                modifier = Modifier
                                    .size(100.dp)
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
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                    Icon(Icons.Default.Mic, null, tint = if (isRecording) Color.Red else Color.Gray)
                                    Text(if (isRecording) "松开结束" else "按住录音", fontSize = 12.sp, color = Color.Gray)
                                }
                            }
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
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Surface(
        onClick = if (enabled) onClick else ({}), 
        shape = RoundedCornerShape(8.dp), 
        color = if (enabled) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), 
        modifier = Modifier.size(100.dp),
        enabled = enabled
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally, 
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.alpha(if (enabled) 1f else 0.4f)
        ) {
            Icon(icon, null, tint = Color.Gray)
            Text(text, fontSize = 12.sp, color = Color.Gray)
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
