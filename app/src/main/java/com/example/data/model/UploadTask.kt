package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UploadStatus {
    QUEUED,
    UPLOADING,
    PAUSED,
    COMPLETED,
    FAILED,
    CANCELLED
}

@Entity(tableName = "upload_tasks")
data class UploadTask(
    @PrimaryKey val id: String,
    val userId: String,
    val fileName: String,
    val mimeType: String,
    val totalBytes: Long,
    val uploadedBytes: Long = 0L,
    val chunkSize: Int = 1024 * 1024, // 1MB dynamic chunk size
    val totalChunks: Int = 1,
    val completedChunks: Int = 0,
    val speedBytesPerSec: Long = 0L,
    val etaSeconds: Long = 0L,
    val status: UploadStatus = UploadStatus.QUEUED,
    val targetFolderId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val errorReason: String? = null,
    val retryCount: Int = 0,
    val parallelStreams: Int = 4,
    val sourceUri: String? = null
) {
    val progressFraction: Float
        get() = if (totalBytes <= 0) 0f else (uploadedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)

    val progressPercent: Int
        get() = (progressFraction * 100).toInt()

    val formattedSpeed: String
        get() = when {
            speedBytesPerSec <= 0 -> "0 B/s"
            speedBytesPerSec < 1024 * 1024 -> "${speedBytesPerSec / 1024} KB/s"
            speedBytesPerSec < 1024L * 1024 * 1024 -> String.format(java.util.Locale.US, "%.1f MB/s", speedBytesPerSec / (1024.0 * 1024.0))
            else -> String.format(java.util.Locale.US, "%.2f GB/s", speedBytesPerSec / (1024.0 * 1024.0 * 1024.0))
        }

    val formattedEta: String
        get() = when {
            status == UploadStatus.COMPLETED -> "Completed"
            status == UploadStatus.PAUSED -> "Paused"
            etaSeconds <= 0 -> "Calculating..."
            etaSeconds < 60 -> "${etaSeconds}s remaining"
            etaSeconds < 3600 -> "${etaSeconds / 60}m ${etaSeconds % 60}s remaining"
            else -> "${etaSeconds / 3600}h ${(etaSeconds % 3600) / 60}m remaining"
        }
}
