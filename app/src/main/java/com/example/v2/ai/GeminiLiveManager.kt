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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
        engine {
            config {
                connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                readTimeout(0, java.util.concurrent.TimeUnit.SECONDS) // For WebSockets
                writeTimeout(0, java.util.concurrent.TimeUnit.SECONDS)
            }
        }
    }
    
    private val exceptionHandler = kotlinx.coroutines.CoroutineExceptionHandler { _, throwable ->
        val msg = throwable.message ?: "Uncaught Exception"
        android.util.Log.e("WifeVoice", "[GEMINI] Uncaught Exception: $msg")
        updateState(com.example.v2.core.GeminiConnectionState.FAILED, com.example.v2.core.VoiceSessionState.ERROR, "Critical Error: $msg")
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob() + exceptionHandler)
    
    private var webSocketSession: WebSocketSession? = null
    private var secureStorage: SecureStorage? = null

    fun init(context: android.content.Context) {
        secureStorage = SecureStorage(context)
    }
    
    private val _connectionState = MutableStateFlow<com.example.v2.core.GeminiConnectionState>(com.example.v2.core.GeminiConnectionState.DISCONNECTED)
    val connectionState: kotlinx.coroutines.flow.StateFlow<com.example.v2.core.GeminiConnectionState> = _connectionState.asStateFlow()

    private val _sessionState = MutableStateFlow<com.example.v2.core.VoiceSessionState>(com.example.v2.core.VoiceSessionState.DISCONNECTED)
    val sessionState: StateFlow<com.example.v2.core.VoiceSessionState> = _sessionState.asStateFlow()

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

    private fun updateState(conn: com.example.v2.core.GeminiConnectionState, session: com.example.v2.core.VoiceSessionState, error: String? = null) {
        _connectionState.value = conn
        _sessionState.value = session
        com.example.v2.core.StateManager.updateState { 
            it.copy(
                geminiState = conn, 
                voiceSessionState = session, 
                lastError = error ?: if (conn == com.example.v2.core.GeminiConnectionState.DISCONNECTED) it.lastError else null
            ) 
        }
        android.util.Log.d("WifeVoice", "[STATE] Gemini: $conn | Session: $session | Error: $error")
    }

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

    private var reconnectionAttempt = 0
    private val maxReconnectionAttempts = 5
    private var heartbeatJob: Job? = null
    private var debugMode: Boolean = false
    private var lastSystemInstruction: String = ""
    private var lastDynamicTools: List<com.example.v2.core.tools.AssistantTool> = emptyList()
    private var lastVoiceName: String = "Aoede"

    private var isConnecting = false
    private var setupTimeoutJob: Job? = null

    suspend fun connect(systemInstruction: String = "", apiKeyOverride: String? = null, dynamicTools: List<com.example.v2.core.tools.AssistantTool> = emptyList(), debugMode: Boolean = false, voiceName: String = "Aoede") {
        android.util.Log.i("WifeVoice", "[GEMINI] connect() called. isConnecting=$isConnecting, state=${_connectionState.value}")
        if (isConnecting && _connectionState.value != com.example.v2.core.GeminiConnectionState.RECONNECTING) {
            android.util.Log.d("WifeVoice", "[GEMINI] Connection already in progress. Ignoring.")
            return
        }
        if (_connectionState.value == com.example.v2.core.GeminiConnectionState.CONNECTED) {
            android.util.Log.d("WifeVoice", "[GEMINI] Already connected. Ignoring.")
            return
        }
        isConnecting = true
        
        try {
            if (systemInstruction.isNotBlank()) {
                this.lastSystemInstruction = systemInstruction
            }
            if (dynamicTools.isNotEmpty()) {
                this.lastDynamicTools = dynamicTools
            }
            this.lastVoiceName = voiceName
            
            this.debugMode = debugMode
            // Reset heartbeat if exists
            heartbeatJob?.cancel()
            heartbeatJob = null
            
            if (_connectionState.value != com.example.v2.core.GeminiConnectionState.RECONNECTING) {
                disconnect()
            }
            
            var apiKey = apiKeyOverride ?: secureStorage?.getApiKey() ?: ""
            
            // Fallback to BuildConfig
            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                try {
                    val buildConfigClass = Class.forName("com.example.BuildConfig")
                    val field = buildConfigClass.getField("GEMINI_API_KEY")
                    val buildConfigKey = field.get(null) as? String
                    if (!buildConfigKey.isNullOrBlank() && buildConfigKey != "MY_GEMINI_API_KEY") {
                        apiKey = buildConfigKey
                    }
                } catch (e: Exception) {}
            }

            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                updateState(com.example.v2.core.GeminiConnectionState.FAILED, com.example.v2.core.VoiceSessionState.ERROR, "API Key Required")
                _errorFlow.emit("API Key Required")
                return
            }

            android.util.Log.d("WifeVoice", "[GEMINI] Connecting... Attempt ${reconnectionAttempt + 1}")
            if (reconnectionAttempt > 0) {
                updateState(com.example.v2.core.GeminiConnectionState.RECONNECTING, com.example.v2.core.VoiceSessionState.CONNECTING)
            } else {
                updateState(com.example.v2.core.GeminiConnectionState.CONNECTING, com.example.v2.core.VoiceSessionState.CONNECTING)
            }
            
            webSocketSession = try {
                kotlinx.coroutines.withTimeout(30000) {
                    val host = "generativelanguage.googleapis.com"
                    val path = "/ws/google.ai.generativelanguage.v1alpha.GenerativeService.BidiGenerateContent"
                    
                    client.webSocketSession(
                        method = HttpMethod.Get,
                        host = host,
                        port = 443,
                        path = path
                    ) {
                        url.protocol = io.ktor.http.URLProtocol.WSS
                        url.parameters.append("key", apiKey)
                    }
                }
            } catch (e: Exception) {
                val msg = e.message ?: "WebSocket Handshake Failed"
                android.util.Log.e("WifeVoice", "[GEMINI] Handshake Error: $msg")
                throw e
            }
            
            android.util.Log.i("WifeVoice", "[GEMINI] WebSocket open, awaiting setupComplete")
            
            // Send setup
            val setupMessage = JSONObject().apply {
                put("setup", JSONObject().apply {
                    put("model", "models/gemini-2.5-flash-native-audio-preview-12-2025")
                    
                    put("generation_config", JSONObject().apply {
                        put("speech_config", JSONObject().apply {
                            put("voice_config", JSONObject().apply {
                                put("prebuilt_voice_config", JSONObject().apply {
                                    put("voice_name", voiceName)
                                })
                            })
                        })
                    })

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
                    val toolsToUse = if (dynamicTools.isNotEmpty()) dynamicTools else lastDynamicTools
                    toolsToUse.forEach { tool ->
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
                                put("text", (if (systemInstruction.isNotBlank()) systemInstruction else lastSystemInstruction) + """
                                    
                                    CRITICAL VOICE & SPEECH GUIDELINES:
                                    - You are in MULTILINGUAL MODE. Automatically detect the user's language and respond in the SAME language natively.
                                    - Human-like conversational delivery: Avoid robotic, monotone, or word-by-word delivery.
                                    - Natural rhythm: Use smooth speech rhythm with natural emphasis on important words.
                                    - Natural pauses: Use ellipses (...) or em-dashes (—) to create natural pauses between thoughts for a human-like flow.
                                    - Expression: Your voice ($voiceName) should sound expressive, warm, and emotionally connected.
                                    - No fake breathing: Do not manually type breathing sounds; let the voice engine handle the naturalism.
                                """.trimIndent())
                            })
                        })
                    })
                    put("generationConfig", JSONObject().apply {
                        put("responseModalities", JSONArray().apply {
                            put("AUDIO")
                            put("TEXT")
                        })
                        put("speechConfig", JSONObject().apply {
                            put("voiceConfig", JSONObject().apply {
                                put("prebuiltVoiceConfig", JSONObject().apply {
                                    put("voiceName", voiceName)
                                })
                            })
                        })
                    })
                })
            }
            webSocketSession?.send(Frame.Text(setupMessage.toString()))
            android.util.Log.d("WifeVoice", "[SESSION] Setup message sent")
            
            // Listen to responses
            setupTimeoutJob?.cancel()
            setupTimeoutJob = scope.launch {
                kotlinx.coroutines.delay(20000)
                if (_connectionState.value == com.example.v2.core.GeminiConnectionState.CONNECTING || _connectionState.value == com.example.v2.core.GeminiConnectionState.RECONNECTING) {
                    android.util.Log.e("WifeVoice", "[GEMINI] Setup complete timeout")
                    updateState(com.example.v2.core.GeminiConnectionState.FAILED, com.example.v2.core.VoiceSessionState.ERROR, "Setup Timeout")
                    _errorFlow.emit("Setup Timeout: Check API Key or Region")
                    disconnect()
                }
            }
            listenForMessages()
            
        } catch (e: Exception) {
            val rawMsg = e.message ?: "Connection failed"
            val errorMsg = rawMsg.replace(Regex("key=[^&\\s]+"), "key=***MASKED***")
            android.util.Log.e("WifeVoice", "[GEMINI] Connection Exception: $errorMsg", e)
            isConnecting = false
            
            val finalError = when {
                errorMsg.contains("429") || errorMsg.contains("resource_exhausted", ignoreCase = true) -> 
                    "Gemini API Quota Exceeded (429). Please check your billing or wait."
                errorMsg.contains("403") || errorMsg.contains("forbidden", ignoreCase = true) ->
                    "Gemini API Access Forbidden (403). Check if API is enabled and region is supported."
                errorMsg.contains("400") -> "Bad Request (400): Model or parameters invalid."
                errorMsg.contains("Job was cancelled", ignoreCase = true) -> "Connection attempt was cancelled."
                errorMsg.contains("Timed out", ignoreCase = true) -> "Connection Timed Out. Check your internet."
                else -> "Gemini connection failed: $errorMsg"
            }
            
            updateState(com.example.v2.core.GeminiConnectionState.FAILED, com.example.v2.core.VoiceSessionState.ERROR, finalError)
            _errorFlow.emit(finalError)
        } finally {
            isConnecting = false
        }
    }
    
    private suspend fun listenForMessages() {
        val session = webSocketSession ?: return
        try {
            for (frame in session.incoming) {
                if (frame !is Frame.Text) continue
                val text = frame.readText()
                if (debugMode) {
                    android.util.Log.v("WifeVoice", "[GEMINI_RAW_IN] $text")
                }
                val json = JSONObject(text)
                
                if (json.has("error")) {
                    val errorObj = json.getJSONObject("error")
                    val msg = errorObj.optString("message", "Unknown Server Error")
                    val code = errorObj.optInt("code", 0)
                    android.util.Log.e("WifeVoice", "[GEMINI] Server JSON Error: $msg (code: $code)")
                    updateState(com.example.v2.core.GeminiConnectionState.FAILED, com.example.v2.core.VoiceSessionState.ERROR, "Server Error ($code): $msg")
                    _errorFlow.emit("Server Error: $msg")
                    break
                }

                if (json.has("setupComplete")) {
                    android.util.Log.i("WifeVoice", "[SESSION] Setup complete received")
                    setupTimeoutJob?.cancel()
                    reconnectionAttempt = 0
                    updateState(com.example.v2.core.GeminiConnectionState.CONNECTED, com.example.v2.core.VoiceSessionState.CONNECTED)
                    _setupCompleteFlow.emit(Unit)
                    
                    // Start heartbeat AFTER setup complete
                    heartbeatJob?.cancel()
                    heartbeatJob = scope.launch {
                        while (isActive) {
                            kotlinx.coroutines.delay(20000) // Heartbeat every 20s
                            try {
                                if (webSocketSession != null && webSocketSession!!.isActive) {
                                    android.util.Log.v("WifeVoice", "[GEMINI] Sending heartbeat...")
                                    webSocketSession?.send(Frame.Text(JSONObject().apply { 
                                        put("clientContent", JSONObject().apply { 
                                            put("turnComplete", false) 
                                        }) 
                                    }.toString()))
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("WifeVoice", "[GEMINI] Heartbeat failed: ${e.message}")
                                break
                            }
                        }
                    }
                }
                if (json.has("serverContent")) {
                    val serverContent = json.getJSONObject("serverContent")
                    if (serverContent.has("interrupted") && serverContent.getBoolean("interrupted")) {
                        android.util.Log.d("WifeVoice", "[SESSION] Interrupted by server")
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
                                    android.util.Log.v("WifeVoice", "[AUDIO_OUT] Received bytes: ${decoded.size}")
                                }
                                _audioFlow.emit(decoded)
                            }
                            if (part.has("functionCall")) {
                                val functionCall = part.getJSONObject("functionCall")
                                android.util.Log.d("WifeVoice", "[SESSION] Function call: ${functionCall.optString("name")}")
                                _functionCallFlow.emit(functionCall)
                            }
                            if (part.has("text")) {
                                val text = part.getString("text")
                                android.util.Log.v("WifeVoice", "[SESSION] Received text: $text")
                                _textFlow.emit(text)
                            }
                        }
                    }
                    if (serverContent.has("turnComplete") && serverContent.getBoolean("turnComplete")) {
                        android.util.Log.v("WifeVoice", "[SESSION] Turn complete")
                        _turnCompleteFlow.emit(Unit)
                    }
                }
            }
            android.util.Log.d("WifeVoice", "[GEMINI] WebSocket loop ended normally. Moving to DISCONNECTED.")
            updateState(com.example.v2.core.GeminiConnectionState.DISCONNECTED, com.example.v2.core.VoiceSessionState.DISCONNECTED)
            _disconnectedFlow.emit(Unit)
        } catch (e: Exception) {
            val rawMsg = e.message ?: "Connection closed unexpectedly"
            val errorMsg = rawMsg.replace(Regex("key=[^&\\s]+"), "key=***MASKED***")
            android.util.Log.e("WifeVoice", "[GEMINI] WebSocket loop TERMINATED with exception: $errorMsg", e)
            
            // Auto-reconnect logic
            if (reconnectionAttempt < maxReconnectionAttempts && !rawMsg.contains("429") && !rawMsg.contains("403") && !rawMsg.contains("resource_exhausted", ignoreCase = true)) {
                reconnectionAttempt++
                updateState(com.example.v2.core.GeminiConnectionState.RECONNECTING, com.example.v2.core.VoiceSessionState.CONNECTING)
                android.util.Log.i("WifeVoice", "[GEMINI] Attempting auto-reconnect ($reconnectionAttempt/$maxReconnectionAttempts)...")
                scope.launch {
                    kotlinx.coroutines.delay(2000L * reconnectionAttempt)
                    connect(systemInstruction = lastSystemInstruction, dynamicTools = lastDynamicTools, voiceName = lastVoiceName)
                }
            } else {
                val finalError = if (rawMsg.contains("429") || rawMsg.contains("resource_exhausted", ignoreCase = true)) {
                    "Gemini API Quota Exceeded (429). Please check your billing or wait."
                } else {
                    "Gemini connection closed: $errorMsg"
                }
                updateState(com.example.v2.core.GeminiConnectionState.FAILED, com.example.v2.core.VoiceSessionState.ERROR, finalError)
                _errorFlow.emit(finalError)
            }
        }
    }
    
    suspend fun sendAudioChunk(pcmData: ByteArray, sampleRate: Int = 16000) {
        if (pcmData.isEmpty()) return
        android.util.Log.v("WifeVoice", "[AUDIO_IN] Sending bytes: ${pcmData.size} at rate $sampleRate")
        val base64Data = Base64.encodeToString(pcmData, Base64.NO_WRAP)
        val message = JSONObject().apply {
            put("realtimeInput", JSONObject().apply {
                put("mediaChunks", JSONArray().apply {
                    put(JSONObject().apply {
                        put("mimeType", "audio/pcm;rate=$sampleRate")
                        put("data", base64Data)
                    })
                })
            })
        }
        if (debugMode) {
            android.util.Log.v("WifeVoice", "[GEMINI_RAW_OUT] ${message.toString()}")
        }
        webSocketSession?.send(Frame.Text(message.toString()))
    }
    
    suspend fun interruptServer() {
        val json = JSONObject().apply {
            put("clientContent", JSONObject().apply {
                put("turnComplete", true)
            })
        }
        if (debugMode) {
            android.util.Log.v("WifeVoice", "[GEMINI_RAW_OUT] ${json.toString()}")
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
        if (debugMode) {
            android.util.Log.v("WifeVoice", "[GEMINI_RAW_OUT] ${json.toString()}")
        }
        webSocketSession?.send(Frame.Text(json.toString()))
    }
    
    suspend fun disconnect() {
        android.util.Log.d("WifeVoice", "[GEMINI] Disconnecting...")
        heartbeatJob?.cancel()
        heartbeatJob = null
        webSocketSession?.close()
        webSocketSession = null
        updateState(com.example.v2.core.GeminiConnectionState.DISCONNECTED, com.example.v2.core.VoiceSessionState.DISCONNECTED)
    }
}
