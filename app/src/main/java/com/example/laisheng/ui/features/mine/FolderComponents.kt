package com.example.laisheng.ui.features.mine

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.laisheng.data.model.CollectionFolder
import com.example.laisheng.data.local.UserPrefs
import com.example.laisheng.ui.theme.AppIcon
import com.example.laisheng.ui.theme.AppIcons

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FolderList(
    folders: List<CollectionFolder>,
    selectedFolderId: String?,
    onFolderClick: (String?) -> Unit,
    onCreateClick: () -> Unit,
    onDeleteFolder: (String) -> Unit
) {
    val context = LocalContext.current
    val userPrefs = remember { UserPrefs(context) }
    var defaultFolderName by remember { mutableStateOf(userPrefs.getDefaultCollectionFolderName()) }

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        item {
            Surface(
                onClick = onCreateClick,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                modifier = Modifier
                    .height(32.dp)
                    .aspectRatio(1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    AppIcon(
                        glyph = AppIcons.Add,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        size = 16.dp
                    )
                }
            }
        }

        item {
            FolderChip(
                text = "全部",
                isSelected = selectedFolderId == null,
                onClick = { onFolderClick(null) }
            )
        }

        item {
            val showMenu = remember { mutableStateOf(false) }
            Box {
                FolderChip(
                    text = defaultFolderName,
                    isSelected = selectedFolderId == "uncategorized",
                    onClick = { onFolderClick("uncategorized") },
                    onLongClick = { showMenu.value = true }
                )
                DropdownMenu(
                    expanded = showMenu.value,
                    onDismissRequest = { showMenu.value = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("改成默认收藏夹") },
                        onClick = {
                            defaultFolderName = "默认收藏夹"
                            userPrefs.saveDefaultCollectionFolderName(defaultFolderName)
                            showMenu.value = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("改成未分类") },
                        onClick = {
                            defaultFolderName = "未分类"
                            userPrefs.saveDefaultCollectionFolderName(defaultFolderName)
                            showMenu.value = false
                        }
                    )
                }
            }
        }

        items(folders, key = { it.id }) { folder ->
            val showMenu = remember { mutableStateOf(false) }
            Box {
                FolderChip(
                    text = folder.name,
                    isSelected = selectedFolderId == folder.id,
                    onClick = { onFolderClick(folder.id) },
                    onLongClick = { showMenu.value = true }
                )

                DropdownMenu(
                    expanded = showMenu.value,
                    onDismissRequest = { showMenu.value = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("删除收藏夹", color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            onDeleteFolder(folder.id)
                            showMenu.value = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FolderChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    val backgroundColor =
        if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val contentColor =
        if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
    val borderColor =
        if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)

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
