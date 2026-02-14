package com.example.laisheng.ui.features.mine

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.laisheng.data.model.CollectionFolder

@Composable
fun CreateFolderDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新建收藏夹") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { if (it.length <= 10) name = it },
                label = { Text("名称 (最多10字)") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onConfirm(name) }) {
                Text("创建")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

@Composable
fun MoveToFolderDialog(
    folders: List<CollectionFolder>,
    onDismiss: () -> Unit,
    onSelectFolder: (String?) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("收藏到...") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Default option
                 TextButton(
                    onClick = { onSelectFolder(null) }, // Move to default (uncategorized)
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text("默认收藏夹", color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.weight(1f))
                }
                
                HorizontalDivider()
                
                if (folders.isEmpty()) {
                     Text(
                        "暂无其他收藏夹", 
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                
                folders.forEach { folder ->
                    TextButton(
                        onClick = { onSelectFolder(folder.id) },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(folder.name, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
