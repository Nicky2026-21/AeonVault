package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.data.repository.VaultRepository
import com.example.ui.components.CyberBadge
import com.example.ui.components.GlassCard
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.NeonCyan
import kotlinx.coroutines.launch

data class ChatMessage(
    val id: String,
    val sender: String, // USER or ASSISTANT
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Composable
fun GeminiAssistantScreen(
    repository: VaultRepository,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val allActiveItems by repository.getAllActiveItems().collectAsState(initial = emptyList())
    val currentUser by repository.currentUser.collectAsState()

    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                id = "1",
                sender = "ASSISTANT",
                text = "Welcome to ÆonVault Quantum AI Assistant powered by Gemini 3.5 Flash.\n\nI can analyze your 1 QB logical storage, search files by natural language, summarize documents, explain distributed storage safeguards, or suggest optimization strategies. How may I assist your vault today?"
            )
        )
    }

    var inputText by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    fun sendMessage(query: String) {
        if (query.isBlank() || isGenerating) return
        val userMsg = ChatMessage(id = System.currentTimeMillis().toString(), sender = "USER", text = query.trim())
        messages.add(userMsg)
        inputText = ""
        isGenerating = true

        scope.launch {
            // Build vault context for Gemini
            val filesSummary = allActiveItems.take(20).joinToString("\n") {
                "- ${it.name} (${it.formattedSize}, Type: ${it.aiCategory}, Tags: [${it.aiTags}], Summary: ${it.aiSummary ?: "N/A"})"
            }
            val vaultContext = """
                User: ${currentUser?.username ?: "Commander"}
                Quota Used: ${User.formatStorageSize(currentUser?.quotaUsedBytes ?: 0L)} / 1 QB Logical
                Total Files: ${allActiveItems.size}
                Permanent Protection: Active (No auto-deletion for inactivity)
                Distributed Infrastructure: 12 mesh nodes
                Sample Files in Vault:
                $filesSummary
            """.trimIndent()

            val result = repository.geminiService.chatWithVaultAssistant(query, vaultContext)
            result.onSuccess { reply ->
                messages.add(ChatMessage(id = (System.currentTimeMillis() + 1).toString(), sender = "ASSISTANT", text = reply))
                isGenerating = false
                listState.animateScrollToItem(messages.size - 1)
            }.onFailure { err ->
                val fallbackReply = when {
                    query.contains("1 QB", ignoreCase = true) || query.contains("quota", ignoreCase = true) ->
                        "Notice: ÆonVault provides a 1 QB (Quettabyte) logical storage quota. This represents maximum assigned logical allocation and does not mean 1 QB is individually reserved on a single server. Capacity depends on distributed infrastructure, with zero inactivity deletion."
                    query.contains("summarize", ignoreCase = true) ->
                        "Your vault currently holds ${allActiveItems.size} files across Quantum Documents, Media, and Neural Code modules, securely stored with AES-256-GCM quantum-resistant encryption."
                    else ->
                        "Your query has been indexed against the distributed ledger. If Gemini API key is missing, add GEMINI_API_KEY in Secrets panel to unlock full LLM generation. Query: $query"
                }
                messages.add(ChatMessage(id = (System.currentTimeMillis() + 1).toString(), sender = "ASSISTANT", text = fallbackReply))
                isGenerating = false
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(ElectricViolet.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ElectricViolet, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Quantum Assistant", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Gemini 3.5 Flash · Natural Language Engine", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            CyberBadge(if (repository.geminiService.isApiKeyConfigured) "Gemini Connected" else "Default Rules", color = if (repository.geminiService.isApiKeyConfigured) EmeraldGlow else ElectricViolet)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Suggested Queries
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val suggestions = listOf(
                "How does the 1 QB storage quota work?",
                "Summarize all PDF documents in my vault",
                "Which files take up the most space?",
                "Are my files protected against inactivity wipe?",
                "Find quantum research blueprints"
            )
            suggestions.forEach { suggestion ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
                        .border(1.dp, ElectricViolet.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                        .clickable { sendMessage(suggestion) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(suggestion, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                val isUser = msg.sender == "USER"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isUser) NeonCyan.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(
                                1.dp,
                                if (isUser) NeonCyan.copy(alpha = 0.4f) else ElectricViolet.copy(alpha = 0.3f),
                                RoundedCornerShape(16.dp)
                            )
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (isUser) "You" else "ÆonVault Assistant (Gemini)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isUser) NeonCyan else ElectricViolet
                                )
                                Text(
                                    text = if (isUser) "Client" else "AI Mesh",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = msg.text,
                                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            if (isGenerating) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = ElectricViolet)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Synthesizing response...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Input Field
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Ask Gemini about your vault, files, quota...") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("assistant_chat_input"),
                shape = RoundedCornerShape(16.dp),
                maxLines = 3
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = { sendMessage(inputText) },
                enabled = inputText.isNotBlank() && !isGenerating,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(if (inputText.isNotBlank()) NeonCyan else MaterialTheme.colorScheme.surfaceVariant)
                    .testTag("assistant_send_button")
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "Send",
                    tint = if (inputText.isNotBlank()) Color(0xFF04101A) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
