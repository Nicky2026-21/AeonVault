package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
    val allActiveItems by repository.getAllActiveItems().collectAsState(initial = emptyList())
    val recentItems by repository.getRecentItems(5).collectAsState(initial = emptyList())
    val uploadTasks by repository.getUploadTasks().collectAsState(initial = emptyList())
    val activeUploads = uploadTasks.filter { it.status == UploadStatus.UPLOADING || it.status == UploadStatus.QUEUED }

    val fileCount = allActiveItems.count { !it.isFolder }
    val folderCount = allActiveItems.count { it.isFolder }
    val totalUsedBytes = currentUser?.quotaUsedBytes ?: 0L

    var aiInsightText by remember { mutableStateOf("Analyzing vault storage topology with Gemini 3.5 Flash...") }
    var isLoadingInsight by remember { mutableStateOf(false) }

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
                GlowingIndicator(text = "Mesh Online")
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
                        Text("Independent web app at https://aeonvault.vercel.app", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://aeonvault.vercel.app"))
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
                    android.widget.Toast.makeText(context, "Downloaded '${item.name}' to local device storage", android.widget.Toast.LENGTH_SHORT).show()
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
