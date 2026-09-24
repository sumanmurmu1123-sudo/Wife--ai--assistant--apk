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
    
    // Runtime Diagnostics
    private var totalBytesSent: Long = 0
    private var totalBytesReceived: Long = 0
    private var lastAudioPacketTime: Long = 0

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
                lastError = error ?: if (conn == com.example.v2.core.GeminiConnectionState.DISCONNECTED) it.lastError else null,
                diagnosticSummary = "G: $conn | S: $session | B_OUT: $totalBytesSent | B_IN: $totalBytesReceived"
            ) 
        }
        android.util.Log.i("WifeVoice", "[STATE] Gemini: $conn | Session: $session | Error: $error | Sent: $totalBytesSent | Recv: $totalBytesReceived")
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
    private var lastConnectionAttemptTime = 0L
    private var setupTimeoutJob: Job? = null

    suspend fun connect(systemInstruction: String = "", apiKeyOverride: String? = null, dynamicTools: List<com.example.v2.core.tools.AssistantTool> = emptyList(), debugMode: Boolean = false, voiceName: String = "Aoede") {
        val now = System.currentTimeMillis()
        android.util.Log.i("WifeVoice", "[GEMINI] connect() called. isConnecting=$isConnecting, state=${_connectionState.value}")
        
        if (isConnecting) {
            if (now - lastConnectionAttemptTime < 30000) {
                android.util.Log.d("WifeVoice", "[GEMINI] Connection already in progress (<30s). Ignoring.")
                return
            } else {
                android.util.Log.w("WifeVoice", "[GEMINI] Stale connection attempt detected. Forcing reset.")
                isConnecting = false
            }
        }
        
        if (_connectionState.value == com.example.v2.core.GeminiConnectionState.CONNECTED && webSocketSession != null) {
            android.util.Log.d("WifeVoice", "[GEMINI] Already connected and session active.")
            return
        }
        
        isConnecting = true
        lastConnectionAttemptTime = now
        totalBytesSent = 0
        totalBytesReceived = 0
        
        try {
            if (systemInstruction.isNotBlank()) this.lastSystemInstruction = systemInstruction
            if (dynamicTools.isNotEmpty()) this.lastDynamicTools = dynamicTools
            this.lastVoiceName = voiceName
            this.debugMode = debugMode
            
            heartbeatJob?.cancel()
            heartbeatJob = null
            
            if (_connectionState.value != com.example.v2.core.GeminiConnectionState.RECONNECTING) {
                disconnect()
            }
            
            var apiKey = apiKeyOverride ?: secureStorage?.getApiKey() ?: ""
            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                try {
                    val buildConfigClass = Class.forName("com.example.BuildConfig")
                    val field = buildConfigClass.getField("GEMINI_API_KEY")
                    apiKey = field.get(null) as String
                } catch (e: Exception) {}
            }

            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                updateState(com.example.v2.core.GeminiConnectionState.FAILED, com.example.v2.core.VoiceSessionState.ERROR, "API Key Missing")
                _errorFlow.emit("API Key Required")
                return
            }

            android.util.Log.i("WifeVoice", "[GEMINI] Handshake start...")
            updateState(
                if (reconnectionAttempt > 0) com.example.v2.core.GeminiConnectionState.RECONNECTING else com.example.v2.core.GeminiConnectionState.CONNECTING,
                com.example.v2.core.VoiceSessionState.CONNECTING
            )
            
            webSocketSession = kotlinx.coroutines.withTimeout(20000) {
                val host = "generativelanguage.googleapis.com"
                val path = "/ws/google.ai.generativelanguage.v1alpha.GenerativeService.BidiGenerateContent"
                client.webSocketSession(method = HttpMethod.Get, host = host, port = 443, path = path) {
                    url.protocol = io.ktor.http.URLProtocol.WSS
                    url.parameters.append("key", apiKey)
                }
            }
            
            android.util.Log.i("WifeVoice", "[GEMINI] Handshake SUCCESS. Sending setup...")
            
            // Build Setup Message with proper structure
            val setupMessage = JSONObject().apply {
                put("setup", JSONObject().apply {
                    put("model", "models/gemini-2.0-flash-exp")
                    
                    put("generationConfig", JSONObject().apply {
                        put("responseModalities", JSONArray().apply { put("AUDIO"); put("TEXT") })
                        put("speechConfig", JSONObject().apply {
                            put("voiceConfig", JSONObject().apply {
                                put("prebuiltVoiceConfig", JSONObject().apply { put("voiceName", voiceName) })
                            })
                        })
                    })

                    put("tools", JSONArray().apply {
                        put(JSONObject().apply {
                            put("functionDeclarations", JSONArray().apply {
                                // Static UPI tool
                                put(JSONObject().apply {
                                    put("name", "initiate_upi_payment")
                                    put("description", "Initiate a secure UPI payment. Ask for recipient name and amount.")
                                    put("parameters", JSONObject().apply {
                                        put("type", "OBJECT")
                                        put("properties", JSONObject().apply {
                                            put("amount", JSONObject().apply { put("type", "NUMBER") })
                                            put("recipientName", JSONObject().apply { put("type", "STRING") })
                                        })
                                        put("required", JSONArray().apply { put("amount"); put("recipientName") })
                                    })
                                })
                                
                                // Dynamic tools with safe names
                                (if (dynamicTools.isNotEmpty()) dynamicTools else lastDynamicTools).forEach { tool ->
                                    val safeName = tool.id.replace(Regex("[^a-zA-Z0-9_]"), "_")
                                    put(JSONObject().apply {
                                        put("name", safeName)
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
                                put("text", (if (systemInstruction.isNotBlank()) systemInstruction else lastSystemInstruction) + "\n\nIMPORTANT: You are a Polyglot. Always respond in the language the user is using. Your default favorite is Bengali.")
                            })
                        })
                    })
                })
            }
            
            webSocketSession?.send(Frame.Text(setupMessage.toString()))
            
            setupTimeoutJob?.cancel()
            setupTimeoutJob = scope.launch {
                kotlinx.coroutines.delay(15000)
                if (_sessionState.value == com.example.v2.core.VoiceSessionState.CONNECTING) {
                    android.util.Log.e("WifeVoice", "[GEMINI] Setup Complete Timeout.")
                    updateState(com.example.v2.core.GeminiConnectionState.FAILED, com.example.v2.core.VoiceSessionState.ERROR, "Setup Timeout")
                    disconnect()
                }
            }
            listenForMessages()
            
        } catch (e: Exception) {
            val rawMsg = e.message ?: "Connection failed"
            android.util.Log.e("WifeVoice", "[GEMINI] FATAL: $rawMsg", e)
            updateState(com.example.v2.core.GeminiConnectionState.FAILED, com.example.v2.core.VoiceSessionState.ERROR, rawMsg)
            _errorFlow.emit(rawMsg)
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
                totalBytesReceived += text.length
                
                if (debugMode) android.util.Log.v("WifeVoice", "[GEMINI_IN] $text")
                
                val json = JSONObject(text)
                
                if (json.has("error")) {
                    val errorObj = json.getJSONObject("error")
                    val msg = errorObj.optString("message", "Unknown Server Error")
                    android.util.Log.e("WifeVoice", "[GEMINI_SERVER_ERR] $msg")
                    updateState(com.example.v2.core.GeminiConnectionState.FAILED, com.example.v2.core.VoiceSessionState.ERROR, msg)
                    _errorFlow.emit(msg)
                    break
                }

                if (json.has("setupComplete")) {
                    android.util.Log.i("WifeVoice", "[GEMINI_READY] Setup Complete.")
                    setupTimeoutJob?.cancel()
                    reconnectionAttempt = 0
                    updateState(com.example.v2.core.GeminiConnectionState.CONNECTED, com.example.v2.core.VoiceSessionState.CONNECTED)
                    _setupCompleteFlow.emit(Unit)
                }

                if (json.has("serverContent")) {
                    val serverContent = json.getJSONObject("serverContent")
                    if (serverContent.optBoolean("interrupted", false)) {
                        android.util.Log.i("WifeVoice", "[GEMINI] Interrupted by server.")
                        _turnCompleteFlow.emit(Unit)
                    }
                    if (serverContent.has("modelTurn")) {
                        val parts = serverContent.getJSONObject("modelTurn").getJSONArray("parts")
                        for (i in 0 until parts.length()) {
                            val part = parts.getJSONObject(i)
                            if (part.has("inlineData")) {
                                val data = part.getJSONObject("inlineData").getString("data")
                                val decoded = Base64.decode(data, Base64.DEFAULT)
                                if (decoded.isNotEmpty()) {
                                    lastAudioPacketTime = System.currentTimeMillis()
                                    _audioFlow.emit(decoded)
                                }
                            }
                            if (part.has("functionCall")) {
                                _functionCallFlow.emit(part.getJSONObject("functionCall"))
                            }
                            if (part.has("text")) {
                                _textFlow.emit(part.getString("text"))
                            }
                        }
                    }
                    if (serverContent.optBoolean("turnComplete", false)) {
                        _turnCompleteFlow.emit(Unit)
                    }
                }
            }
            updateState(com.example.v2.core.GeminiConnectionState.DISCONNECTED, com.example.v2.core.VoiceSessionState.DISCONNECTED)
            _disconnectedFlow.emit(Unit)
        } catch (e: Exception) {
            val rawMsg = e.message ?: "Connection closed unexpectedly"
            android.util.Log.e("WifeVoice", "[GEMINI_LOOP_ERR] $rawMsg", e)
            
            if (reconnectionAttempt < maxReconnectionAttempts && !rawMsg.contains("429") && !rawMsg.contains("403")) {
                reconnectionAttempt++
                updateState(com.example.v2.core.GeminiConnectionState.RECONNECTING, com.example.v2.core.VoiceSessionState.CONNECTING)
                scope.launch {
                    kotlinx.coroutines.delay(3000L)
                    connect(systemInstruction = lastSystemInstruction, dynamicTools = lastDynamicTools, voiceName = lastVoiceName)
                }
            } else {
                updateState(com.example.v2.core.GeminiConnectionState.FAILED, com.example.v2.core.VoiceSessionState.ERROR, rawMsg)
                _errorFlow.emit(rawMsg)
            }
        }
    }
    
    suspend fun sendAudioChunk(pcmData: ByteArray, sampleRate: Int = 16000) {
        if (pcmData.isEmpty() || _connectionState.value != com.example.v2.core.GeminiConnectionState.CONNECTED) return
        try {
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
            webSocketSession?.send(Frame.Text(message.toString()))
            totalBytesSent += pcmData.size
        } catch (e: Exception) {
            android.util.Log.e("WifeVoice", "[GEMINI_SEND_ERR] ${e.message}")
        }
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
