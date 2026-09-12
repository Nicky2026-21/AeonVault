package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(
    tableName = "vault_items",
    indices = [
        Index("userId"),
        Index("parentId"),
        Index("isTrash"),
        Index("isFavorite"),
        Index("isShared")
    ]
)
data class VaultItem(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val parentId: String? = null,
    val isFolder: Boolean = false,
    val mimeType: String = "application/octet-stream",
    val sizeBytes: Long = 0L,
    val extension: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val isShared: Boolean = false,
    val isTrash: Boolean = false,
    val trashedAt: Long? = null,
    val sha256Checksum: String = "",
    val storageCluster: String = "Æon Distributed Cluster Delta-7",
    val encryptionStandard: String = "AES-256-GCM (Quantum Resistant)",
    val aiTags: String = "",
    val aiSummary: String? = null,
    val aiCategory: String = "GENERAL",
    val aiDescription: String? = null,
    val textContentPreview: String? = null,
    val localCachedPath: String? = null
) {
    val formattedSize: String
        get() = if (isFolder) "--" else User.formatStorageSize(sizeBytes)

    val formattedDate: String
        get() = SimpleDateFormat("MMM d, yyyy · HH:mm", Locale.getDefault()).format(Date(modifiedAt))

    val tagsList: List<String>
        get() = if (aiTags.isBlank()) emptyList() else aiTags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
}
