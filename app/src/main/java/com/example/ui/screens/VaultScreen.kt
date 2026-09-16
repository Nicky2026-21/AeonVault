package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VaultItem
import com.example.data.repository.VaultRepository
import com.example.ui.components.FileItemCard
import com.example.ui.components.QuotaDisclaimerCard
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.CoralNeon
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.NeonCyan
import kotlinx.coroutines.launch

data class Breadcrumb(val id: String?, val name: String)

@Composable
fun VaultScreen(
    repository: VaultRepository,
    onFileClick: (VaultItem) -> Unit,
    onShareFile: (VaultItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var currentFolderId by remember { mutableStateOf<String?>(null) }
    var breadcrumbs by remember { mutableStateOf(listOf(Breadcrumb(null, "Vault Root"))) }
    var isGridView by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("ALL") } // ALL, FOLDERS, DOCUMENT, IMAGE, VIDEO, AUDIO, CODE, ARCHIVE, FAVORITES, TRASH
    var searchQuery by remember { mutableStateOf("") }
    var sortMode by remember { mutableStateOf("DATE_DESC") } // DATE_DESC, DATE_ASC, NAME_ASC, SIZE_DESC
    var sortMenuOpen by remember { mutableStateOf(false) }

    // Dialog States
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }

    var renamingItem by remember { mutableStateOf<VaultItem?>(null) }
    var renameInput by remember { mutableStateOf("") }

    var movingItem by remember { mutableStateOf<VaultItem?>(null) }

    // Flows
    val itemsInCurrentFolder by repository.getItemsInFolder(currentFolderId).collectAsState(initial = emptyList())
    val allActiveItems by repository.getAllActiveItems().collectAsState(initial = emptyList())
    val favoriteItems by repository.getFavorites().collectAsState(initial = emptyList())
    val trashItems by repository.getTrashItems().collectAsState(initial = emptyList())
    val searchResults by repository.searchItems(searchQuery).collectAsState(initial = emptyList())

    // Filter and Sort calculation
    val displayedItems = remember(
        selectedCategory,
        searchQuery,
        sortMode,
        itemsInCurrentFolder,
        allActiveItems,
        favoriteItems,
        trashItems,
        searchResults
    ) {
        val baseList = when {
            searchQuery.isNotBlank() -> searchResults
            selectedCategory == "TRASH" -> trashItems
            selectedCategory == "FAVORITES" -> favoriteItems
            selectedCategory == "ALL" -> itemsInCurrentFolder
            selectedCategory == "FOLDERS" -> allActiveItems.filter { it.isFolder }
            else -> allActiveItems.filter { it.aiCategory == selectedCategory && !it.isTrash }
        }

        when (sortMode) {
            "NAME_ASC" -> baseList.sortedWith(compareBy<VaultItem> { !it.isFolder }.thenBy { it.name.lowercase() })
            "SIZE_DESC" -> baseList.sortedWith(compareBy<VaultItem> { !it.isFolder }.thenByDescending { it.sizeBytes })
            "DATE_ASC" -> baseList.sortedWith(compareBy<VaultItem> { !it.isFolder }.thenBy { it.modifiedAt })
            else -> baseList.sortedWith(compareBy<VaultItem> { !it.isFolder }.thenByDescending { it.modifiedAt })
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // MANDATORY: Storage Quota Disclaimer on My Vault
        QuotaDisclaimerCard(modifier = Modifier.padding(bottom = 8.dp))

        // Search Bar & View Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search files, tags, metadata...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("vault_search_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Sort Button
            Box {
                IconButton(
                    onClick = { sortMenuOpen = true },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("vault_sort_button")
                ) {
                    Icon(Icons.Default.Sort, contentDescription = "Sort")
                }
                DropdownMenu(expanded = sortMenuOpen, onDismissRequest = { sortMenuOpen = false }) {
                    DropdownMenuItem(text = { Text("Newest Modified") }, onClick = { sortMode = "DATE_DESC"; sortMenuOpen = false })
                    DropdownMenuItem(text = { Text("Oldest Modified") }, onClick = { sortMode = "DATE_ASC"; sortMenuOpen = false })
                    DropdownMenuItem(text = { Text("Name (A-Z)") }, onClick = { sortMode = "NAME_ASC"; sortMenuOpen = false })
                    DropdownMenuItem(text = { Text("File Size (Largest)") }, onClick = { sortMode = "SIZE_DESC"; sortMenuOpen = false })
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Grid / List Toggle
            IconButton(
                onClick = { isGridView = !isGridView },
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .testTag("vault_view_toggle")
            ) {
                Icon(
                    imageVector = if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                    contentDescription = "Toggle Grid View"
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Breadcrumbs & Action Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically
            ) {
                breadcrumbs.forEachIndexed { index, crumb ->
                    Text(
                        text = crumb.name,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = if (index == breadcrumbs.lastIndex) FontWeight.Bold else FontWeight.Normal,
                            color = if (index == breadcrumbs.lastIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier
                            .clickable {
                                currentFolderId = crumb.id
                                breadcrumbs = breadcrumbs.take(index + 1)
                                selectedCategory = "ALL"
                            }
                            .padding(vertical = 4.dp, horizontal = 2.dp)
                    )
                    if (index < breadcrumbs.lastIndex) {
                        Text(" / ", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Row {
                if (selectedCategory == "TRASH") {
                    IconButton(
                        onClick = { scope.launch { repository.emptyTrash() } },
                        modifier = Modifier.testTag("empty_trash_button")
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Empty Trash", tint = CoralNeon)
                    }
                } else {
                    IconButton(
                        onClick = { showCreateFolderDialog = true },
                        modifier = Modifier.testTag("create_folder_button")
                    ) {
                        Icon(Icons.Default.CreateNewFolder, contentDescription = "Create Folder", tint = AmberAlert)
                    }
                }
            }
        }

        // Category Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val categories = listOf(
                "ALL" to "All",
                "DOCUMENT" to "Documents",
                "IMAGE" to "Images",
                "VIDEO" to "Video",
                "AUDIO" to "Audio",
                "CODE" to "Code",
                "ARCHIVE" to "Archives",
                "FAVORITES" to "Favorites",
                "TRASH" to "Trash"
            )
            categories.forEach { (catKey, catLabel) ->
                FilterChip(
                    selected = selectedCategory == catKey,
                    onClick = {
                        selectedCategory = catKey
                        if (catKey != "ALL") {
                            currentFolderId = null
                            breadcrumbs = listOf(Breadcrumb(null, catLabel))
                        } else {
                            breadcrumbs = listOf(Breadcrumb(null, "Vault Root"))
                        }
                    },
                    label = { Text(catLabel, fontSize = 11.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Files List or Grid
        if (displayedItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (selectedCategory == "TRASH") Icons.Default.Delete else Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (selectedCategory == "TRASH") "Trash is empty" else "No matching items found in vault",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (isGridView) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(displayedItems, key = { it.id }) { item ->
                    FileItemCard(
                        item = item,
                        isGrid = true,
                        onClick = {
                            if (item.isFolder) {
                                currentFolderId = item.id
                                breadcrumbs = breadcrumbs + Breadcrumb(item.id, item.name)
                            } else {
                                onFileClick(item)
                            }
                        },
                        onFavoriteToggle = { scope.launch { repository.toggleFavorite(item) } },
                        onShare = { onShareFile(item) },
                        onRename = {
                            renamingItem = item
                            renameInput = item.name
                        },
                        onMove = { movingItem = item },
                        onTrashOrRestore = {
                            scope.launch {
                                if (item.isTrash) repository.restoreFromTrash(item) else repository.moveToTrash(item)
                            }
                        },
                        onPermanentDelete = { scope.launch { repository.permanentlyDelete(item) } },
                        onDownload = {
                            Toast.makeText(context, "Downloaded '${item.name}'", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(displayedItems, key = { it.id }) { item ->
                    FileItemCard(
                        item = item,
                        isGrid = false,
                        onClick = {
                            if (item.isFolder) {
                                currentFolderId = item.id
                                breadcrumbs = breadcrumbs + Breadcrumb(item.id, item.name)
                            } else {
                                onFileClick(item)
                            }
                        },
                        onFavoriteToggle = { scope.launch { repository.toggleFavorite(item) } },
                        onShare = { onShareFile(item) },
                        onRename = {
                            renamingItem = item
                            renameInput = item.name
                        },
                        onMove = { movingItem = item },
                        onTrashOrRestore = {
                            scope.launch {
                                if (item.isTrash) repository.restoreFromTrash(item) else repository.moveToTrash(item)
                            }
                        },
                        onPermanentDelete = { scope.launch { repository.permanentlyDelete(item) } },
                        onDownload = {
                            Toast.makeText(context, "Downloaded '${item.name}'", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }

    // Create Folder Dialog
    if (showCreateFolderDialog) {
        AlertDialog(
            onDismissRequest = { showCreateFolderDialog = false },
            title = { Text("Create New Directory") },
            text = {
                OutlinedTextField(
                    value = newFolderName,
                    onValueChange = { newFolderName = it },
                    label = { Text("Directory Name") },
                    placeholder = { Text("e.g., Cryptographic Keys") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_directory_name_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = newFolderName.trim()
                        if (trimmed.isNotBlank()) {
                            scope.launch {
                                val result = repository.createFolder(trimmed, currentFolderId)
                                result.onSuccess {
                                    Toast.makeText(context, "Directory '$trimmed' created", Toast.LENGTH_SHORT).show()
                                    newFolderName = ""
                                    showCreateFolderDialog = false
                                    selectedCategory = "ALL"
                                    searchQuery = ""
                                }.onFailure { err ->
                                    Toast.makeText(context, err.message ?: "Could not create directory", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    },
                    modifier = Modifier.testTag("confirm_create_folder_button")
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateFolderDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Rename Dialog
    if (renamingItem != null) {
        AlertDialog(
            onDismissRequest = { renamingItem = null },
            title = { Text("Rename Item") },
            text = {
                OutlinedTextField(
                    value = renameInput,
                    onValueChange = { renameInput = it },
                    label = { Text("New Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val item = renamingItem
                        if (item != null && renameInput.isNotBlank()) {
                            scope.launch {
                                repository.renameItem(item, renameInput.trim())
                                renamingItem = null
                            }
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { renamingItem = null }) { Text("Cancel") }
            }
        )
    }

    // Move Dialog
    if (movingItem != null) {
        val itemToMove = movingItem!!
        val availableFolders = allActiveItems.filter { it.isFolder && it.id != itemToMove.id }

        AlertDialog(
            onDismissRequest = { movingItem = null },
            title = { Text("Move '${itemToMove.name}'") },
            text = {
                Column {
                    Text("Select destination directory:", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                repository.moveItem(itemToMove, null)
                                movingItem = null
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Move to Vault Root")
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    for (folder in availableFolders) {
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    repository.moveItem(itemToMove, folder.id)
                                    movingItem = null
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CreateNewFolder, contentDescription = null, tint = AmberAlert)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(folder.name)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { movingItem = null }) { Text("Cancel") }
            }
        )
    }
}
