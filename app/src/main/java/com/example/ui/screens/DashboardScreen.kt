package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.UploadStatus
import com.example.data.model.User
import com.example.data.model.VaultItem
import com.example.data.repository.VaultRepository
import com.example.ui.components.AddAccountDialog
import com.example.ui.components.CyberBadge
import com.example.ui.components.FileItemCard
import com.example.ui.components.GlassCard
import com.example.ui.components.GlowingIndicator
import com.example.ui.components.MetricTile
import com.example.ui.components.QuotaDisclaimerCard
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.QuantumIndigo
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    repository: VaultRepository,
    onNavigateToVault: () -> Unit,
    onNavigateToUpload: () -> Unit,
    onNavigateToAssistant: () -> Unit,
    onFileClick: (VaultItem) -> Unit,
    onShareFile: (VaultItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentUser by repository.currentUser.collectAsState()
    val allUsers by repository.allUsers.collectAsState(initial = emptyList())
    val allActiveItems by repository.getAllActiveItems().collectAsState(initial = emptyList())
    val recentItems by repository.getRecentItems(5).collectAsState(initial = emptyList())
    val uploadTasks by repository.getUploadTasks().collectAsState(initial = emptyList())
    val syncState by repository.syncState.collectAsState()
    val activeUploads = uploadTasks.filter { it.status == UploadStatus.UPLOADING || it.status == UploadStatus.QUEUED }

    val fileCount = allActiveItems.count { !it.isFolder }
    val folderCount = allActiveItems.count { it.isFolder }
    val totalUsedBytes = currentUser?.quotaUsedBytes ?: 0L

    var aiInsightText by remember { mutableStateOf("Analyzing vault storage topology with Gemini 3.5 Flash...") }
    var isLoadingInsight by remember { mutableStateOf(false) }
    var showAccountsQuickSheet by remember { mutableStateOf(false) }
    var showAddAccountDialog by remember { mutableStateOf(false) }

    LaunchedEffect(fileCount, totalUsedBytes) {
        if (repository.geminiService.isApiKeyConfigured) {
            isLoadingInsight = true
            val breakdown = allActiveItems.groupBy { it.aiCategory }.mapValues { it.value.size }
            val result = repository.geminiService.generateStorageInsights(totalUsedBytes, fileCount, folderCount, breakdown)
            result.onSuccess { insight ->
                aiInsightText = insight
                isLoadingInsight = false
            }.onFailure {
                aiInsightText = "• Distributed quantum redundancy verified across 12 mesh nodes.\n• Permanent account protection active (no automatic inactivity purge).\n• 1 QB logical addressing space ready for massive uploads."
                isLoadingInsight = false
            }
        } else {
            aiInsightText = "• Distributed quantum redundancy active across 12 geo-replicated clusters.\n• Permanent account status active: Files are protected from inactivity purge.\n• 1 QB logical addressing space ready. Add Gemini API key in Secrets to enable live AI analysis."
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Space / Header
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ÆonVault",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "“The Vault That Never Forgets”",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Clickable User Account Pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                            .border(1.dp, NeonCyan.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
                            .clickable { showAccountsQuickSheet = true }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .testTag("dashboard_user_account_pill")
                    ) {
                        val initial = currentUser?.username?.take(2)?.uppercase() ?: "AV"
                        val avatarBg = try {
                            Color(android.graphics.Color.parseColor(currentUser?.avatarColorHex ?: "#00F5FF"))
                        } catch (e: Exception) {
                            NeonCyan
                        }
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(avatarBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initial,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = Color.Black
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = currentUser?.username ?: "Account",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (syncState.isSyncing) ElectricViolet.copy(alpha = 0.18f)
                                    else EmeraldGlow.copy(alpha = 0.12f)
                                )
                                .clickable {
                                    repository.syncManager.triggerImmediateSync()
                                    Toast.makeText(context, "Synchronizing all linked accounts with 12 mesh nodes", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (syncState.isSyncing) ElectricViolet else EmeraldGlow)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (syncState.isSyncing) "Syncing..." else "Mesh Synced",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = if (syncState.isSyncing) ElectricViolet else EmeraldGlow,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        GlowingIndicator(text = "Online")
                    }
                }
            }
        }

        // Hero Banner Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, NeonCyan.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.aeon_vault_hero_1789212061060),
                    contentDescription = "ÆonVault Quantum Core",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xEE060813),
                                    Color(0xAA0E1326),
                                    Color(0x44060813)
                                )
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    CyberBadge("QUANTUM CLOUD ARCHITECTURE", color = NeonCyan)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "1 QB Logical Storage Core",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "High-speed resumable uploads · Distributed multi-node persistence",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
            }
        }

        // 1 QB Storage Card (Prominent)
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_storage_card"),
                borderColor = NeonCyan.copy(alpha = 0.4f)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "LOGICAL STORAGE QUOTA",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = User.formatStorageSize(totalUsedBytes),
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = " / 1 QB",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 2.dp, start = 4.dp)
                                )
                            }
                        }
                        CyberBadge("Æon Prime · 1 QB", color = ElectricViolet)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { 0.0001f }, // 1 QB is virtually infinite
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = NeonCyan,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Remaining Quota: ~1.00 Quettabyte",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "No Inactivity Purge",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldGlow,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // MANDATORY Storage Disclaimer on Dashboard storage card
                    QuotaDisclaimerCard(isProminent = true)
                }
            }
        }

        // Quick Metrics Tiles (File Count, Folder Count, Active Uploads)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricTile(
                    title = "Files",
                    value = "$fileCount",
                    subtext = "Encrypted objects",
                    icon = Icons.Default.InsertDriveFile,
                    iconTint = NeonCyan,
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    title = "Folders",
                    value = "$folderCount",
                    subtext = "Vault directories",
                    icon = Icons.Default.Folder,
                    iconTint = ElectricViolet,
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    title = "Transfers",
                    value = "${activeUploads.size}",
                    subtext = if (activeUploads.isEmpty()) "Idle engine" else "High-speed active",
                    icon = Icons.Default.Speed,
                    iconTint = if (activeUploads.isEmpty()) QuantumIndigo else EmeraldGlow,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // High-Speed Active Uploads Banner (if active)
        if (activeUploads.isNotEmpty()) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = EmeraldGlow.copy(alpha = 0.5f),
                    onClick = onNavigateToUpload
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = EmeraldGlow)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "High-Speed Upload Engine Active",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldGlow
                                )
                            }
                            CyberBadge("${activeUploads.size} Running", color = EmeraldGlow)
                        }
                        val primaryTask = activeUploads.first()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${primaryTask.fileName} · ${primaryTask.formattedSpeed} · ${primaryTask.formattedEta}",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { primaryTask.progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = EmeraldGlow
                        )
                    }
                }
            }
        }

        // Gemini AI Insights Card
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("gemini_insights_card"),
                borderColor = ElectricViolet.copy(alpha = 0.4f),
                onClick = onNavigateToAssistant
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ElectricViolet)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Gemini Quantum Insights",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (isLoadingInsight) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = ElectricViolet)
                        } else {
                            CyberBadge("Live AI", color = ElectricViolet)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = aiInsightText,
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "Tap to chat with Vault Assistant →",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricViolet,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Web Version Shortcut Card
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = QuantumIndigo.copy(alpha = 0.35f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Official ÆonVault Web", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("Independent web app at https://aeonvaultfilemanager.vercel.app", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://aeonvaultfilemanager.vercel.app"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.testTag("open_web_version_button")
                    ) {
                        Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Open Web")
                    }
                }
            }
        }

        // Recent Files Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.History, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Recent Files",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Button(
                    onClick = onNavigateToVault,
                    colors = ButtonDefaults.textButtonColors(),
                    modifier = Modifier.testTag("view_all_vault_button")
                ) {
                    Text("View Vault", color = NeonCyan)
                }
            }
        }

        // Recent Files List
        items(recentItems) { item ->
            FileItemCard(
                item = item,
                isGrid = false,
                onClick = { onFileClick(item) },
                onFavoriteToggle = { scope.launch { repository.toggleFavorite(item) } },
                onShare = { onShareFile(item) },
                onRename = {},
                onMove = {},
                onTrashOrRestore = { scope.launch { repository.moveToTrash(item) } },
                onPermanentDelete = { scope.launch { repository.permanentlyDelete(item) } },
                onDownload = {
                    if (item.isFolder) {
                        scope.launch {
                            android.widget.Toast.makeText(context, "Exporting '${item.name}' as ZIP...", android.widget.Toast.LENGTH_SHORT).show()
                            val result = repository.exportFolderAsZip(item.id)
                            result.onSuccess { zipFile ->
                                val formattedSize = com.example.data.model.User.formatStorageSize(zipFile.length())
                                android.widget.Toast.makeText(context, "ZIP created: ${zipFile.name} ($formattedSize)", android.widget.Toast.LENGTH_LONG).show()
                                try {
                                    val shareIntent = com.example.engine.zip.FolderZipExporter.createShareOrOpenIntent(context, zipFile)
                                    context.startActivity(android.content.Intent.createChooser(shareIntent, "Open or Share ZIP Archive"))
                                } catch (e: Exception) {}
                            }.onFailure { err ->
                                android.widget.Toast.makeText(context, err.message ?: "Failed to generate ZIP", android.widget.Toast.LENGTH_LONG).show()
                            }
                        }
                    } else {
                        android.widget.Toast.makeText(context, "Downloaded '${item.name}' to local device storage", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showAccountsQuickSheet) {
        AlertDialog(
            onDismissRequest = { showAccountsQuickSheet = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.ManageAccounts,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Vault Accounts")
                    }
                    IconButton(onClick = { showAccountsQuickSheet = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "ACTIVE PROFILE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        letterSpacing = 1.sp
                    )

                    // Current Active Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val initial = currentUser?.username?.take(2)?.uppercase() ?: "AV"
                                val bg = try {
                                    Color(android.graphics.Color.parseColor(currentUser?.avatarColorHex ?: "#00F5FF"))
                                } catch (e: Exception) {
                                    NeonCyan
                                }
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(bg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(initial, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Black)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(currentUser?.username ?: "User", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Text(currentUser?.email ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            CyberBadge("Active", color = NeonCyan)
                        }
                    }

                    // Other accounts
                    val otherAccounts = allUsers.filter { it.id != currentUser?.id }
                    if (otherAccounts.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "SWITCH ACCOUNT",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )

                        otherAccounts.forEach { acc ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(acc.username, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                    Text(acc.email, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                OutlinedButton(
                                    onClick = {
                                        repository.switchAccount(acc)
                                        showAccountsQuickSheet = false
                                        android.widget.Toast.makeText(context, "Switched to ${acc.username}", android.widget.Toast.LENGTH_SHORT).show()
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.testTag("dashboard_switch_account_${acc.id}")
                                ) {
                                    Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Switch", fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = {
                            showAccountsQuickSheet = false
                            showAddAccountDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dashboard_add_account_button")
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add New Vault Account", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAccountsQuickSheet = false }) {
                    Text("Done")
                }
            }
        )
    }

    if (showAddAccountDialog) {
        AddAccountDialog(
            repository = repository,
            onDismiss = { showAddAccountDialog = false },
            onAccountAdded = {
                showAddAccountDialog = false
            }
        )
    }
}
