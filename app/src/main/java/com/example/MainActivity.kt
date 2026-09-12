package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VaultItem
import com.example.data.repository.VaultRepository
import com.example.ui.components.FilePreviewModal
import com.example.ui.components.ShareDialog
import com.example.ui.screens.ActivityScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.GeminiAssistantScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SharedScreen
import com.example.ui.screens.UploadScreen
import com.example.ui.screens.VaultScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyan
import kotlinx.coroutines.launch

enum class AppDestination(val label: String, val icon: ImageVector) {
    DASHBOARD("Overview", Icons.Default.Dashboard),
    VAULT("Vault", Icons.Default.Folder),
    UPLOAD("Upload", Icons.Default.CloudUpload),
    ASSISTANT("AI Assistant", Icons.Default.AutoAwesome),
    SHARED("Shared", Icons.Default.Share),
    ACTIVITY("Activity", Icons.Default.History),
    SETTINGS("Settings", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {
    private lateinit var repository: VaultRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        repository = VaultRepository(applicationContext)

        handleIncomingIntents(intent)

        setContent {
            var isDarkTheme by remember { mutableStateOf(true) }
            val currentUser by repository.currentUser.collectAsState()

            MyApplicationTheme(darkTheme = isDarkTheme) {
                if (currentUser == null) {
                    AuthScreen(
                        repository = repository,
                        onAuthSuccess = {
                            // User state automatically updates through StateFlow
                        }
                    )
                } else {
                    AeonVaultApp(
                        repository = repository,
                        isDarkTheme = isDarkTheme,
                        onToggleDarkTheme = { isDarkTheme = !isDarkTheme },
                        onLogout = { repository.logout() }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingIntents(intent)
    }

    private fun handleIncomingIntents(intent: Intent?) {
        if (intent == null) return

        // Check for Deep Links (e.g., https://aeonvaultfilemanager.vercel.app or aeonvault://)
        val data: Uri? = intent.data
        if (data != null) {
            val path = data.path
            Toast.makeText(this, "Opened via ÆonVault link: $data", Toast.LENGTH_SHORT).show()
        }

        // Check for Share Sheet intents (ACTION_SEND or ACTION_SEND_MULTIPLE)
        when (intent.action) {
            Intent.ACTION_SEND -> {
                val streamUri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                if (streamUri != null) {
                    processReceivedFile(streamUri)
                }
            }
            Intent.ACTION_SEND_MULTIPLE -> {
                val streamUris = intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
                streamUris?.forEach { uri ->
                    processReceivedFile(uri)
                }
            }
        }
    }

    private fun processReceivedFile(uri: Uri) {
        var fileName = "shared_file_${System.currentTimeMillis()}"
        var fileSize = 10_000_000L
        val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"

        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (nameIndex != -1) fileName = cursor.getString(nameIndex)
                if (sizeIndex != -1 && !cursor.isNull(sizeIndex)) fileSize = cursor.getLong(sizeIndex)
            }
        }
        repository.enqueueUpload(uri, fileName, mimeType, fileSize, null)
        Toast.makeText(this, "Added '$fileName' to ÆonVault upload queue", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun AeonVaultApp(
    repository: VaultRepository,
    isDarkTheme: Boolean,
    onToggleDarkTheme: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var currentDestination by remember { mutableStateOf(AppDestination.DASHBOARD) }

    // Modals
    var previewingFile by remember { mutableStateOf<VaultItem?>(null) }
    var sharingFile by remember { mutableStateOf<VaultItem?>(null) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 700.dp

        Scaffold(
            bottomBar = {
                if (!isWideScreen) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp,
                        modifier = Modifier.testTag("main_bottom_nav")
                    ) {
                        AppDestination.values().forEach { dest ->
                            NavigationBarItem(
                                selected = currentDestination == dest,
                                onClick = { currentDestination = dest },
                                icon = {
                                    Icon(
                                        imageVector = dest.icon,
                                        contentDescription = dest.label,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                label = { Text(dest.label, fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = NeonCyan,
                                    selectedTextColor = NeonCyan,
                                    indicatorColor = NeonCyan.copy(alpha = 0.2f)
                                ),
                                modifier = Modifier.testTag("nav_item_${dest.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isWideScreen) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        AppDestination.values().forEach { dest ->
                            NavigationRailItem(
                                selected = currentDestination == dest,
                                onClick = { currentDestination = dest },
                                icon = {
                                    Icon(
                                        imageVector = dest.icon,
                                        contentDescription = dest.label
                                    )
                                },
                                label = { Text(dest.label) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = NeonCyan,
                                    selectedTextColor = NeonCyan,
                                    indicatorColor = NeonCyan.copy(alpha = 0.2f)
                                )
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    when (currentDestination) {
                        AppDestination.DASHBOARD -> DashboardScreen(
                            repository = repository,
                            onNavigateToVault = { currentDestination = AppDestination.VAULT },
                            onNavigateToUpload = { currentDestination = AppDestination.UPLOAD },
                            onNavigateToAssistant = { currentDestination = AppDestination.ASSISTANT },
                            onFileClick = { previewingFile = it },
                            onShareFile = { sharingFile = it }
                        )
                        AppDestination.VAULT -> VaultScreen(
                            repository = repository,
                            onFileClick = { previewingFile = it },
                            onShareFile = { sharingFile = it }
                        )
                        AppDestination.UPLOAD -> UploadScreen(
                            repository = repository
                        )
                        AppDestination.ASSISTANT -> GeminiAssistantScreen(
                            repository = repository
                        )
                        AppDestination.SHARED -> SharedScreen(
                            repository = repository
                        )
                        AppDestination.ACTIVITY -> ActivityScreen(
                            repository = repository
                        )
                        AppDestination.SETTINGS -> SettingsScreen(
                            repository = repository,
                            isDarkTheme = isDarkTheme,
                            onToggleDarkTheme = onToggleDarkTheme,
                            onLogout = onLogout
                        )
                    }
                }
            }
        }
    }

    // File Preview Modal
    previewingFile?.let { item ->
        FilePreviewModal(
            item = item,
            onDismiss = { previewingFile = null },
            onShare = {
                sharingFile = item
                previewingFile = null
            },
            onDownload = {
                Toast.makeText(context, "Downloaded '${item.name}' to local device storage", Toast.LENGTH_SHORT).show()
                previewingFile = null
            }
        )
    }

    // Share Dialog
    sharingFile?.let { item ->
        ShareDialog(
            item = item,
            onDismiss = { sharingFile = null },
            onConfirmCreateShare = { accessType, isPass, pass, allowDl, expiry ->
                scope.launch {
                    repository.createShareLink(
                        item = item,
                        accessType = accessType,
                        isPasswordProtected = isPass,
                        password = pass,
                        allowDownload = allowDl,
                        expiresInHours = expiry
                    )
                }
            }
        )
    }
}
