package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VaultItem
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.CoralNeon
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.QuantumIndigo

fun getCategoryIconAndColor(item: VaultItem): Pair<ImageVector, Color> {
    if (item.isFolder) {
        return Pair(Icons.Default.Folder, AmberAlert)
    }
    return when (item.aiCategory) {
        "IMAGE" -> Pair(Icons.Default.Image, NeonCyan)
        "VIDEO" -> Pair(Icons.Default.VideoFile, CoralNeon)
        "AUDIO" -> Pair(Icons.Default.AudioFile, ElectricViolet)
        "CODE" -> Pair(Icons.Default.Code, EmeraldGlow)
        "ARCHIVE" -> Pair(Icons.Default.Archive, QuantumIndigo)
        "DOCUMENT" -> Pair(Icons.Default.Description, NeonCyan)
        else -> Pair(Icons.Default.Description, Color.Gray)
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun FileItemCard(
    item: VaultItem,
    isGrid: Boolean = false,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onShare: () -> Unit,
    onRename: () -> Unit,
    onMove: () -> Unit,
    onTrashOrRestore: () -> Unit,
    onPermanentDelete: () -> Unit,
    onDownload: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val (icon, color) = getCategoryIconAndColor(item)

    if (isGrid) {
        // Grid View Card
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
                .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { menuExpanded = true }
                )
                .padding(12.dp)
                .testTag("file_grid_item_${item.id}")
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Row {
                        if (!item.isFolder && !item.isTrash) {
                            IconButton(
                                onClick = onFavoriteToggle,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = if (item.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "Favorite",
                                    tint = if (item.isFavorite) AmberAlert else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = item.formattedSize,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Monospace
                    )
                    if (item.isShared) {
                        CyberBadge("Shared", color = ElectricViolet)
                    }
                }
            }

            FileActionMenu(
                expanded = menuExpanded,
                item = item,
                onDismiss = { menuExpanded = false },
                onShare = { menuExpanded = false; onShare() },
                onRename = { menuExpanded = false; onRename() },
                onMove = { menuExpanded = false; onMove() },
                onTrashOrRestore = { menuExpanded = false; onTrashOrRestore() },
                onPermanentDelete = { menuExpanded = false; onPermanentDelete() },
                onDownload = { menuExpanded = false; onDownload() }
            )
        }
    } else {
        // List View Card
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f))
                .border(1.dp, color.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { menuExpanded = true }
                )
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .testTag("file_list_item_${item.id}")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = item.formattedSize,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = " · ",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = item.formattedDate,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (item.isShared) {
                        CyberBadge("Shared", color = ElectricViolet, modifier = Modifier.padding(end = 4.dp))
                    }
                    if (!item.isFolder && !item.isTrash) {
                        IconButton(onClick = onFavoriteToggle) {
                            Icon(
                                imageVector = if (item.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Favorite",
                                tint = if (item.isFavorite) AmberAlert else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            FileActionMenu(
                expanded = menuExpanded,
                item = item,
                onDismiss = { menuExpanded = false },
                onShare = { menuExpanded = false; onShare() },
                onRename = { menuExpanded = false; onRename() },
                onMove = { menuExpanded = false; onMove() },
                onTrashOrRestore = { menuExpanded = false; onTrashOrRestore() },
                onPermanentDelete = { menuExpanded = false; onPermanentDelete() },
                onDownload = { menuExpanded = false; onDownload() }
            )
        }
    }
}

@Composable
fun FileActionMenu(
    expanded: Boolean,
    item: VaultItem,
    onDismiss: () -> Unit,
    onShare: () -> Unit,
    onRename: () -> Unit,
    onMove: () -> Unit,
    onTrashOrRestore: () -> Unit,
    onPermanentDelete: () -> Unit,
    onDownload: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        if (!item.isTrash) {
            if (!item.isFolder) {
                DropdownMenuItem(
                    text = { Text("Download to Device") },
                    leadingIcon = { Icon(Icons.Default.Download, contentDescription = null) },
                    onClick = onDownload
                )
                DropdownMenuItem(
                    text = { Text("Share Link") },
                    leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                    onClick = onShare
                )
            }
            DropdownMenuItem(
                text = { Text("Rename") },
                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                onClick = onRename
            )
            DropdownMenuItem(
                text = { Text("Move") },
                leadingIcon = { Icon(Icons.Default.DriveFileMove, contentDescription = null) },
                onClick = onMove
            )
            DropdownMenuItem(
                text = { Text("Move to Trash") },
                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = CoralNeon) },
                onClick = onTrashOrRestore
            )
        } else {
            DropdownMenuItem(
                text = { Text("Restore to Vault") },
                leadingIcon = { Icon(Icons.Default.Restore, contentDescription = null, tint = EmeraldGlow) },
                onClick = onTrashOrRestore
            )
            DropdownMenuItem(
                text = { Text("Permanently Delete") },
                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = CoralNeon) },
                onClick = onPermanentDelete
            )
        }
    }
}
