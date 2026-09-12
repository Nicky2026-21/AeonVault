package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.security.MessageDigest
import java.text.DecimalFormat

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String,
    val username: String,
    val email: String,
    val passwordHash: String,
    val salt: String,
    val recoveryKey: String,
    val createdAt: Long = System.currentTimeMillis(),
    val quotaUsedBytes: Long = 0L,
    val planTier: String = "Æon Prime",
    val avatarColorHex: String = "#00F5FF",
    val isPermanentProtected: Boolean = true,
    val replicationNodes: Int = 12,
    val disasterRecoveryActive: Boolean = true
) {
    companion object {
        // 1 Quettabyte = 10^30 bytes (or 2^100 bytes logically)
        const val LOGICAL_QUOTA_STR = "1 QB"
        
        fun hashPassword(password: String, salt: String): String {
            val md = MessageDigest.getInstance("SHA-256")
            val bytes = md.digest((password + salt).toByteArray(Charsets.UTF_8))
            return bytes.joinToString("") { "%02x".format(it) }
        }

        fun formatStorageSize(bytes: Long): String {
            if (bytes <= 0) return "0 B"
            val units = arrayOf("B", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB", "RB", "QB")
            var value = bytes.toDouble()
            var unitIndex = 0
            while (value >= 1024.0 && unitIndex < units.size - 1) {
                value /= 1024.0
                unitIndex++
            }
            val df = DecimalFormat("#,##0.##")
            return "${df.format(value)} ${units[unitIndex]}"
        }
    }
}
