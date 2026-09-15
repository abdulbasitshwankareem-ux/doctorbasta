package com.example.data.remote

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.data.local.ApiKeyDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"
    private const val MODEL_TEXT = "gemini-3.5-flash"
    private const val MODEL_IMAGE = "gemini-2.5-flash-image"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private const val SYSTEM_PROMPT = """
تۆ "Basit AI"یت، زیرەکترین و خێراترین یاریدەدەری کوردی.
یاسا سەرەکییەکان:
1. هەمیشە وەڵامەکانت بە زمانی کوردیی سۆرانیی پوخت، پاراو، زۆر ڕوون و ئاسان بنووسە.
2. بە هیچ شێوەیەک بە زمانی عەرەبی یان ئینگلیزی وەڵام مەدەرەوە، مەگەر بەکارهێنەر بە ڕوونی داوای وەرگێڕان بۆ زمانێکی تر بکات یان کۆدی پرۆگرامسازی بێت.
3. بۆ وێنە، فایل، بەڵگەنامە و ڤیدیۆ: بە وردی سەیری ناوەڕۆک و نووسین و داتاکانی بکە و بە کوردی شیکاری بکە.
4. بۆ دروستکردنی فایل: ئەگەر داوای دروستکردنی PDF، Excel، CSV یان تێکست کرا، داتاکە بە ڕوونی و بە خشتە دابنێ بۆ دروستکردن.
5. هەمیشە بەڕێز، دۆستانە و خێرا وەڵام بدەرەوە.
"""

    private const val PREFS_NAME = "basit_ai_prefs"
    private const val PREF_KEY_GEMINI = "custom_gemini_api_key"

    fun getUserCustomKey(context: android.content.Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
        return prefs.getString(PREF_KEY_GEMINI, "") ?: ""
    }

    fun saveUserCustomKey(context: android.content.Context, key: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
        prefs.edit().putString(PREF_KEY_GEMINI, key.trim()).apply()
    }

    fun getApiKey(context: android.content.Context? = null): String {
        if (context != null) {
            val custom = getUserCustomKey(context)
            if (custom.isNotBlank()) return custom
        }
        val buildKey = BuildConfig.GEMINI_API_KEY
        if (buildKey.isNotBlank() && buildKey != "MY_GEMINI_API_KEY") {
            return buildKey
        }
        return ""
    }

    fun isApiKeyConfigured(context: android.content.Context? = null): Boolean {
        val key = getApiKey(context)
        return key.isNotBlank() && key != "MY_GEMINI_API_KEY"
    }

    /**
     * Converts a Bitmap to Base64 JPEG string
     */
    fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    /**
     * Generates content and emits smooth progressive streaming chunks
     */
    fun streamChatResponse(
        prompt: String,
        history: List<Pair<String, String>> = emptyList(), // role ("user"/"model") to content
        attachmentBase64: String? = null,
        attachmentMimeType: String? = null,
        context: android.content.Context? = null
    ): Flow<String> = flow {
        var apiKey = getApiKey(context)
        if (apiKey.isBlank() && context != null) {
            apiKey = ApiKeyDataStore.getApiKey(context)
        }
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            emit("تکایە کلیلی تایبەتی Gemini API لە دوگمەی کلیلی 🔑 سەرەوە لە ئەپەکە دابنێ تاوەکو Basit AI ڕاستەوخۆ وەڵام بداتەوە.")
            return@flow
        }

        try {
            val requestJson = JSONObject()

            // System instruction
            val systemInstruction = JSONObject()
            val systemParts = JSONArray()
            systemParts.put(JSONObject().put("text", SYSTEM_PROMPT))
            systemInstruction.put("parts", systemParts)
            requestJson.put("systemInstruction", systemInstruction)

            // Contents (History + Current Prompt)
            val contentsArray = JSONArray()

            // Add previous recent history turns
            val recentHistory = history.takeLast(8)
            for (turn in recentHistory) {
                val turnObj = JSONObject()
                turnObj.put("role", if (turn.first == "model") "model" else "user")
                val parts = JSONArray()
                parts.put(JSONObject().put("text", turn.second))
                turnObj.put("parts", parts)
                contentsArray.put(turnObj)
            }

            // Current turn
            val currentTurn = JSONObject()
            currentTurn.put("role", "user")
            val currentParts = JSONArray()

            if (!attachmentBase64.isNullOrBlank() && !attachmentMimeType.isNullOrBlank()) {
                val inlineData = JSONObject()
                inlineData.put("mimeType", attachmentMimeType)
                inlineData.put("data", attachmentBase64)
                currentParts.put(JSONObject().put("inlineData", inlineData))
            }

            currentParts.put(JSONObject().put("text", prompt))
            currentTurn.put("parts", currentParts)
            contentsArray.put(currentTurn)

            requestJson.put("contents", contentsArray)

            // Generation config
            val genConfig = JSONObject()
            genConfig.put("temperature", 0.7)
            genConfig.put("topP", 0.95)
            requestJson.put("generationConfig", genConfig)

            val url = "$BASE_URL/$MODEL_TEXT:generateContent?key=$apiKey"
            val body = requestJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            val response = withContext(Dispatchers.IO) {
                client.newCall(request).execute()
            }

            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: "Unknown error"
                Log.e(TAG, "API Error: ${response.code} -> $errorBody")
                emit("ببورە، هەڵەیەک ڕوویدا لە کاتی پەیوەندیکردن بە سێرڤەرەوە (${response.code}). تکایە دووبارە هەوڵ بدەرەوە.")
                return@flow
            }

            val responseStr = response.body?.string() ?: ""
            val jsonResponse = JSONObject(responseStr)
            val candidates = jsonResponse.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val fullText = parts?.optJSONObject(0)?.optString("text", "") ?: ""

            if (fullText.isBlank()) {
                emit("وەڵامێک نەدۆزرایەوە، تکایە پرسیارەکەت دووبارە بنووسەرەوە.")
                return@flow
            }

            // Provide a fast, natural word-by-word streaming effect for smooth Kurdish reading
            val words = fullText.split(" ")
            val chunkBuffer = StringBuilder()
            for (i in words.indices) {
                chunkBuffer.append(words[i]).append(" ")
                if (i % 3 == 0 || i == words.lastIndex) {
                    emit(chunkBuffer.toString())
                    delay(25) // Smooth natural typing pace
                }
            }
            emit(fullText) // Ensure final exact content
        } catch (e: Exception) {
            Log.e(TAG, "Exception during chat: ${e.message}", e)
            emit("کێشەیەک لە هێڵی ئینتەرنێت هەیە: ${e.localizedMessage ?: "هەڵەی نەزانراو"}")
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Generates an image using Gemini's image generation model or returns status
     */
    suspend fun generateImage(prompt: String): String? = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (!isApiKeyConfigured()) return@withContext null

        try {
            val requestJson = JSONObject()
            val contents = JSONArray()
            val contentObj = JSONObject()
            val parts = JSONArray()
            parts.put(JSONObject().put("text", prompt))
            contentObj.put("parts", parts)
            contents.put(contentObj)
            requestJson.put("contents", contents)

            val genConfig = JSONObject()
            genConfig.put("responseModalities", JSONArray().put("IMAGE").put("TEXT"))
            val imageConfig = JSONObject()
            imageConfig.put("aspectRatio", "1:1")
            imageConfig.put("imageSize", "1K")
            genConfig.put("imageConfig", imageConfig)
            requestJson.put("generationConfig", genConfig)

            val url = "$BASE_URL/$MODEL_IMAGE:generateContent?key=$apiKey"
            val body = requestJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val resBody = response.body?.string() ?: ""
                val json = JSONObject(resBody)
                val candidates = json.optJSONArray("candidates")
                val partsArr = candidates?.optJSONObject(0)?.optJSONObject("content")?.optJSONArray("parts")
                for (i in 0 until (partsArr?.length() ?: 0)) {
                    val part = partsArr?.optJSONObject(i)
                    val inlineData = part?.optJSONObject("inlineData")
                    val data = inlineData?.optString("data")
                    if (!data.isNullOrBlank()) {
                        return@withContext data
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Image generation error: ${e.message}", e)
        }
        return@withContext null
    }
}
