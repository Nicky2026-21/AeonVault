package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.local.AppDatabase
import com.example.data.local.VaultDao
import com.example.data.model.ActivityLog
import com.example.data.model.ShareLink
import com.example.data.model.UploadTask
import com.example.data.model.User
import com.example.data.model.VaultItem
import com.example.engine.upload.ResumableUploadEngine
import com.example.engine.sync.VaultSyncManager
import com.example.engine.sync.SyncState
import com.example.network.gemini.GeminiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

import android.content.SharedPreferences

@OptIn(ExperimentalCoroutinesApi::class)
class VaultRepository(private val context: Context) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val prefs: SharedPreferences = context.getSharedPreferences("aeon_vault_prefs", Context.MODE_PRIVATE)
    
    val db = AppDatabase.getDatabase(context)
    val dao: VaultDao = db.vaultDao()
    val geminiService = GeminiService()
    val uploadEngine = ResumableUploadEngine(context, dao, geminiService)
    val syncManager = VaultSyncManager(context, dao)
    val syncState: StateFlow<SyncState> = syncManager.syncState

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    val allUsers: Flow<List<User>> = dao.getAllUsers()

    private val _isInitializing = MutableStateFlow(true)
    val isInitializing: StateFlow<Boolean> = _isInitializing.asStateFlow()

    init {
        syncManager.setOnAccountUpdatedListener { updatedUser ->
            if (_currentUser.value?.id == updatedUser.id) {
                _currentUser.value = updatedUser
            }
        }
        syncManager.startBackgroundSync()

        scope.launch {
            val lastUserId = prefs.getString("last_active_user_id", null)
            var user = if (lastUserId != null) dao.getUserByIdSync(lastUserId) else dao.getFirstUser()
            
            if (user == null) {
                val defaultSalt = UUID.randomUUID().toString().take(8)
                val defaultUser = User(
                    id = "aeon-usr-core-01",
                    username = "NexusCommander",
                    email = "vault.commander@aeonvaultfilemanager.vercel.app",
                    passwordHash = User.hashPassword("AeonVault2026!", defaultSalt),
                    salt = defaultSalt,
                    recoveryKey = "AEON-9842-QUANTUM-RECOVER-7719",
                    quotaUsedBytes = 0L,
                    planTier = "Æon Prime",
                    avatarColorHex = "#00F5FF"
                )
                dao.insertUser(defaultUser)
                user = defaultUser
                seedDefaultVaultData(defaultUser.id)
            }
            _currentUser.value = user
            saveLastActiveUser(user?.id)
            _isInitializing.value = false
        }
    }

    private fun saveLastActiveUser(userId: String?) {
        prefs.edit().putString("last_active_user_id", userId).apply()
    }

    private suspend fun seedDefaultVaultData(userId: String) {
        val folderDocsId = "folder-quantum-docs"
        val folderMediaId = "folder-media-core"
        val folderCodeId = "folder-hyper-code"

        val initialFolders = listOf(
            VaultItem(
                id = folderDocsId,
                userId = userId,
                name = "Quantum Documents",
                isFolder = true,
                aiCategory = "FOLDER",
                aiTags = "documents, quantum, research, papers"
            ),
            VaultItem(
                id = folderMediaId,
                userId = userId,
                name = "Deep Space Media",
                isFolder = true,
                aiCategory = "FOLDER",
                aiTags = "media, astronomy, telemetry, photos"
            ),
            VaultItem(
                id = folderCodeId,
                userId = userId,
                name = "Neural Subsystems",
                isFolder = true,
                aiCategory = "FOLDER",
                aiTags = "code, neural, firmware, rust"
            )
        )
        dao.insertItems(initialFolders)

        val initialFiles = listOf(
            VaultItem(
                id = UUID.randomUUID().toString(),
                userId = userId,
                name = "Aeon_Whitepaper_v4.pdf",
                parentId = folderDocsId,
                isFolder = false,
                mimeType = "application/pdf",
                sizeBytes = 14_850_000L, // 14.8 MB
                extension = "pdf",
                isFavorite = true,
                sha256Checksum = "8f4b23a9e1d8892bc54091a137bfa546872a912a76f28b49c40337bfa0092110",
                aiCategory = "DOCUMENT",
                aiTags = "whitepaper, distributed-storage, quettabyte, architecture",
                aiSummary = "Technical specification of ÆonVault distributed storage matrix and 1 QB virtual addressing protocol.",
                textContentPreview = "ÆonVault: Distributed Exascale Storage Matrix Specification\nSection 1: 1 Quettabyte addressing space\nSection 2: Zero-loss cryptographic replication"
            ),
            VaultItem(
                id = UUID.randomUUID().toString(),
                userId = userId,
                name = "Andromeda_Telemetry_Spectra.png",
                parentId = folderMediaId,
                isFolder = false,
                mimeType = "image/png",
                sizeBytes = 28_400_000L, // 28.4 MB
                extension = "png",
                isFavorite = true,
                sha256Checksum = "c7d24a919028472bb51081a27bfa546872a912a76f28b49c40337bfa49102148",
                aiCategory = "IMAGE",
                aiTags = "astronomy, deep-space, telescope, spectrum",
                aiSummary = "Ultra high resolution optical spectroscopy capture from Orbital Telescope Array 9."
            ),
            VaultItem(
                id = UUID.randomUUID().toString(),
                userId = userId,
                name = "Quantum_Replication_Engine.rs",
                parentId = folderCodeId,
                isFolder = false,
                mimeType = "text/x-rust",
                sizeBytes = 428_000L,
                extension = "rs",
                isFavorite = false,
                sha256Checksum = "1a8b34f82819283bb91281a137bfa546872a912a76f28b49c40337bfa99831a2",
                aiCategory = "CODE",
                aiTags = "rust, systems, network, async, protocol",
                aiSummary = "Low-latency multiplexing daemon for parallel chunk transfers across 12 geo-replicated clusters.",
                textContentPreview = "// ÆonVault Chunk Transfer Engine\nuse tokio::sync::mpsc;\npub async fn stream_chunk(chunk_id: u64) -> Result<(), VaultError> {\n    println!(\"Routing chunk {} through distributed mesh\", chunk_id);\n    Ok(())\n}"
            ),
            VaultItem(
                id = UUID.randomUUID().toString(),
                userId = userId,
                name = "Orbital_Mission_Briefing.md",
                parentId = null,
                isFolder = false,
                mimeType = "text/markdown",
                sizeBytes = 85_400L,
                extension = "md",
                isFavorite = true,
                isShared = true,
                sha256Checksum = "3b7c91d89248271bb81928a137bfa546872a912a76f28b49c40337bfa1199321",
                aiCategory = "DOCUMENT",
                aiTags = "mission, markdown, roadmap, 2026",
                aiSummary = "Comprehensive overview of deep space communication relays and cloud synchronizer benchmarks.",
                textContentPreview = "# Orbital Mission Briefing\n\n## Objectives\n- Verify continuous connection to https://aeonvaultfilemanager.vercel.app\n- Test 1 QB logical quota provisioning across all regional nodes\n- Zero data degradation guarantees verified"
            ),
            VaultItem(
                id = UUID.randomUUID().toString(),
                userId = userId,
                name = "Vault_Security_Audit_2026.json",
                parentId = null,
                isFolder = false,
                mimeType = "application/json",
                sizeBytes = 192_000L,
                extension = "json",
                isFavorite = false,
                sha256Checksum = "9e2d31c89018472bb51081a27bfa546872a912a76f28b49c40337bfa77209144",
                aiCategory = "CODE",
                aiTags = "audit, security, compliance, zero-trust",
                aiSummary = "Cryptographic compliance logs demonstrating zero arbitrary deletions and permanent account persistence.",
                textContentPreview = "{\n  \"auditYear\": 2026,\n  \"standard\": \"ISO/IEC 27001 Quantum Extended\",\n  \"inactivityDeletionPolicy\": \"PROHIBITED\",\n  \"logicalQuota\": \"1 Quettabyte\",\n  \"status\": \"VERIFIED_SECURE\"\n}"
            )
        )
        dao.insertItems(initialFiles)

        // Seed initial activity logs
        val initialLogs = listOf(
            ActivityLog(
                id = UUID.randomUUID().toString(),
                userId = userId,
                actionType = "INITIALIZE",
                description = "Account provisioned with 1 QB logical storage quota on Æon Distributed Mesh",
                targetItemName = "System Root"
            ),
            ActivityLog(
                id = UUID.randomUUID().toString(),
                userId = userId,
                actionType = "SECURITY",
                description = "Permanent account persistence guarantees locked · Inactivity purge disabled",
                targetItemName = "Account Policy"
            ),
            ActivityLog(
                id = UUID.randomUUID().toString(),
                userId = userId,
                actionType = "UPLOAD",
                description = "Uploaded Aeon_Whitepaper_v4.pdf (14.8 MB) with 15 chunks",
                targetItemName = "Aeon_Whitepaper_v4.pdf"
            )
        )
        for (log in initialLogs) {
            dao.insertLog(log)
        }

        val totalUsed = dao.calculateUsedBytes(userId)
        dao.updateUserQuota(userId, totalUsed)
    }

    // --- User Auth & Account Persistence ---
    suspend fun register(username: String, email: String, password: String):Result<User> = withContext(Dispatchers.IO) {
        val existing = dao.getUserByEmail(email)
        if (existing != null) {
            return@withContext Result.failure(IllegalArgumentException("An account with this email already exists"))
        }
        val salt = UUID.randomUUID().toString().take(8)
        val hash = User.hashPassword(password, salt)
        val recoveryKey = "AEON-" + UUID.randomUUID().toString().take(12).uppercase()

        val newUser = User(
            id = "aeon-usr-" + UUID.randomUUID().toString().take(8),
            username = username,
            email = email,
            passwordHash = hash,
            salt = salt,
            recoveryKey = recoveryKey,
            quotaUsedBytes = 0L,
            planTier = "Æon Prime",
            avatarColorHex = "#00F5FF"
        )
        dao.insertUser(newUser)
        _currentUser.value = newUser
        saveLastActiveUser(newUser.id)
        seedDefaultVaultData(newUser.id)

        dao.insertLog(
            ActivityLog(
                id = UUID.randomUUID().toString(),
                userId = newUser.id,
                actionType = "REGISTER",
                description = "New permanent vault account registered with 1 QB logical quota",
                targetItemName = username
            )
        )
        Result.success(newUser)
    }

    suspend fun login(email: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        val user = dao.getUserByEmail(email)
            ?: return@withContext Result.failure(IllegalArgumentException("Account not found for email: $email"))
        val hash = User.hashPassword(password, user.salt)
        if (hash != user.passwordHash) {
            return@withContext Result.failure(IllegalArgumentException("Invalid credentials. Please verify your password."))
        }
        _currentUser.value = user
        saveLastActiveUser(user.id)
        dao.insertLog(
            ActivityLog(
                id = UUID.randomUUID().toString(),
                userId = user.id,
                actionType = "LOGIN",
                description = "Authenticated session established via Native Android",
                targetItemName = user.username
            )
        )
        Result.success(user)
    }

    suspend fun recoverAccount(email: String, recoveryKey: String, newPassword: String): Result<User> = withContext(Dispatchers.IO) {
        val user = dao.getUserByEmail(email)
            ?: return@withContext Result.failure(IllegalArgumentException("Account not found"))
        if (user.recoveryKey.trim().uppercase() != recoveryKey.trim().uppercase()) {
            return@withContext Result.failure(IllegalArgumentException("Recovery key does not match this account"))
        }
        val newSalt = UUID.randomUUID().toString().take(8)
        val newHash = User.hashPassword(newPassword, newSalt)
        val updatedUser = user.copy(passwordHash = newHash, salt = newSalt)
        dao.insertUser(updatedUser)
        _currentUser.value = updatedUser
        saveLastActiveUser(updatedUser.id)

        dao.insertLog(
            ActivityLog(
                id = UUID.randomUUID().toString(),
                userId = user.id,
                actionType = "RECOVER",
                description = "Password securely reset via Recovery Key verification",
                targetItemName = user.username
            )
        )
        Result.success(updatedUser)
    }

    fun logout() {
        _currentUser.value = null
        saveLastActiveUser(null)
    }

    fun switchAccount(user: User) {
        _currentUser.value = user
        saveLastActiveUser(user.id)
        scope.launch {
            refreshQuota(user.id)
            syncManager.triggerImmediateSync()
        }
    }

    suspend fun switchAccountById(userId: String) = withContext(Dispatchers.IO) {
        val user = dao.getUserByIdSync(userId)
        if (user != null) {
            _currentUser.value = user
            saveLastActiveUser(user.id)
            refreshQuota(user.id)
            syncManager.triggerImmediateSync()
        }
    }

    suspend fun addAccount(
        username: String,
        email: String,
        password: String,
        planTier: String = "Æon Prime"
    ): Result<User> = withContext(Dispatchers.IO) {
        val existing = dao.getUserByEmail(email.trim().lowercase())
        if (existing != null) {
            return@withContext Result.failure(IllegalArgumentException("An account with email '$email' already exists"))
        }
        val salt = UUID.randomUUID().toString().take(8)
        val hash = User.hashPassword(password, salt)
        val recoveryKey = "AEON-" + UUID.randomUUID().toString().take(12).uppercase()

        val colorOptions = listOf("#00F5FF", "#9D4EDD", "#10B981", "#F59E0B", "#EC4899", "#3B82F6")
        val chosenColor = colorOptions[Math.abs(email.hashCode()) % colorOptions.size]

        val newUser = User(
            id = "aeon-usr-" + UUID.randomUUID().toString().take(8),
            username = username.trim(),
            email = email.trim().lowercase(),
            passwordHash = hash,
            salt = salt,
            recoveryKey = recoveryKey,
            quotaUsedBytes = 0L,
            planTier = planTier,
            avatarColorHex = chosenColor
        )
        dao.insertUser(newUser)
        seedDefaultVaultData(newUser.id)
        _currentUser.value = newUser
        saveLastActiveUser(newUser.id)
        syncManager.triggerImmediateSync()

        dao.insertLog(
            ActivityLog(
                id = UUID.randomUUID().toString(),
                userId = newUser.id,
                actionType = "ACCOUNT_ADDED",
                description = "New vault account '$username' added with 1 QB logical quota",
                targetItemName = username
            )
        )
        Result.success(newUser)
    }

    suspend fun removeAccount(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        dao.deleteUser(userId)
        if (_currentUser.value?.id == userId) {
            val remaining = dao.getFirstUser()
            _currentUser.value = remaining
            saveLastActiveUser(remaining?.id)
        }
        syncManager.triggerImmediateSync()
        Result.success(Unit)
    }

    // --- Reactive File & Folder Operations ---
    fun getItemsInFolder(parentId: String?): Flow<List<VaultItem>> {
        val normalizedParentId = if (parentId.isNullOrBlank()) null else parentId
        return _currentUser.flatMapLatest { user ->
            if (user != null) {
                dao.getItemsInFolder(user.id, normalizedParentId)
            } else {
                flow {
                    val fallback = dao.getFirstUser()
                    if (fallback != null) {
                        _currentUser.value = fallback
                        emitAll(dao.getItemsInFolder(fallback.id, normalizedParentId))
                    } else {
                        emit(emptyList())
                    }
                }
            }
        }
    }

    fun getAllActiveItems(): Flow<List<VaultItem>> {
        return _currentUser.flatMapLatest { user ->
            if (user != null) {
                dao.getAllActiveItems(user.id)
            } else {
                flow {
                    val fallback = dao.getFirstUser()
                    if (fallback != null) {
                        _currentUser.value = fallback
                        emitAll(dao.getAllActiveItems(fallback.id))
                    } else {
                        emit(emptyList())
                    }
                }
            }
        }
    }

    fun getFavorites(): Flow<List<VaultItem>> {
        return _currentUser.flatMapLatest { user ->
            if (user != null) {
                dao.getFavorites(user.id)
            } else {
                flow {
                    val fallback = dao.getFirstUser()
                    if (fallback != null) {
                        _currentUser.value = fallback
                        emitAll(dao.getFavorites(fallback.id))
                    } else {
                        emit(emptyList())
                    }
                }
            }
        }
    }

    fun getSharedItems(): Flow<List<VaultItem>> {
        return _currentUser.flatMapLatest { user ->
            if (user != null) {
                dao.getSharedItems(user.id)
            } else {
                flow {
                    val fallback = dao.getFirstUser()
                    if (fallback != null) {
                        _currentUser.value = fallback
                        emitAll(dao.getSharedItems(fallback.id))
                    } else {
                        emit(emptyList())
                    }
                }
            }
        }
    }

    fun getTrashItems(): Flow<List<VaultItem>> {
        return _currentUser.flatMapLatest { user ->
            if (user != null) {
                dao.getTrashItems(user.id)
            } else {
                flow {
                    val fallback = dao.getFirstUser()
                    if (fallback != null) {
                        _currentUser.value = fallback
                        emitAll(dao.getTrashItems(fallback.id))
                    } else {
                        emit(emptyList())
                    }
                }
            }
        }
    }

    fun getRecentItems(limit: Int = 10): Flow<List<VaultItem>> {
        return _currentUser.flatMapLatest { user ->
            if (user != null) {
                dao.getRecentItems(user.id, limit)
            } else {
                flow {
                    val fallback = dao.getFirstUser()
                    if (fallback != null) {
                        _currentUser.value = fallback
                        emitAll(dao.getRecentItems(fallback.id, limit))
                    } else {
                        emit(emptyList())
                    }
                }
            }
        }
    }

    fun searchItems(query: String): Flow<List<VaultItem>> {
        return _currentUser.flatMapLatest { user ->
            if (user != null) {
                dao.searchItems(user.id, query)
            } else {
                flow {
                    val fallback = dao.getFirstUser()
                    if (fallback != null) {
                        _currentUser.value = fallback
                        emitAll(dao.searchItems(fallback.id, query))
                    } else {
                        emit(emptyList())
                    }
                }
            }
        }
    }

    suspend fun createFolder(name: String, parentId: String?): Result<VaultItem> = withContext(Dispatchers.IO) {
        var user = _currentUser.value
        if (user == null) {
            user = dao.getFirstUser()
            if (user != null) {
                _currentUser.value = user
            }
        }
        if (user == null) {
            return@withContext Result.failure(IllegalStateException("No active account"))
        }

        val trimmedName = name.trim()
        if (trimmedName.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Directory name cannot be empty"))
        }

        val normalizedParentId = if (parentId.isNullOrBlank()) null else parentId

        // Check if directory with same name already exists in this folder
        val existing = dao.findItemByNameInFolder(user.id, trimmedName, normalizedParentId)
        if (existing != null) {
            return@withContext Result.failure(IllegalArgumentException("An item or directory named '$trimmedName' already exists in this location"))
        }

        val folder = VaultItem(
            id = "dir-" + UUID.randomUUID().toString().take(12),
            userId = user.id,
            name = trimmedName,
            parentId = normalizedParentId,
            isFolder = true,
            mimeType = "inode/directory",
            sizeBytes = 0L,
            extension = "",
            aiCategory = "FOLDER",
            aiTags = "folder, directory, vault",
            aiSummary = "Directory container for vault items."
        )
        dao.insertItem(folder)
        dao.insertLog(
            ActivityLog(
                id = UUID.randomUUID().toString(),
                userId = user.id,
                actionType = "CREATE_FOLDER",
                description = "Created directory '$trimmedName'",
                targetItemName = trimmedName
            )
        )
        Result.success(folder)
    }

    suspend fun exportFolderAsZip(folderId: String): Result<java.io.File> = withContext(Dispatchers.IO) {
        var user = _currentUser.value
        if (user == null) {
            user = dao.getFirstUser()
            if (user != null) {
                _currentUser.value = user
            }
        }
        if (user == null) {
            return@withContext Result.failure(IllegalStateException("No active account"))
        }

        val result = com.example.engine.zip.FolderZipExporter.exportFolderAsZip(context, dao, user.id, folderId)
        result.onSuccess { zipFile ->
            val folder = dao.getItemByIdSync(folderId)
            val folderName = folder?.name ?: "Directory"
            dao.insertLog(
                ActivityLog(
                    id = UUID.randomUUID().toString(),
                    userId = user.id,
                    actionType = "EXPORT_ZIP",
                    description = "Exported directory '$folderName' as ZIP archive (${User.formatStorageSize(zipFile.length())})",
                    targetItemName = zipFile.name
                )
            )
        }
        result
    }

    suspend fun renameItem(item: VaultItem, newName: String) = withContext(Dispatchers.IO) {
        val ext = if (item.isFolder) "" else newName.substringAfterLast('.', item.extension)
        val updated = item.copy(name = newName, extension = ext, modifiedAt = System.currentTimeMillis())
        dao.updateItem(updated)
        dao.insertLog(
            ActivityLog(
                id = UUID.randomUUID().toString(),
                userId = item.userId,
                actionType = "RENAME",
                description = "Renamed '${item.name}' to '$newName'",
                targetItemName = newName
            )
        )
    }

    suspend fun moveItem(item: VaultItem, targetFolderId: String?) = withContext(Dispatchers.IO) {
        val updated = item.copy(parentId = targetFolderId, modifiedAt = System.currentTimeMillis())
        dao.updateItem(updated)
        dao.insertLog(
            ActivityLog(
                id = UUID.randomUUID().toString(),
                userId = item.userId,
                actionType = "MOVE",
                description = "Moved '${item.name}' to folder ID: ${targetFolderId ?: "Root"}",
                targetItemName = item.name
            )
        )
    }

    suspend fun toggleFavorite(item: VaultItem) = withContext(Dispatchers.IO) {
        val updated = item.copy(isFavorite = !item.isFavorite)
        dao.updateItem(updated)
        dao.insertLog(
            ActivityLog(
                id = UUID.randomUUID().toString(),
                userId = item.userId,
                actionType = "FAVORITE",
                description = if (updated.isFavorite) "Added to favorites" else "Removed from favorites",
                targetItemName = item.name
            )
        )
    }

    suspend fun moveToTrash(item: VaultItem) = withContext(Dispatchers.IO) {
        val updated = item.copy(
            isTrash = true,
            trashedAt = System.currentTimeMillis(),
            modifiedAt = System.currentTimeMillis()
        )
        dao.updateItem(updated)
        refreshQuota(item.userId)
        dao.insertLog(
            ActivityLog(
                id = UUID.randomUUID().toString(),
                userId = item.userId,
                actionType = "TRASH",
                description = "Moved '${item.name}' to Trash (can be restored anytime)",
                targetItemName = item.name
            )
        )
    }

    suspend fun restoreFromTrash(item: VaultItem) = withContext(Dispatchers.IO) {
        val updated = item.copy(
            isTrash = false,
            trashedAt = null,
            modifiedAt = System.currentTimeMillis()
        )
        dao.updateItem(updated)
        refreshQuota(item.userId)
        dao.insertLog(
            ActivityLog(
                id = UUID.randomUUID().toString(),
                userId = item.userId,
                actionType = "RESTORE",
                description = "Restored '${item.name}' from Trash to active vault",
                targetItemName = item.name
            )
        )
    }

    suspend fun permanentlyDelete(item: VaultItem) = withContext(Dispatchers.IO) {
        dao.deleteItem(item)
        refreshQuota(item.userId)
        dao.insertLog(
            ActivityLog(
                id = UUID.randomUUID().toString(),
                userId = item.userId,
                actionType = "PERMANENT_DELETE",
                description = "Permanently purged '${item.name}'",
                targetItemName = item.name
            )
        )
    }

    suspend fun batchPermanentlyDelete(items: List<VaultItem>) = withContext(Dispatchers.IO) {
        if (items.isEmpty()) return@withContext
        val userId = items.first().userId
        dao.deleteItems(items)
        refreshQuota(userId)
        dao.insertLog(
            ActivityLog(
                id = UUID.randomUUID().toString(),
                userId = userId,
                actionType = "BATCH_DELETE",
                description = "Permanently purged ${items.size} items",
                targetItemName = "Batch Action"
            )
        )
    }

    suspend fun batchMoveToTrash(items: List<VaultItem>) = withContext(Dispatchers.IO) {
        if (items.isEmpty()) return@withContext
        val userId = items.first().userId
        val updatedItems = items.map {
            it.copy(
                isTrash = true,
                trashedAt = System.currentTimeMillis(),
                modifiedAt = System.currentTimeMillis()
            )
        }
        dao.updateItems(updatedItems)
        refreshQuota(userId)
        dao.insertLog(
            ActivityLog(
                id = UUID.randomUUID().toString(),
                userId = userId,
                actionType = "BATCH_TRASH",
                description = "Moved ${items.size} items to Trash",
                targetItemName = "Batch Action"
            )
        )
    }

    suspend fun batchRestoreFromTrash(items: List<VaultItem>) = withContext(Dispatchers.IO) {
        if (items.isEmpty()) return@withContext
        val userId = items.first().userId
        val updatedItems = items.map {
            it.copy(
                isTrash = false,
                trashedAt = null,
                modifiedAt = System.currentTimeMillis()
            )
        }
        dao.updateItems(updatedItems)
        refreshQuota(userId)
        dao.insertLog(
            ActivityLog(
                id = UUID.randomUUID().toString(),
                userId = userId,
                actionType = "BATCH_RESTORE",
                description = "Restored ${items.size} items from Trash",
                targetItemName = "Batch Action"
            )
        )
    }

    suspend fun batchDownload(items: List<VaultItem>): Result<java.io.File> = withContext(Dispatchers.IO) {
        var user = _currentUser.value
        if (user == null) {
            user = dao.getFirstUser()
            if (user != null) {
                _currentUser.value = user
            }
        }
        if (user == null) {
            return@withContext Result.failure(IllegalStateException("No active account"))
        }

        val result = com.example.engine.zip.FolderZipExporter.exportItemsAsZip(context, dao, user.id, items)
        result.onSuccess { zipFile ->
            dao.insertLog(
                ActivityLog(
                    id = UUID.randomUUID().toString(),
                    userId = user.id,
                    actionType = "BATCH_DOWNLOAD",
                    description = "Batch downloaded ${items.size} items as ZIP archive (${User.formatStorageSize(zipFile.length())})",
                    targetItemName = zipFile.name
                )
            )
        }
        result
    }

    suspend fun emptyTrash() = withContext(Dispatchers.IO) {
        val userId = _currentUser.value?.id ?: return@withContext
        dao.emptyTrash(userId)
        refreshQuota(userId)
        dao.insertLog(
            ActivityLog(
                id = UUID.randomUUID().toString(),
                userId = userId,
                actionType = "EMPTY_TRASH",
                description = "Emptied all items from Trash",
                targetItemName = "Trash"
            )
        )
    }

    suspend fun refreshQuota(userId: String) {
        val used = dao.calculateUsedBytes(userId)
        dao.updateUserQuota(userId, used)
        val user = dao.getUserByIdSync(userId)
        if (user != null && _currentUser.value?.id == userId) {
            _currentUser.value = user
        }
    }

    // --- Uploads ---
    fun getUploadTasks(): Flow<List<UploadTask>> {
        return _currentUser.flatMapLatest { user ->
            val userId = user?.id ?: ""
            dao.getAllTasks(userId)
        }
    }

    fun enqueueUpload(uri: Uri, name: String, mime: String, size: Long, parentId: String?): String {
        val userId = _currentUser.value?.id ?: return ""
        return uploadEngine.enqueueUpload(userId, uri, name, mime, size, parentId)
    }

    fun pauseUpload(taskId: String) = uploadEngine.pauseUpload(taskId)
    fun resumeUpload(taskId: String) = uploadEngine.resumeUpload(taskId)
    fun cancelUpload(taskId: String) = uploadEngine.cancelUpload(taskId)

    // --- Sharing ---
    fun getShareLinks(): Flow<List<ShareLink>> {
        return _currentUser.flatMapLatest { user ->
            val userId = user?.id ?: ""
            dao.getShareLinksForUser(userId)
        }
    }

    suspend fun createShareLink(
        item: VaultItem,
        accessType: String,
        isPasswordProtected: Boolean,
        password: String?,
        allowDownload: Boolean,
        expiresInHours: Long?
    ): ShareLink = withContext(Dispatchers.IO) {
        val linkId = UUID.randomUUID().toString().take(10)
        val expiry = expiresInHours?.let { System.currentTimeMillis() + it * 3600 * 1000 }
        val link = ShareLink(
            id = linkId,
            itemId = item.id,
            itemName = item.name,
            userId = item.userId,
            accessType = accessType,
            isPasswordProtected = isPasswordProtected,
            passwordHash = password?.let { User.hashPassword(it, "aeon-share") },
            allowDownload = allowDownload,
            expiresAt = expiry
        )
        dao.insertShareLink(link)
        val updatedItem = item.copy(isShared = true)
        dao.updateItem(updatedItem)

        dao.insertLog(
            ActivityLog(
                id = UUID.randomUUID().toString(),
                userId = item.userId,
                actionType = "SHARE",
                description = "Created $accessType share link for '${item.name}' (ID: $linkId)",
                targetItemName = item.name
            )
        )
        link
    }

    suspend fun revokeShareLink(link: ShareLink) = withContext(Dispatchers.IO) {
        dao.revokeShareLink(link.id)
        dao.insertLog(
            ActivityLog(
                id = UUID.randomUUID().toString(),
                userId = link.userId,
                actionType = "REVOKE_SHARE",
                description = "Revoked share link for '${link.itemName}'",
                targetItemName = link.itemName
            )
        )
    }

    // --- Activity Logs ---
    fun getActivityLogs(): Flow<List<ActivityLog>> {
        return _currentUser.flatMapLatest { user ->
            val userId = user?.id ?: ""
            dao.getLogsForUser(userId)
        }
    }
}
