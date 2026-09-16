package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ActivityLog
import com.example.data.model.ShareLink
import com.example.data.model.UploadTask
import com.example.data.model.User
import com.example.data.model.VaultItem
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultDao {

    // --- Users ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserById(id: String): Flow<User?>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserByIdSync(id: String): User?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getFirstUser(): User?

    @Query("SELECT * FROM users ORDER BY createdAt ASC")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM users ORDER BY createdAt ASC")
    suspend fun getAllUsersSync(): List<User>

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUser(userId: String)

    @Query("UPDATE users SET quotaUsedBytes = :usedBytes WHERE id = :userId")
    suspend fun updateUserQuota(userId: String, usedBytes: Long)

    // --- Vault Items ---
    @Query("SELECT * FROM vault_items WHERE userId = :userId AND isTrash = 0 AND ((:parentId IS NULL AND (parentId IS NULL OR parentId = '')) OR parentId = :parentId) ORDER BY isFolder DESC, name ASC")
    fun getItemsInFolder(userId: String, parentId: String?): Flow<List<VaultItem>>

    @Query("SELECT * FROM vault_items WHERE userId = :userId AND name = :name AND ((:parentId IS NULL AND (parentId IS NULL OR parentId = '')) OR parentId = :parentId) AND isTrash = 0 LIMIT 1")
    suspend fun findItemByNameInFolder(userId: String, name: String, parentId: String?): VaultItem?

    @Query("SELECT * FROM vault_items WHERE userId = :userId AND isTrash = 0 ORDER BY modifiedAt DESC")
    fun getAllActiveItems(userId: String): Flow<List<VaultItem>>

    @Query("SELECT * FROM vault_items WHERE userId = :userId AND isTrash = 0 ORDER BY modifiedAt DESC")
    suspend fun getAllActiveItemsSync(userId: String): List<VaultItem>

    @Query("SELECT * FROM vault_items WHERE userId = :userId AND isTrash = 0 AND isFavorite = 1 ORDER BY name ASC")
    fun getFavorites(userId: String): Flow<List<VaultItem>>

    @Query("SELECT * FROM vault_items WHERE userId = :userId AND isTrash = 0 AND isShared = 1 ORDER BY modifiedAt DESC")
    fun getSharedItems(userId: String): Flow<List<VaultItem>>

    @Query("SELECT * FROM vault_items WHERE userId = :userId AND isTrash = 1 ORDER BY trashedAt DESC")
    fun getTrashItems(userId: String): Flow<List<VaultItem>>

    @Query("SELECT * FROM vault_items WHERE userId = :userId AND isTrash = 0 ORDER BY modifiedAt DESC LIMIT :limit")
    fun getRecentItems(userId: String, limit: Int = 10): Flow<List<VaultItem>>

    @Query("SELECT * FROM vault_items WHERE userId = :userId AND isTrash = 0 AND (name LIKE '%' || :query || '%' OR aiTags LIKE '%' || :query || '%' OR aiDescription LIKE '%' || :query || '%') ORDER BY modifiedAt DESC")
    fun searchItems(userId: String, query: String): Flow<List<VaultItem>>

    @Query("SELECT * FROM vault_items WHERE id = :id LIMIT 1")
    fun getItemById(id: String): Flow<VaultItem?>

    @Query("SELECT * FROM vault_items WHERE id = :id LIMIT 1")
    suspend fun getItemByIdSync(id: String): VaultItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: VaultItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<VaultItem>)

    @Update
    suspend fun updateItem(item: VaultItem)

    @Delete
    suspend fun deleteItem(item: VaultItem)

    @Query("DELETE FROM vault_items WHERE id = :id")
    suspend fun deleteItemById(id: String)

    @Query("DELETE FROM vault_items WHERE userId = :userId AND isTrash = 1")
    suspend fun emptyTrash(userId: String)

    @Query("SELECT COALESCE(SUM(sizeBytes), 0) FROM vault_items WHERE userId = :userId AND isTrash = 0 AND isFolder = 0")
    suspend fun calculateUsedBytes(userId: String): Long

    // --- Upload Tasks ---
    @Query("SELECT * FROM upload_tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: String): UploadTask?

    @Query("SELECT * FROM upload_tasks WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllTasks(userId: String): Flow<List<UploadTask>>

    @Query("SELECT * FROM upload_tasks WHERE userId = :userId AND status IN ('UPLOADING', 'QUEUED')")
    fun getActiveTasks(userId: String): Flow<List<UploadTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: UploadTask)

    @Update
    suspend fun updateTask(task: UploadTask)

    @Delete
    suspend fun deleteTask(task: UploadTask)

    @Query("DELETE FROM upload_tasks WHERE id = :id")
    suspend fun deleteTaskById(id: String)

    @Query("DELETE FROM upload_tasks WHERE userId = :userId AND status = 'COMPLETED'")
    suspend fun clearCompletedTasks(userId: String)

    // --- Activity Logs ---
    @Query("SELECT * FROM activity_logs WHERE userId = :userId ORDER BY timestamp DESC LIMIT :limit")
    fun getLogsForUser(userId: String, limit: Int = 100): Flow<List<ActivityLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ActivityLog)

    // --- Share Links ---
    @Query("SELECT * FROM share_links WHERE userId = :userId AND isRevoked = 0 ORDER BY createdAt DESC")
    fun getShareLinksForUser(userId: String): Flow<List<ShareLink>>

    @Query("SELECT * FROM share_links WHERE id = :id LIMIT 1")
    suspend fun getShareLinkById(id: String): ShareLink?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShareLink(link: ShareLink)

    @Query("UPDATE share_links SET isRevoked = 1 WHERE id = :id")
    suspend fun revokeShareLink(id: String)

    @Query("DELETE FROM share_links WHERE id = :id")
    suspend fun deleteShareLink(id: String)
}
