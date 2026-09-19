package com.example.v2.core.api

import com.example.v2.core.AssistantConnectionState
import com.example.v2.core.StateManager
import com.example.v2.core.security.SecureStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class GeminiRepository(
    private val secureStorage: SecureStorage,
    private val client: OkHttpClient = OkHttpClient()
) {
    suspend fun testConnection(): Result<AssistantConnectionState> = withContext(Dispatchers.IO) {
        val apiKey = secureStorage.getApiKey()
        if (apiKey.isNullOrBlank()) {
            updateState(AssistantConnectionState.NOT_CONFIGURED)
            return@withContext Result.failure(Exception("API key not configured"))
        }

        updateState(AssistantConnectionState.CONNECTING)

        try {
            // We use a simple models list request to verify the key
            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models?key=$apiKey")
                .get()
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful) {
                updateState(AssistantConnectionState.CONNECTED)
                Result.success(AssistantConnectionState.CONNECTED)
            } else {
                val json = responseBody?.let { JSONObject(it) }
                val error = json?.optJSONObject("error")
                val message = error?.optString("message") ?: "Unknown error"
                val status = error?.optString("status") ?: "ERROR"
                
                val connectionError = when {
                    response.code == 401 || status == "UNAUTHENTICATED" -> "Invalid API key"
                    response.code == 403 || status == "PERMISSION_DENIED" -> "Forbidden / API key restricted"
                    response.code == 429 || status == "RESOURCE_EXHAUSTED" -> "Quota exceeded"
                    else -> "Server error: $message"
                }
                
                updateState(AssistantConnectionState.ERROR)
                Result.failure(Exception(connectionError))
            }
        } catch (e: java.io.IOException) {
            updateState(AssistantConnectionState.ERROR)
            Result.failure(Exception("Network error: ${e.localizedMessage}"))
        } catch (e: Exception) {
            updateState(AssistantConnectionState.ERROR)
            Result.failure(Exception("Unexpected error: ${e.localizedMessage}"))
        }
    }

    private fun updateState(state: AssistantConnectionState) {
        StateManager.updateState { it.copy(geminiState = state) }
    }
}
