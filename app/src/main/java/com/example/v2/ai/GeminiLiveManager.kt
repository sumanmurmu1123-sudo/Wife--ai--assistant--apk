package com.example.v2.ai

import android.util.Base64
import com.example.v2.core.security.SecureStorage
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.header
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
        android.util.Log.e("MayaVoice", "[GEMINI] Uncaught Exception: $msg")
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
        android.util.Log.i("MayaVoice", "[STATE] Gemini: $conn | Session: $session | Error: $error | Sent: $totalBytesSent | Recv: $totalBytesReceived")
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

    suspend fun connect(
        systemInstruction: String = "", 
        dynamicTools: List<com.example.v2.core.tools.AssistantTool> = emptyList(), 
        debugMode: Boolean = false, 
        voiceName: String = "Aoede",
        backendWsUrl: String? = null
    ) {
        val now = System.currentTimeMillis()
        android.util.Log.i("MayaVoice", "[GEMINI] connect() called. isConnecting=$isConnecting, state=${_connectionState.value}")
        
        if (isConnecting) {
            if (now - lastConnectionAttemptTime < 30000) {
                android.util.Log.d("MayaVoice", "[GEMINI] Connection already in progress (<30s). Ignoring.")
                return
            } else {
                android.util.Log.w("MayaVoice", "[GEMINI] Stale connection attempt detected. Forcing reset.")
                isConnecting = false
            }
        }
        
        if (_connectionState.value == com.example.v2.core.GeminiConnectionState.CONNECTED && webSocketSession != null) {
            android.util.Log.d("MayaVoice", "[GEMINI] Already connected and session active.")
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
            
            val sessionToken = secureStorage?.getSessionToken()
            if (sessionToken.isNullOrBlank()) {
                updateState(com.example.v2.core.GeminiConnectionState.FAILED, com.example.v2.core.VoiceSessionState.ERROR, "Session Token Missing")
                _errorFlow.emit("Authentication Required")
                return
            }

            val finalUrl = if (backendWsUrl != null) {
                if (backendWsUrl.contains("?")) "$backendWsUrl&token=$sessionToken" 
                else "$backendWsUrl?token=$sessionToken"
            } else {
                updateState(com.example.v2.core.GeminiConnectionState.FAILED, com.example.v2.core.VoiceSessionState.ERROR, "Backend URL Missing")
                return
            }

            android.util.Log.i("MayaVoice", "[GEMINI] Connecting to Gateway: $finalUrl")
            updateState(
                if (reconnectionAttempt > 0) com.example.v2.core.GeminiConnectionState.RECONNECTING else com.example.v2.core.GeminiConnectionState.CONNECTING,
                com.example.v2.core.VoiceSessionState.CONNECTING
            )
            
            webSocketSession = kotlinx.coroutines.withTimeout(20000) {
                client.webSocketSession(finalUrl)
            }
            
            android.util.Log.i("MayaVoice", "[GEMINI] Gateway connection SUCCESS. Sending setup...")
            
            val setupMessage = JSONObject().apply {
                put("event", "session.start")
                put("setup", JSONObject().apply {
                    put("generationConfig", JSONObject().apply {
                        put("responseModalities", JSONArray().apply { put("AUDIO"); put("TEXT") })
                        put("speechConfig", JSONObject().apply {
                            put("voiceConfig", JSONObject().apply {
                                put("prebuiltVoiceConfig", JSONObject().apply { put("voiceName", voiceName) })
                            })
                        })
                    })
                })
            }
            
            android.util.Log.i("MayaVoice", "[GEMINI] Sending Maya session.start setup...")
            webSocketSession?.send(Frame.Text(setupMessage.toString()))
            
            setupTimeoutJob?.cancel()
            setupTimeoutJob = scope.launch {
                kotlinx.coroutines.delay(20000)
                if (_sessionState.value == com.example.v2.core.VoiceSessionState.CONNECTING) {
                    android.util.Log.e("MayaVoice", "[GEMINI] Setup Complete Timeout.")
                    updateState(com.example.v2.core.GeminiConnectionState.FAILED, com.example.v2.core.VoiceSessionState.ERROR, "Setup Timeout")
                    disconnect()
                }
            }
            
            // Start Heartbeat
            startHeartbeat()
            
            listenForMessages()
            
        } catch (e: Exception) {
            val rawMsg = e.message ?: "Connection failed"
            android.util.Log.e("MayaVoice", "[GEMINI] FATAL: $rawMsg", e)
            updateState(com.example.v2.core.GeminiConnectionState.FAILED, com.example.v2.core.VoiceSessionState.ERROR, rawMsg)
            _errorFlow.emit(rawMsg)
        } finally {
            isConnecting = false
        }
    }
    
    private fun startHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = scope.launch {
            // Wait for connected state
            while (isActive && _connectionState.value != com.example.v2.core.GeminiConnectionState.CONNECTED) {
                kotlinx.coroutines.delay(500L)
            }
            
            while (isActive && _connectionState.value == com.example.v2.core.GeminiConnectionState.CONNECTED) {
                kotlinx.coroutines.delay(30000L) // 30 seconds heartbeat
                try {
                    // Send an empty realtimeInput or clientContent to keep socket warm
                    val heartbeat = JSONObject().apply {
                        put("clientContent", JSONObject().apply {
                            put("turnComplete", false)
                        })
                    }
                    webSocketSession?.send(Frame.Text(heartbeat.toString()))
                    if (debugMode) android.util.Log.v("MayaVoice", "[GEMINI] Heartbeat sent.")
                } catch (e: Exception) {
                    android.util.Log.w("MayaVoice", "[GEMINI] Heartbeat failed: ${e.message}")
                    break
                }
            }
        }
    }

    private suspend fun listenForMessages() {
        val session = webSocketSession ?: return
        try {
            for (frame in session.incoming) {
                if (frame !is Frame.Text) continue
                val text = frame.readText()
                totalBytesReceived += text.length
                
                if (debugMode) android.util.Log.v("MayaVoice", "[MAYA_IN] $text")
                
                val json = JSONObject(text)
                val event = json.optString("event")
                
                when (event) {
                    "session.ready" -> {
                        android.util.Log.i("MayaVoice", "[GEMINI_READY] Maya Gateway Ready.")
                        setupTimeoutJob?.cancel()
                        reconnectionAttempt = 0
                        updateState(com.example.v2.core.GeminiConnectionState.CONNECTED, com.example.v2.core.VoiceSessionState.CONNECTED)
                        _setupCompleteFlow.emit(Unit)
                    }
                    "audio.output" -> {
                        val data = json.optString("data")
                        if (data.isNotEmpty()) {
                            val decoded = Base64.decode(data, Base64.DEFAULT)
                            if (decoded.isNotEmpty()) {
                                lastAudioPacketTime = System.currentTimeMillis()
                                _audioFlow.emit(decoded)
                            }
                        }
                    }
                    "interrupted" -> {
                        android.util.Log.i("MayaVoice", "[GEMINI] Interrupted.")
                        _turnCompleteFlow.emit(Unit)
                    }
                    "turn.completed" -> {
                        _turnCompleteFlow.emit(Unit)
                    }
                    "tool.call" -> {
                        android.util.Log.i("MayaVoice", "[GEMINI] Tool executed on backend: ${json.optString("name")}")
                    }
                    "error" -> {
                        val msg = json.optString("message", "Unknown Gateway Error")
                        android.util.Log.e("MayaVoice", "[GEMINI_SERVER_ERR] $msg")
                        updateState(com.example.v2.core.GeminiConnectionState.FAILED, com.example.v2.core.VoiceSessionState.ERROR, msg)
                        _errorFlow.emit(msg)
                        break
                    }
                    "pong" -> {
                        // Heartbeat pong
                    }
                }
            }
            updateState(com.example.v2.core.GeminiConnectionState.DISCONNECTED, com.example.v2.core.VoiceSessionState.DISCONNECTED)
            _disconnectedFlow.emit(Unit)
        } catch (e: Exception) {
            val rawMsg = e.message ?: "Connection closed unexpectedly"
            android.util.Log.e("MayaVoice", "[GEMINI_LOOP_ERR] $rawMsg", e)
            
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
                put("event", "audio.input")
                put("data", base64Data)
            }
            webSocketSession?.send(Frame.Text(message.toString()))
            totalBytesSent += pcmData.size
        } catch (e: Exception) {
            android.util.Log.e("MayaVoice", "[GEMINI_SEND_ERR] ${e.message}")
        }
    }
    
    suspend fun interruptServer() {
        val json = JSONObject().apply {
            put("event", "user.interrupt")
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
            android.util.Log.v("MayaVoice", "[GEMINI_RAW_OUT] ${json.toString()}")
        }
        webSocketSession?.send(Frame.Text(json.toString()))
    }
    
    suspend fun disconnect() {
        android.util.Log.d("MayaVoice", "[GEMINI] Disconnecting...")
        heartbeatJob?.cancel()
        heartbeatJob = null
        webSocketSession?.close()
        webSocketSession = null
        updateState(com.example.v2.core.GeminiConnectionState.DISCONNECTED, com.example.v2.core.VoiceSessionState.DISCONNECTED)
    }
}
