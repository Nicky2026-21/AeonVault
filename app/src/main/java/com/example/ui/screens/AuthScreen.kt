package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.example.data.repository.VaultRepository
import com.example.ui.components.CyberBadge
import com.example.ui.components.GlassCard
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.NeonCyan
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    repository: VaultRepository,
    onAuthSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val allUsers by repository.allUsers.collectAsState(initial = emptyList())

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Login, 1: Register, 2: Recover
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var recoveryKeyInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            borderColor = NeonCyan.copy(alpha = 0.4f)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(NeonCyan.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(32.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "ÆonVault",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "“The Vault That Never Forgets”",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clip(RoundedCornerShape(10.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0; errorMessage = null },
                        text = { Text("Sign In", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1; errorMessage = null },
                        text = { Text("Register", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2; errorMessage = null },
                        text = { Text("Recover", fontSize = 12.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                if (selectedTab == 1) {
                    // Registration extra field
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Commander Username") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("auth_username_input")
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Vault Email") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("auth_email_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (selectedTab == 2) {
                    // Recovery key field
                    OutlinedTextField(
                        value = recoveryKeyInput,
                        onValueChange = { recoveryKeyInput = it },
                        label = { Text("Security Recovery Key") },
                        leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("auth_recovery_key_input")
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(if (selectedTab == 2) "New Password" else "Master Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("auth_password_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        isLoading = true
                        errorMessage = null
                        scope.launch {
                            when (selectedTab) {
                                0 -> {
                                    val res = repository.login(email.trim(), password)
                                    res.onSuccess {
                                        isLoading = false
                                        onAuthSuccess()
                                    }.onFailure {
                                        isLoading = false
                                        errorMessage = it.message ?: "Authentication failed"
                                    }
                                }
                                1 -> {
                                    val uName = if (username.isNotBlank()) username.trim() else "VaultUser"
                                    val res = repository.register(uName, email.trim(), password)
                                    res.onSuccess {
                                        isLoading = false
                                        Toast.makeText(context, "Account created with 1 QB logical quota!", Toast.LENGTH_LONG).show()
                                        onAuthSuccess()
                                    }.onFailure {
                                        isLoading = false
                                        errorMessage = it.message ?: "Registration failed"
                                    }
                                }
                                2 -> {
                                    val res = repository.recoverAccount(email.trim(), recoveryKeyInput.trim(), password)
                                    res.onSuccess {
                                        isLoading = false
                                        Toast.makeText(context, "Password reset successfully!", Toast.LENGTH_SHORT).show()
                                        onAuthSuccess()
                                    }.onFailure {
                                        isLoading = false
                                        errorMessage = it.message ?: "Recovery failed"
                                    }
                                }
                            }
                        }
                    },
                    enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth().testTag("auth_submit_button")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text(
                            text = when (selectedTab) {
                                0 -> "Sign In to Vault"
                                1 -> "Create Permanent Account (1 QB)"
                                else -> "Reset Password & Enter"
                            },
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CyberBadge("Protected: No Inactivity Deletion", color = EmeraldGlow)
                }

                if (allUsers.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "OR RESUME SAVED VAULT ACCOUNT",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        allUsers.forEach { user ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .border(1.dp, NeonCyan.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                    .clickable {
                                        repository.switchAccount(user)
                                        Toast.makeText(context, "Resumed session as ${user.username}", Toast.LENGTH_SHORT).show()
                                        onAuthSuccess()
                                    }
                                    .padding(10.dp)
                                    .testTag("auth_saved_account_${user.id}"),
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
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(user.username, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        Text(user.email, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Icon(Icons.Default.SwapHoriz, contentDescription = "Switch to this account", tint = NeonCyan, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
