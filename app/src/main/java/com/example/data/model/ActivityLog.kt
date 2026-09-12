package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "activity_logs")
data class ActivityLog(
    @PrimaryKey val id: String,
    val userId: String,
    val actionType: String, // UPLOAD, DOWNLOAD, SHARE, DELETE, RESTORE, GEMINI_QUERY, RENAME, MOVE, FAVORITE
    val description: String,
    val targetItemName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val ipNode: String = "198.51.100.42 (Edge-Tokyo-01)",
    val clientType: String = "Native Android v1.0"
) {
    val formattedTimestamp: String
        get() = SimpleDateFormat("MMM d, yyyy · HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
}
