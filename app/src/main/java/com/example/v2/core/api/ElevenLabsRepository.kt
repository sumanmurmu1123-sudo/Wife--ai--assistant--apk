package com.example.v2.core.api

import com.example.v2.core.AssistantConnectionState
import com.example.v2.core.StateManager
import com.example.v2.core.security.SecureStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class ElevenLabsRepository(
    private val secureStorage: SecureStorage,
    private val client: OkHttpClient = OkHttpClient()
) {
    suspend fun testConnection(): Result<AssistantConnectionState> = withContext(Dispatchers.IO) {
        val apiKey = secureStorage.getElevenLabsKey()
        if (apiKey.isNullOrBlank()) {
            updateState(AssistantConnectionState.NOT_CONFIGURED)
            return@withContext Result.failure(Exception("ElevenLabs API key not configured"))
        }

        updateState(AssistantConnectionState.CONNECTING)

        try {
            val request = Request.Builder()
                .url("https://api.elevenlabs.io/v1/voices")
                .addHeader("xi-api-key", apiKey)
                .get()
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful) {
                updateState(AssistantConnectionState.CONNECTED)
                Result.success(AssistantConnectionState.CONNECTED)
            } else {
                val connectionError = when (response.code) {
                    401 -> "Invalid ElevenLabs API key"
                    403 -> "Unauthorized: Quota exceeded or restricted"
                    429 -> "Rate limit exceeded"
                    else -> "ElevenLabs error: ${response.message}"
                }
                updateState(AssistantConnectionState.ERROR)
                Result.failure(Exception(connectionError))
            }
        } catch (e: Exception) {
            updateState(AssistantConnectionState.ERROR)
            Result.failure(Exception("Network error: ${e.localizedMessage}"))
        }
    }

    suspend fun generateTts(text: String): Result<ByteArray> = withContext(Dispatchers.IO) {
        val apiKey = secureStorage.getElevenLabsKey()
        val voiceId = secureStorage.getElevenLabsVoiceId()

        if (apiKey.isNullOrBlank()) {
            return@withContext Result.failure(Exception("ElevenLabs API key not configured"))
        }

        try {
            val json = JSONObject().apply {
                put("text", text)
                put("model_id", "eleven_multilingual_v2")
                put("voice_settings", JSONObject().apply {
                    put("stability", 0.5)
                    put("similarity_boost", 0.75)
                })
            }

            val request = Request.Builder()
                .url("https://api.elevenlabs.io/v1/text-to-speech/$voiceId?output_format=pcm_24000")
                .addHeader("xi-api-key", apiKey)
                .addHeader("Content-Type", "application/json")
                .post(json.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val bytes = response.body?.bytes()
                if (bytes != null) {
                    Result.success(bytes)
                } else {
                    Result.failure(Exception("Empty response body from ElevenLabs"))
                }
            } else {
                val errorMsg = when (response.code) {
                    401 -> "Invalid ElevenLabs API key"
                    429 -> "ElevenLabs Quota exceeded"
                    else -> "ElevenLabs synthesis failed: ${response.message}"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("TTS request failed: ${e.localizedMessage}"))
        }
    }

    private fun updateState(state: AssistantConnectionState) {
        StateManager.updateState { it.copy(elevenLabsState = state) }
    }
}
