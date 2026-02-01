package com.example.laisheng.ui.features.post

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.laisheng.R
import com.example.laisheng.ui.theme.LaishengTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostScreen(
    onCancel: () -> Unit,
    onPost: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onCancel) {
                    Text("取消")
                }
                Text("发布瞬间", fontWeight = FontWeight.Bold)
                Button(onClick = onPost) {
                    Text("发布")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            UserInfo()
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = "",
                onValueChange = {},
                placeholder = { Text("分享此刻的情绪与故事...") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            ImagePlaceholder()
        }
    }
}

@Composable
fun UserInfo() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "Avatar",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
        )
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text("Alex Chen", fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                Text("公开可见", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun ImagePlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Image, contentDescription = null, tint = Color.Gray)
            Text("添加图片或视频", color = Color.Gray)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PostScreenPreview() {
    LaishengTheme {
        PostScreen(onCancel = {}, onPost = {})
    }
}
