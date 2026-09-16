package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.data.repository.VaultRepository
import com.example.ui.components.AddAccountDialog
import com.example.ui.components.CyberBadge
import com.example.ui.components.GlassCard
import com.example.ui.components.QuotaDisclaimerCard
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.CoralNeon
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.NeonCyan
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    repository: VaultRepository,
    isDarkTheme: Boolean,
    onToggleDarkTheme: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentUser by repository.currentUser.collectAsState()
    val allUsers by repository.allUsers.collectAsState(initial = emptyList())
    val syncState by repository.syncState.collectAsState()

    var configuredWebUrl by remember { mutableStateOf("https://aeonvaultfilemanager.vercel.app") }
    var isEditingWebUrl by remember { mutableStateOf(false) }
    var tempWebUrl by remember { mutableStateOf("https://aeonvaultfilemanager.vercel.app") }
    var showDeploymentInfoDialog by remember { mutableStateOf(false) }
    var showAddAccountDialog by remember { mutableStateOf(false) }
    var userToDelete by remember { mutableStateOf<User?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Settings & Vault Core",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Infrastructure · Quota · Web Deployment",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                CyberBadge("Node Config", color = NeonCyan)
            }
        }

        // Multi-Account Management & Identity Section
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_accounts_card"),
                borderColor = NeonCyan.copy(alpha = 0.4f)
            ) {
                Column {
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
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Vault Accounts (${allUsers.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Button(
                            onClick = { showAddAccountDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("settings_add_account_button")
                        ) {
                            Icon(
                                Icons.Default.PersonAdd,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Add Account",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Active Account Card
                    Text(
                        "CURRENT ACTIVE ACCOUNT",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                            .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val userInitial = currentUser?.username?.take(2)?.uppercase() ?: "AV"
                                    val avatarBg = try {
                                        Color(android.graphics.Color.parseColor(currentUser?.avatarColorHex ?: "#00F5FF"))
                                    } catch (e: Exception) {
                                        NeonCyan
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(avatarBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = userInitial,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color.Black
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = currentUser?.username ?: "NexusCommander",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = currentUser?.email ?: "vault.commander@aeonvaultfilemanager.vercel.app",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                CyberBadge("Active · ${currentUser?.planTier ?: "Æon Prime"}", color = NeonCyan)
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Key, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Recovery Key: ${currentUser?.recoveryKey ?: "AEON-RECOVER-KEY"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Recovery Key", currentUser?.recoveryKey ?: ""))
                                        Toast.makeText(context, "Recovery key copied", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy Recovery Key", modifier = Modifier.size(16.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = EmeraldGlow, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Permanent account protected · Inactivity deletion prohibited",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EmeraldGlow,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Other Saved Accounts list (if more than 1)
                    val otherUsers = allUsers.filter { it.id != currentUser?.id }
                    if (otherUsers.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            "OTHER SAVED VAULT ACCOUNTS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            otherUsers.forEach { user ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        val initial = user.username.take(2).uppercase()
                                        val bg = try {
                                            Color(android.graphics.Color.parseColor(user.avatarColorHex))
                                        } catch (e: Exception) {
                                            ElectricViolet
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(bg),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = initial,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = Color.Black
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = user.username,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = "${user.email} · ${user.planTier}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        OutlinedButton(
                                            onClick = {
                                                repository.switchAccount(user)
                                                Toast.makeText(context, "Switched to account '${user.username}'", Toast.LENGTH_SHORT).show()
                                            },
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.testTag("switch_account_${user.id}")
                                        ) {
                                            Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Switch", fontSize = 11.sp)
                                        }
                                        Spacer(modifier = Modifier.width(4.dp))
                                        IconButton(
                                            onClick = { userToDelete = user },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Remove account from device",
                                                tint = CoralNeon.copy(alpha = 0.8f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Background Multi-Account Sync Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (syncState.isSyncing) ElectricViolet else EmeraldGlow)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (syncState.isSyncing) "Background Sync Active..." else "Continuous Mesh Sync Active",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (syncState.isSyncing) ElectricViolet else EmeraldGlow
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "All ${allUsers.size} accounts synced across 12 distributed nodes · Seamless switching",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                repository.syncManager.triggerImmediateSync()
                                Toast.makeText(context, "Synchronizing all ${allUsers.size} accounts", Toast.LENGTH_SHORT).show()
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("sync_all_accounts_now_button")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp), tint = NeonCyan)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sync Now", fontSize = 11.sp, color = NeonCyan)
                        }
                    }
                }
            }
        }

        // MANDATORY: Storage & Quota Section with REQUIRED Disclaimer
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_storage_quota_section"),
                borderColor = NeonCyan.copy(alpha = 0.4f)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Storage, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Storage & Quota Allocation", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        CyberBadge("1 QB", color = NeonCyan)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Current Used: ${User.formatStorageSize(currentUser?.quotaUsedBytes ?: 0L)} of 1 Quettabyte",
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Virtual Logical Addressing Matrix: 1,000,000,000,000,000,000,000,000,000,000 bytes",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // MANDATORY Storage Disclaimer on Settings -> Storage & Quota
                    QuotaDisclaimerCard(isProminent = true)
                }
            }
        }

        // Official Domain & Web Application Integration Card
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = ElectricViolet.copy(alpha = 0.35f)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Language, contentDescription = null, tint = ElectricViolet, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Web App & Vercel Deployment", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        CyberBadge("Deployment Guide", color = ElectricViolet)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Target Domain: $configuredWebUrl",
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "The web application works independently from the Android app across browsers (Safari, Chrome, Edge, Firefox) on Android, iOS, iPad, macOS, Linux, and Windows.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Helpful Vercel DEPLOYMENT_NOT_FOUND explanation box
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = AmberAlert, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Notice: 'DEPLOYMENT_NOT_FOUND' occurs until the repository is deployed to Vercel.",
                                style = MaterialTheme.typography.labelSmall,
                                color = AmberAlert
                            )
                        }
                        TextButton(
                            onClick = { showDeploymentInfoDialog = true },
                            modifier = Modifier.align(Alignment.Start)
                        ) {
                            Icon(Icons.Default.HelpOutline, contentDescription = null, modifier = Modifier.size(16.dp), tint = NeonCyan)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("How to Fix DEPLOYMENT_NOT_FOUND & Deploy", color = NeonCyan, fontSize = 12.sp)
                        }
                    }

                    if (isEditingWebUrl) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = tempWebUrl,
                            onValueChange = { tempWebUrl = it },
                            label = { Text("Web App URL / Custom Domain") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    if (tempWebUrl.isNotBlank()) {
                                        configuredWebUrl = tempWebUrl.trim()
                                        isEditingWebUrl = false
                                        Toast.makeText(context, "Updated Web URL", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Text("Save URL")
                            }
                            OutlinedButton(
                                onClick = {
                                    configuredWebUrl = "https://aeonvaultfilemanager.vercel.app"
                                    tempWebUrl = "https://aeonvaultfilemanager.vercel.app"
                                    isEditingWebUrl = false
                                }
                            ) {
                                Text("Reset Default")
                            }
                        }
                    } else {
                        TextButton(onClick = { isEditingWebUrl = true; tempWebUrl = configuredWebUrl }) {
                            Text("Edit or Change Web URL", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(configuredWebUrl))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Cannot open URL: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth().testTag("launch_aeonvault_web_button")
                    ) {
                        Icon(Icons.Default.OpenInBrowser, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open Web Version in Browser", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }

        // Theme & UI Preferences
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DarkMode, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Cyber Dark Theme", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text("Futuristic neon contrast mode", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Switch(checked = isDarkTheme, onCheckedChange = { onToggleDarkTheme() }, modifier = Modifier.testTag("theme_switch"))
                }
            }
        }

        // Logout
        item {
            OutlinedButton(
                onClick = onLogout,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralNeon),
                modifier = Modifier.fillMaxWidth().testTag("logout_button")
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out / Switch Account")
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showDeploymentInfoDialog) {
        AlertDialog(
            onDismissRequest = { showDeploymentInfoDialog = false },
            title = { Text("Why DEPLOYMENT_NOT_FOUND Happens") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Vercel returns 'DEPLOYMENT_NOT_FOUND' because Vercel requires connecting the repository to an active Vercel project deployment.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "How to Deploy to Vercel in 3 Steps:\n" +
                                "1. Export this repository or push to your GitHub account.\n" +
                                "2. Log into https://vercel.com/new and click 'Import' for your repository.\n" +
                                "3. Set the project name to 'aeonvaultfilemanager' and click 'Deploy'. The included vercel.json and public/index.html will instantly build and serve the live web application!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "If your deployment generates a different URL, you can paste it into 'Edit Web URL' above.",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonCyan
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showDeploymentInfoDialog = false }) {
                    Text("Got It")
                }
            }
        )
    }

    if (showAddAccountDialog) {
        AddAccountDialog(
            repository = repository,
            onDismiss = { showAddAccountDialog = false },
            onAccountAdded = { newAccount ->
                showAddAccountDialog = false
            }
        )
    }

    if (userToDelete != null) {
        val target = userToDelete!!
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            title = { Text("Remove Account from Device?") },
            text = {
                Text(
                    "Are you sure you want to remove '${target.username}' (${target.email}) from this device?\n\nYour encrypted files in the vault partition remain safely preserved on the distributed mesh network."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            repository.removeAccount(target.id)
                            Toast.makeText(context, "Account '${target.username}' removed from device", Toast.LENGTH_SHORT).show()
                            userToDelete = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralNeon)
                ) {
                    Text("Remove", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { userToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
