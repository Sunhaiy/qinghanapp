package com.example.laisheng.ui.features.mine

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.BorderStroke
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.example.laisheng.data.model.CollectionFolder

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FolderList(
    folders: List<CollectionFolder>,
    selectedFolderId: String?,
    onFolderClick: (String?) -> Unit,
    onCreateClick: () -> Unit,
    onDeleteFolder: (String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)
    ) {
        item {
            FolderChip(
                text = "全部",
                isSelected = selectedFolderId == null,
                onClick = { onFolderClick(null) }
            )
        }
        item {
             FolderChip(
                text = "默认",
                isSelected = selectedFolderId == "uncategorized",
                onClick = { onFolderClick("uncategorized") }
            )
        }
        items(folders.size) { index ->
            val folder = folders[index]
            var showMenu by remember { mutableStateOf(false) }

            Box {
                FolderChip(
                    text = folder.name,
                    isSelected = selectedFolderId == folder.id,
                    onClick = { onFolderClick(folder.id) },
                    onLongClick = { showMenu = true }
                )
                
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("删除收藏夹", color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            onDeleteFolder(folder.id)
                            showMenu = false
                        }
                    )
                }
            }
        }
        item {
            Surface(
                onClick = onCreateClick,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                modifier = Modifier.height(32.dp).aspectRatio(1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Lucide.Plus, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FolderChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
    val borderColor = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    
    Surface(
        color = backgroundColor,
        contentColor = contentColor,
        shape = CircleShape,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier
            .height(32.dp)
            .clip(CircleShape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}


