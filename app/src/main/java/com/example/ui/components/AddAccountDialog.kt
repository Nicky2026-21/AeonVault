package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.User
import com.example.data.repository.VaultRepository
import com.example.ui.theme.CoralNeon
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.NeonCyan
import kotlinx.coroutines.launch

@Composable
fun AddAccountDialog(
    repository: VaultRepository,
    onDismiss: () -> Unit,
    onAccountAdded: (User) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Create New Account, 1: Link Existing

    // Create New Form State
    var newUsername by remember { mutableStateOf("") }
    var newEmail by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var selectedPlanTier by remember { mutableStateOf("Æon Prime") }

    // Link Existing Form State
    var linkEmail by remember { mutableStateOf("") }
    var linkPassword by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.5.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .testTag("add_account_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(NeonCyan.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Add Vault Account",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                            Text(
                                text = "Add or switch multi-partition vault profiles",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tabs: Create New vs Link Existing
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    contentColor = NeonCyan,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = NeonCyan
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = {
                            selectedTab = 0
                            errorMessage = null
                        },
                        text = {
                            Text(
                                "Create New Vault",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = {
                            selectedTab = 1
                            errorMessage = null
                        },
                        text = {
                            Text(
                                "Link Existing",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (errorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(CoralNeon.copy(alpha = 0.15f))
                            .border(1.dp, CoralNeon.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = CoralNeon,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (selectedTab == 0) {
                    // TAB 0: CREATE NEW ACCOUNT
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = newUsername,
                            onValueChange = { newUsername = it },
                            label = { Text("Account / Commander Name") },
                            placeholder = { Text("e.g., DeepSpaceResearcher") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = NeonCyan) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("add_account_username_input")
                        )

                        OutlinedTextField(
                            value = newEmail,
                            onValueChange = { newEmail = it },
                            label = { Text("Email Address") },
                            placeholder = { Text("user@vault.space") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = NeonCyan) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("add_account_email_input")
                        )

                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text("Master Passphrase") },
                            placeholder = { Text("Minimum 6 characters") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = NeonCyan) },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("add_account_password_input")
                        )

                        Text(
                            text = "Partition Plan Tier",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val tiers = listOf("Æon Prime", "Sovereign Enterprise")
                            tiers.forEach { tier ->
                                val isSelected = selectedPlanTier == tier
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (isSelected) NeonCyan.copy(alpha = 0.15f)
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                        )
                                        .border(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) NeonCyan else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable { selectedPlanTier = tier }
                                        .padding(vertical = 10.dp, horizontal = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = tier,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) NeonCyan else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "1 QB Partition",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        // Feature notes
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(EmeraldGlow.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Icon(
                                Icons.Default.Security,
                                contentDescription = null,
                                tint = EmeraldGlow,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Every added account receives an isolated 1 QB logical addressing allocation, independent recovery key, and permanent zero-inactivity deletion safeguard.",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = {
                                if (newUsername.isBlank() || newEmail.isBlank() || newPassword.isBlank()) {
                                    errorMessage = "Please complete all fields"
                                    return@Button
                                }
                                if (!newEmail.contains("@") || !newEmail.contains(".")) {
                                    errorMessage = "Please enter a valid email address"
                                    return@Button
                                }
                                if (newPassword.length < 6) {
                                    errorMessage = "Password must be at least 6 characters"
                                    return@Button
                                }

                                isLoading = true
                                errorMessage = null
                                scope.launch {
                                    val result = repository.addAccount(
                                        username = newUsername.trim(),
                                        email = newEmail.trim().lowercase(),
                                        password = newPassword,
                                        planTier = selectedPlanTier
                                    )
                                    isLoading = false
                                    result.onSuccess { createdUser ->
                                        Toast.makeText(context, "Account '${createdUser.username}' created and activated!", Toast.LENGTH_LONG).show()
                                        onAccountAdded(createdUser)
                                        onDismiss()
                                    }.onFailure { err ->
                                        errorMessage = err.message ?: "Failed to add account"
                                    }
                                }
                            },
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("confirm_create_account_button")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Create & Switch to Account",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else {
                    // TAB 1: LINK EXISTING ACCOUNT
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Link an existing vault account to this device to switch between accounts with one tap.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = linkEmail,
                            onValueChange = { linkEmail = it },
                            label = { Text("Account Email") },
                            placeholder = { Text("existing@vault.space") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = ElectricViolet) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("link_account_email_input")
                        )

                        OutlinedTextField(
                            value = linkPassword,
                            onValueChange = { linkPassword = it },
                            label = { Text("Account Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = ElectricViolet) },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("link_account_password_input")
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = {
                                if (linkEmail.isBlank() || linkPassword.isBlank()) {
                                    errorMessage = "Please enter both email and password"
                                    return@Button
                                }

                                isLoading = true
                                errorMessage = null
                                scope.launch {
                                    val result = repository.login(linkEmail.trim().lowercase(), linkPassword)
                                    isLoading = false
                                    result.onSuccess { loggedInUser ->
                                        Toast.makeText(context, "Switched to account '${loggedInUser.username}'", Toast.LENGTH_SHORT).show()
                                        onAccountAdded(loggedInUser)
                                        onDismiss()
                                    }.onFailure { err ->
                                        errorMessage = err.message ?: "Authentication failed"
                                    }
                                }
                            },
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("confirm_link_account_button")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.VpnKey, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Authenticate & Switch Account",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
