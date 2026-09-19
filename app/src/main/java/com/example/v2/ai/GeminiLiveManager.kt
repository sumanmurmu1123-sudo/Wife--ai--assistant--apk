package com.example.v2.ai

import android.util.Base64
import com.example.v2.core.security.SecureStorage
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.http.HttpMethod
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.json.JSONArray
import org.json.JSONObject

class GeminiLiveManager {
    private val client = HttpClient(OkHttp) {
        install(WebSockets) {
        }
    }
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private var webSocketSession: WebSocketSession? = null
    private var secureStorage: SecureStorage? = null

    fun init(context: android.content.Context) {
        secureStorage = SecureStorage(context)
    }
    
    private val _audioFlow = MutableSharedFlow<ByteArray>(replay = 0, extraBufferCapacity = 50)
    val audioFlow: SharedFlow<ByteArray> = _audioFlow
    
    private val _turnCompleteFlow = MutableSharedFlow<Unit>()
    val turnCompleteFlow: SharedFlow<Unit> = _turnCompleteFlow

    private val _textFlow = MutableSharedFlow<String>()
    val textFlow: SharedFlow<String> = _textFlow

    private val _functionCallFlow = MutableSharedFlow<JSONObject>()
    val functionCallFlow: SharedFlow<JSONObject> = _functionCallFlow

    private val _errorFlow = MutableSharedFlow<String>()
    val errorFlow: SharedFlow<String> = _errorFlow

    private val _disconnectedFlow = MutableSharedFlow<Unit>()
    val disconnectedFlow: SharedFlow<Unit> = _disconnectedFlow

    private val _setupCompleteFlow = MutableSharedFlow<Unit>()
    val setupCompleteFlow: SharedFlow<Unit> = _setupCompleteFlow

    private fun mapToJsonObject(map: Map<String, Any?>): JSONObject {
        val json = JSONObject()
        map.forEach { (key, value) ->
            json.put(key, when (value) {
                is Map<*, *> -> mapToJsonObject(value as Map<String, Any?>)
                is List<*> -> listToJsonArray(value)
                else -> value
            })
        }
        return json
    }

    private fun listToJsonArray(list: List<*>): JSONArray {
        val array = JSONArray()
        list.forEach { value ->
            array.put(when (value) {
                is Map<*, *> -> mapToJsonObject(value as Map<String, Any?>)
                is List<*> -> listToJsonArray(value)
                else -> value
            })
        }
        return array
    }

    suspend fun connect(systemInstruction: String = "", apiKeyOverride: String? = null, dynamicTools: List<com.example.v2.core.tools.AssistantTool> = emptyList()) {
        disconnect()
        try {
            var apiKey = apiKeyOverride ?: secureStorage?.getApiKey() ?: ""
            
            // Fallback to BuildConfig if provided at build time and not configured in app
            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                try {
                    val buildConfigClass = Class.forName("com.example.BuildConfig")
                    val field = buildConfigClass.getField("GEMINI_API_KEY")
                    val buildConfigKey = field.get(null) as? String
                    if (!buildConfigKey.isNullOrBlank() && buildConfigKey != "MY_GEMINI_API_KEY") {
                        apiKey = buildConfigKey
                    }
                } catch (e: Exception) {
                    // BuildConfig key not found or accessible
                }
            }

            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                android.util.Log.e("VoiceDiag", "GEMINI_CONNECT: API configuration missing")
                scope.launch { _errorFlow.emit("API configuration required") }
                return
            }
            android.util.Log.d("VoiceDiag", "GEMINI_CONNECT: Starting connection...")
            val host = "generativelanguage.googleapis.com"
            val path = "/ws/google.ai.generativelanguage.v1alpha.GenerativeService.BidiGenerateContent"
            
            webSocketSession = client.webSocketSession(
                method = HttpMethod.Get,
                host = host,
                path = "$path?key=$apiKey",
                port = 443
            ) {
                url.protocol = io.ktor.http.URLProtocol.WSS
            }
            
            // Send setup
            val setupMessage = JSONObject().apply {
                put("setup", JSONObject().apply {
                    put("model", "models/gemini-2.0-flash-exp")
                    
                    put("tools", JSONArray().apply {
                        put(JSONObject().apply {
                            put("functionDeclarations", JSONArray().apply {
                                put(JSONObject().apply {
                                    put("name", "initiate_upi_payment")
                                    put("description", "Initiate a UPI payment to a specified user. Use this when the user asks to pay or send money.")
                                    put("parameters", JSONObject().apply {
                                        put("type", "OBJECT")
                                        put("properties", JSONObject().apply {
                                            put("amount", JSONObject().apply { put("type", "NUMBER") })
                                            put("recipientName", JSONObject().apply { put("type", "STRING") })
                                            put("upiId", JSONObject().apply { put("type", "STRING"); put("description", "UPI ID of the recipient. If unknown, ask the user or leave empty.") })
                                        })
                                        put("required", JSONArray().apply { put("amount"); put("recipientName") })
                                    })
                                })
                                put(JSONObject().apply {
                                    put("name", "open_instagram_reel_creator")
                                    put("description", "Open the Instagram Reel Creator feature. Use this when the user asks to edit a video for Instagram, create a reel, generate viral captions or hashtags for a video.")
                                })
                                put(JSONObject().apply {
                                    put("name", "open_opportunity_center")
                                    put("description", "Open the RIX Opportunity Center. Use this when the user asks for business opportunities, freelance jobs, or their daily business briefing.")
                                })
                                // Add dynamic tools from registry
                                dynamicTools.forEach { tool ->
                                    put(JSONObject().apply {
                                        put("name", tool.id.replace(".", "_"))
                                        put("description", tool.description)
                                        put("parameters", mapToJsonObject(tool.parametersSchema))
                                    })
                                }
                            })
                        })
                    })

                    put("systemInstruction", JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", systemInstruction)
                            })
                        })
                    })
                    put("generationConfig", JSONObject().apply {
                        put("responseModalities", JSONArray().apply {
                            put("AUDIO"); put("TEXT")
                        })
                        put("speechConfig", JSONObject().apply {
                            put("voiceConfig", JSONObject().apply {
                                put("prebuiltVoiceConfig", JSONObject().apply {
                                    put("voiceName", "Aoede") // Female voice
                                })
                            })
                        })
                    })
                })
            }
            webSocketSession?.send(Frame.Text(setupMessage.toString()))
            
            // Listen to responses
            scope.launch {
                listenForMessages()
            }
        } catch (e: Exception) {
            val rawMsg = e.message ?: "Connection failed"
            val errorMsg = rawMsg.replace(Regex("key=[^&\\s]+"), "key=***MASKED***")
            android.util.Log.e("VoiceDiag", "GEMINI_ERROR: Connection Exception: $errorMsg")
            scope.launch {
                _errorFlow.emit("Gemini connection failed: $errorMsg")
            }
        }
    }
    
    private suspend fun listenForMessages() {
        val session = webSocketSession ?: return
        try {
            for (frame in session.incoming) {
                if (frame !is Frame.Text) continue
                val text = frame.readText()
                val json = JSONObject(text)
                
                if (json.has("setupComplete")) {
                    android.util.Log.d("VoiceDiag", "GEMINI_SESSION_READY: Setup complete received")
                    _setupCompleteFlow.emit(Unit)
                }
                if (json.has("serverContent")) {
                    val serverContent = json.getJSONObject("serverContent")
                    if (serverContent.has("interrupted") && serverContent.getBoolean("interrupted")) {
                        _turnCompleteFlow.emit(Unit)
                    }
                    if (serverContent.has("modelTurn")) {
                        val parts = serverContent.getJSONObject("modelTurn").getJSONArray("parts")
                        for (i in 0 until parts.length()) {
                            val part = parts.getJSONObject(i)
                            if (part.has("inlineData")) {
                                val inlineData = part.getJSONObject("inlineData")
                                val data = inlineData.getString("data")
                                val decoded = Base64.decode(data, Base64.DEFAULT)
                                if (decoded.isNotEmpty()) {
                                    android.util.Log.v("VoiceDiag", "AUDIO_RECEIVED: chunk length ${decoded.size}")
                                }
                                _audioFlow.emit(decoded)
                            }
                            if (part.has("functionCall")) {
                                val functionCall = part.getJSONObject("functionCall")
                                _functionCallFlow.emit(functionCall)
                            }
                            if (part.has("text")) {
                                val text = part.getString("text")
                                _textFlow.emit(text)
                            }
                        }
                    }
                    if (serverContent.has("turnComplete") && serverContent.getBoolean("turnComplete")) {
                        _turnCompleteFlow.emit(Unit)
                    }
                }
            }
            android.util.Log.d("VoiceDiag", "GEMINI_CLOSED: WebSocket loop ended normally")
            _disconnectedFlow.emit(Unit)
        } catch (e: Exception) {
            val rawMsg = e.message ?: "Connection closed unexpectedly"
            val errorMsg = rawMsg.replace(Regex("key=[^&\\s]+"), "key=***MASKED***")
            android.util.Log.e("VoiceDiag", "GEMINI_ERROR: WebSocket closed with exception: $errorMsg")
            _errorFlow.emit("Gemini connection closed: $errorMsg")
        }
    }
    
    suspend fun sendAudioChunk(pcmData: ByteArray) {
        if (pcmData.isEmpty()) return
        android.util.Log.v("VoiceDiag", "AUDIO_SEND: chunk length ${pcmData.size}")
        val base64Data = Base64.encodeToString(pcmData, Base64.NO_WRAP)
        val message = JSONObject().apply {
            put("realtimeInput", JSONObject().apply {
                put("mediaChunks", JSONArray().apply {
                    put(JSONObject().apply {
                        put("mimeType", "audio/pcm;rate=16000")
                        put("data", base64Data)
                    })
                })
            })
        }
        webSocketSession?.send(Frame.Text(message.toString()))
    }
    
    suspend fun interruptServer() {
        val json = JSONObject().apply {
            put("clientContent", JSONObject().apply {
                put("turnComplete", true)
            })
        }
        webSocketSession?.send(Frame.Text(json.toString()))
    }

    suspend fun sendClientContentMessage(message: String) {
        val json = JSONObject().apply {
            put("clientContent", JSONObject().apply {
                put("turns", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", message)
                            })
                        })
                    })
                })
                put("turnComplete", true)
            })
        }
        webSocketSession?.send(Frame.Text(json.toString()))
    }
    
    suspend fun disconnect() {
        webSocketSession?.close()
        webSocketSession = null
    }
}
