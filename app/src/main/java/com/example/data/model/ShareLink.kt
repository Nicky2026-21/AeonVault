package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "share_links")
data class ShareLink(
    @PrimaryKey val id: String,
    val itemId: String,
    val itemName: String,
    val userId: String,
    val accessType: String = "PUBLIC", // PUBLIC, PRIVATE, UNLISTED
    val isPasswordProtected: Boolean = false,
    val passwordHash: String? = null,
    val allowDownload: Boolean = true,
    val expiresAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val viewsCount: Int = 0,
    val downloadsCount: Int = 0,
    val isRevoked: Boolean = false
) {
    val shareUrl: String
        get() = "https://aeonvault.vercel.app/share/$id"

    val isExpired: Boolean
        get() = expiresAt != null && System.currentTimeMillis() > expiresAt

    val formattedExpiry: String
        get() = if (expiresAt == null) "Never expires" else "Expires " + SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(expiresAt))
}
