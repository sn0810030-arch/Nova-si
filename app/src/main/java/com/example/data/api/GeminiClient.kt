package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"

    val DEFAULT_MODEL = "gemini-3.5-flash"
    val PRO_MODEL = "gemini-3.1-pro-preview"

    val AVAILABLE_MODELS = listOf(
        DEFAULT_MODEL to "Gemini 3.5 Flash (Ultra-Fast & Smart)",
        PRO_MODEL to "Gemini 3.1 Pro (Deep Complex Reasoning)"
    )

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val requestAdapter = moshi.adapter(GeminiGenerateContentRequest::class.java)

    fun getEffectiveApiKey(customApiKey: String?): String? {
        val userKey = customApiKey?.trim()
        if (!userKey.isNullOrEmpty() && isValidKey(userKey)) {
            return userKey
        }
        val buildKey = runCatching { BuildConfig.GEMINI_API_KEY }.getOrNull()?.trim()
        if (!buildKey.isNullOrEmpty() && isValidKey(buildKey)) {
            return buildKey
        }
        return null
    }

    fun isConfigured(customApiKey: String?): Boolean {
        return getEffectiveApiKey(customApiKey) != null
    }

    private fun isValidKey(key: String): Boolean {
        return key.length > 10 && !key.equals("MY_GEMINI_API_KEY", ignoreCase = true)
    }

    suspend fun testConnection(apiKey: String, model: String = DEFAULT_MODEL): Result<String> = withContext(Dispatchers.IO) {
        if (!isValidKey(apiKey)) {
            return@withContext Result.failure(IllegalArgumentException("Invalid API key format. Please enter a valid Gemini API key."))
        }
        val url = "$BASE_URL$model:generateContent?key=$apiKey"
        val requestJson = JSONObject().apply {
            put("contents", org.json.JSONArray().put(
                JSONObject().put("parts", org.json.JSONArray().put(
                    JSONObject().put("text", "Ping. Reply with: NOVA_ONLINE")
                ))
            ))
        }

        val body = requestJson.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder().url(url).post(body).build()

        try {
            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                val errorMsg = try {
                    JSONObject(responseBody).optJSONObject("error")?.optString("message") ?: "HTTP ${response.code}"
                } catch (e: Exception) {
                    "HTTP ${response.code}: $responseBody"
                }
                return@withContext Result.failure(Exception("API Error: $errorMsg"))
            }

            val text = parseGeminiResponseText(responseBody)
            Result.success(text.ifBlank { "Connected successfully to $model!" })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun streamGenerateContent(
        apiKey: String,
        model: String,
        messages: List<Pair<String, Boolean>>, // text to isFromUser
        systemPrompt: String?,
        temperature: Float = 0.7f
    ): Flow<String> = flow {
        val url = "$BASE_URL$model:streamGenerateContent?alt=sse&key=$apiKey"

        val contentsArray = org.json.JSONArray()
        for ((text, isUser) in messages) {
            val role = if (isUser) "user" else "model"
            contentsArray.put(
                JSONObject()
                    .put("role", role)
                    .put("parts", org.json.JSONArray().put(JSONObject().put("text", text)))
            )
        }

        val rootJson = JSONObject().apply {
            put("contents", contentsArray)
            if (!systemPrompt.isNullOrBlank()) {
                put("systemInstruction", JSONObject().put("parts", org.json.JSONArray().put(
                    JSONObject().put("text", systemPrompt)
                )))
            }
            put("generationConfig", JSONObject().apply {
                put("temperature", temperature.toDouble())
            })
        }

        val body = rootJson.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder().url(url).post(body).build()

        val response = okHttpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            val errorBody = response.body?.string() ?: "Unknown error"
            val errorMsg = try {
                JSONObject(errorBody).optJSONObject("error")?.optString("message") ?: "HTTP ${response.code}"
            } catch (e: Exception) {
                "HTTP ${response.code}: $errorBody"
            }
            throw Exception("Gemini Error: $errorMsg")
        }

        val reader = BufferedReader(InputStreamReader(response.body?.byteStream() ?: throw Exception("Empty stream")))
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            val current = line?.trim() ?: continue
            if (!current.startsWith("data:")) continue
            val jsonPayload = current.removePrefix("data:").trim()
            if (jsonPayload.isEmpty() || jsonPayload == "[DONE]") continue

            try {
                val jsonObj = JSONObject(jsonPayload)
                val candidates = jsonObj.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val partText = parts.getJSONObject(0).optString("text")
                        if (partText.isNotEmpty()) {
                            emit(partText)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error parsing SSE chunk: ${e.message}")
            }
        }
    }.flowOn(Dispatchers.IO)

    suspend fun generateContentSingle(
        apiKey: String,
        model: String,
        messages: List<Pair<String, Boolean>>,
        systemPrompt: String?,
        temperature: Float = 0.7f
    ): Result<String> = withContext(Dispatchers.IO) {
        val url = "$BASE_URL$model:generateContent?key=$apiKey"

        val contentsArray = org.json.JSONArray()
        for ((text, isUser) in messages) {
            val role = if (isUser) "user" else "model"
            contentsArray.put(
                JSONObject()
                    .put("role", role)
                    .put("parts", org.json.JSONArray().put(JSONObject().put("text", text)))
            )
        }

        val rootJson = JSONObject().apply {
            put("contents", contentsArray)
            if (!systemPrompt.isNullOrBlank()) {
                put("systemInstruction", JSONObject().put("parts", org.json.JSONArray().put(
                    JSONObject().put("text", systemPrompt)
                )))
            }
            put("generationConfig", JSONObject().apply {
                put("temperature", temperature.toDouble())
            })
        }

        val body = rootJson.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder().url(url).post(body).build()

        try {
            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                val errorMsg = try {
                    JSONObject(responseBody).optJSONObject("error")?.optString("message") ?: "HTTP ${response.code}"
                } catch (e: Exception) {
                    "HTTP ${response.code}: $responseBody"
                }
                return@withContext Result.failure(Exception("Gemini API Error: $errorMsg"))
            }

            val text = parseGeminiResponseText(responseBody)
            Result.success(text)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseGeminiResponseText(jsonString: String): String {
        return try {
            val obj = JSONObject(jsonString)
            val candidates = obj.optJSONArray("candidates") ?: return ""
            if (candidates.length() == 0) return ""
            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content") ?: return ""
            val parts = content.optJSONArray("parts") ?: return ""
            val sb = StringBuilder()
            for (i in 0 until parts.length()) {
                sb.append(parts.getJSONObject(i).optString("text"))
            }
            sb.toString()
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Intelligent local fallback engine if API key is not configured or in sandbox mode.
     */
    fun generateLocalFallbackResponse(userPrompt: String, persona: String): String {
        val lower = userPrompt.lowercase().trim()
        return when {
            lower.contains("hello") || lower.contains("hi") || lower.contains("hey") ->
                "Hello! I am **Nova AI**, your conversational assistant powered by Google Gemini architecture.\n\nI can help you with:\n- 💻 **Code & Architecture** (Kotlin, Compose, Python, APIs)\n- ✍️ **Writing & Editing** (Emails, documentation, creative stories)\n- 🧠 **Concept Explanations** & Brainstorming\n- 🔍 **Problem Solving**\n\n*Tip: Connect your Google Gemini API key in **Settings** to unlock live real-time cloud responses!*"

            lower.contains("code") || lower.contains("kotlin") || lower.contains("compose") ->
                "Here is an example of an idiomatic **Jetpack Compose** stateful counter with animations:\n\n```kotlin\n@Composable\nfun NovaCounter(modifier: Modifier = Modifier) {\n    var count by remember { mutableIntStateOf(0) }\n    \n    Button(\n        onClick = { count++ },\n        shape = RoundedCornerShape(16.dp),\n        colors = ButtonDefaults.buttonColors(\n            containerColor = Color(0xFF00E5FF)\n        )\n    ) {\n        Text(\"Nova Clicks: \$count\", color = Color(0xFF0B0F19))\n    }\n}\n```\n\nKey takeaways:\n- Uses `remember` and `mutableIntStateOf` to prevent autoboxing.\n- Standard M3 styling with rounded corners.\n- Fully responsive and accessible."

            lower.contains("who are you") || lower.contains("what is nova") ->
                "I am **Nova AI** — an advanced intelligent Android assistant engineered for deep reasoning, creative writing, and high-speed software development.\n\nPowered by Google Gemini 3.5 Flash and Gemini 3.1 Pro models, I support contextual conversation history, custom personas, and code syntax highlighting."

            lower.contains("explain") || lower.contains("how does") ->
                "Here is a structured breakdown of that concept:\n\n1. **Core Mechanism**: Operates via iterative token prediction and deep transformer neural representations.\n2. **Practical Application**: Speeds up development, automates repetitive reasoning, and synthesizes massive information.\n3. **Best Practices**: Keep prompts specific, specify output formats, and provide examples when asking for complex code or structured data."

            else ->
                "Thank you for your prompt! Here is Nova's analysis:\n\nRegarding **\"$userPrompt\"**:\n\n- **Analysis**: This is a great topic with several multifaceted approaches.\n- **Recommendation**: Consider breaking down the problem into atomic modular components.\n- **Next Steps**: Let me know if you would like me to generate code, draft an outline, or write a detailed explanation.\n\n*(To activate live unlimited Gemini AI responses, enter your free API key in the top-right Settings menu!)*"
        }
    }
}
