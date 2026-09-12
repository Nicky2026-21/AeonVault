package com.example.network.gemini

import com.example.BuildConfig
import com.example.data.model.VaultItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val modelName = "gemini-3.5-flash"
    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models"

    val isApiKeyConfigured: Boolean
        get() {
            val key = getApiKey()
            return key.isNotBlank() && key != "MY_GEMINI_API_KEY"
        }

    private fun getApiKey(): String {
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
    }

    suspend fun generateContent(prompt: String, systemInstruction: String? = null): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is not configured. Please add your key in the AI Studio Secrets panel.")
            )
        }

        try {
            val url = "$baseUrl/$modelName:generateContent?key=$apiKey"

            val rootJson = JSONObject()
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()
            val textPart = JSONObject().put("text", prompt)
            partsArray.put(textPart)
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            rootJson.put("contents", contentsArray)

            if (!systemInstruction.isNullOrBlank()) {
                val sysObj = JSONObject()
                val sysParts = JSONArray().put(JSONObject().put("text", systemInstruction))
                sysObj.put("parts", sysParts)
                rootJson.put("systemInstruction", sysObj)
            }

            // Generation config with low temperature for precision
            val genConfig = JSONObject()
            genConfig.put("temperature", 0.4)
            genConfig.put("topP", 0.95)
            rootJson.put("generationConfig", genConfig)

            val requestBody = rootJson.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    val errorMsg = try {
                        val errObj = JSONObject(bodyStr).getJSONObject("error")
                        errObj.getString("message")
                    } catch (e: Exception) {
                        "HTTP ${response.code}: $bodyStr"
                    }
                    return@withContext Result.failure(Exception("Gemini API error: $errorMsg"))
                }

                val respJson = JSONObject(bodyStr)
                val candidates = respJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val text = parts.getJSONObject(0).optString("text")
                        return@withContext Result.success(text)
                    }
                }
                Result.failure(Exception("No content received from Gemini model"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun chatWithVaultAssistant(
        userMessage: String,
        vaultContext: String
    ): Result<String> {
        val systemPrompt = """
            You are the ÆonVault Quantum AI Assistant for "The Vault That Never Forgets".
            ÆonVault features a 1 Quettabyte (1 QB) logical storage quota with distributed infrastructure.
            You help users search files, summarize documents, categorize uploads, inspect storage distribution, and manage vault operations with a calm, high-tech, futuristic tone.
            Never hallucinate file deletion or claim that 1 QB is physically reserved on a single machine.
            Current user vault context:
            $vaultContext
        """.trimIndent()

        return generateContent(prompt = userMessage, systemInstruction = systemPrompt)
    }

    suspend fun analyzeDocumentOrImage(
        fileName: String,
        mimeType: String,
        sampleText: String?
    ): Result<Pair<String, List<String>>> {
        val prompt = """
            Analyze this uploaded file for ÆonVault storage index:
            Filename: $fileName
            MIME type: $mimeType
            Content snippet: ${sampleText ?: "Binary or media asset"}

            Provide a concise, 2-sentence futuristic summary, followed by a line starting with "TAGS:" containing 4 to 6 comma-separated tags suitable for indexing.
            Example:
            SUMMARY: Secure quantum ledger transaction records with SHA-256 verified signatures.
            TAGS: finance, ledger, quantum, security, report
        """.trimIndent()

        val result = generateContent(prompt)
        return result.map { text ->
            var summary = ""
            val tags = mutableListOf<String>()
            val lines = text.lines()
            for (line in lines) {
                when {
                    line.startsWith("SUMMARY:", ignoreCase = true) -> {
                        summary = line.substringAfter(":").trim()
                    }
                    line.startsWith("TAGS:", ignoreCase = true) -> {
                        val tagsStr = line.substringAfter(":").trim()
                        tags.addAll(tagsStr.split(",").map { it.trim() }.filter { it.isNotEmpty() })
                    }
                }
            }
            if (summary.isBlank()) {
                summary = text.trim()
            }
            Pair(summary, tags)
        }
    }

    suspend fun generateStorageInsights(
        usedBytes: Long,
        fileCount: Int,
        folderCount: Int,
        categoryCounts: Map<String, Int>
    ): Result<String> {
        val prompt = """
            Generate an intelligent ÆonVault storage health & optimization insight:
            - Logical Quota: 1 QB (Quettabyte)
            - Current Used: $usedBytes bytes
            - Total Files: $fileCount
            - Total Folders: $folderCount
            - Category breakdown: $categoryCounts

            Provide 3 brief, futuristic, actionable bullet points about storage optimization, backup durability, and organization.
        """.trimIndent()

        return generateContent(prompt)
    }
}
