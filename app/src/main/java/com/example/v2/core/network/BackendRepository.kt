package com.example.v2.core.network

import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

@Serializable
data class AuthResponse(val sessionId: String, val status: String, val config: BackendConfig? = null)

@Serializable
data class BackendConfig(
    val system_instruction: String,
    val tools: List<com.example.v2.core.tools.AssistantTool> = emptyList(),
    val ephemeral_token: String? = null
)

@Serializable
data class TokenResponse(val ephemeralToken: String, val expiresAt: Long)

@Serializable
data class HealthResponse(val status: String, val version: String)

class BackendRepository(private val baseUrl: String) {
    private val client = OkHttpClient()
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    suspend fun checkHealth(): Result<HealthResponse> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        return@withContext try {
            val request = Request.Builder()
                .url("$baseUrl/health")
                .get()
                .build()
            
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw Exception("Unexpected code $response")
                val body = response.body?.string() ?: throw Exception("Empty body")
                Result.success(json.decodeFromString<HealthResponse>(body))
            }
        } catch (e: Exception) {
            Log.e("BackendRepository", "Health check failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun authenticate(bossName: String): Result<AuthResponse> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        return@withContext try {
            val bodyMap = mapOf("boss_name" to bossName)
            val requestBody = json.encodeToString(bodyMap).toRequestBody("application/json".toMediaType())
            
            val request = Request.Builder()
                .url("$baseUrl/auth")
                .post(requestBody)
                .build()
            
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw Exception("Unexpected code $response")
                val body = response.body?.string() ?: throw Exception("Empty body")
                Result.success(json.decodeFromString<AuthResponse>(body))
            }
        } catch (e: Exception) {
            Log.e("BackendRepository", "Auth failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getLiveToken(sessionId: String): Result<TokenResponse> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        return@withContext try {
            val request = Request.Builder()
                .url("$baseUrl/live/token")
                .header("X-Session-Id", sessionId)
                .get()
                .build()
            
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw Exception("Unexpected code $response")
                val body = response.body?.string() ?: throw Exception("Empty body")
                Result.success(json.decodeFromString<TokenResponse>(body))
            }
        } catch (e: Exception) {
            Log.e("BackendRepository", "Token fetch failed: ${e.message}")
            Result.failure(e)
        }
    }
}
