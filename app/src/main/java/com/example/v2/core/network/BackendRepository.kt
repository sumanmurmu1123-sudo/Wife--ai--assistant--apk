package com.example.v2.core.network

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class AuthResponse(val sessionId: String, val status: String)

@Serializable
data class TokenResponse(val ephemeralToken: String, val expiresAt: Long)

@Serializable
data class HealthResponse(val status: String, val version: String)

class BackendRepository(private val baseUrl: String) {
    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            })
        }
    }

    suspend fun checkHealth(): Result<HealthResponse> {
        return try {
            val response = client.get("$baseUrl/health")
            Result.success(response.body())
        } catch (e: Exception) {
            Log.e("BackendRepository", "Health check failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun authenticate(bossName: String): Result<AuthResponse> {
        return try {
            val response = client.post("$baseUrl/auth") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("boss_name" to bossName))
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Log.e("BackendRepository", "Auth failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getLiveToken(sessionId: String): Result<TokenResponse> {
        return try {
            val response = client.get("$baseUrl/live/token") {
                header("X-Session-Id", sessionId)
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Log.e("BackendRepository", "Token fetch failed: ${e.message}")
            Result.failure(e)
        }
    }
}
