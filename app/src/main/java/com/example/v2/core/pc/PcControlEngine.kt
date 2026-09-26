package com.example.v2.core.pc

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.v2.core.PcConnectionState
import com.example.v2.core.StateManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.*
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class PcControlEngine(private val context: Context) {
    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS) // For WebSockets
        .pingInterval(10, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private val _connectionState = MutableStateFlow(PcConnectionState.DISCONNECTED)
    val connectionState = _connectionState.asStateFlow()

    private var heartbeatJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val securePrefs = EncryptedSharedPreferences.create(
        context,
        "maya_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun connect(ip: String, port: Int = 8765) {
        if (_connectionState.value == PcConnectionState.CONNECTED) return

        val request = Request.Builder()
            .url("ws://$ip:$port")
            .build()

        _connectionState.value = PcConnectionState.CONNECTING
        StateManager.updateState { it.copy(pcState = PcConnectionState.CONNECTING, pcIp = ip) }

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                _connectionState.value = PcConnectionState.AUTHENTICATING
                StateManager.updateState { it.copy(pcState = PcConnectionState.AUTHENTICATING) }
                
                // Send Auth/Pairing request
                val token = securePrefs.getString("pc_pairing_token", null)
                val authMsg = JSONObject().apply {
                    put("type", "auth")
                    put("token", token ?: "")
                    put("deviceId", android.os.Build.ID)
                }
                webSocket.send(authMsg.toString())
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val json = JSONObject(text)
                    handleMessage(json)
                } catch (e: Exception) {
                    android.util.Log.e("PcEngine", "Error parsing message: ${e.message}")
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                disconnect()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                _connectionState.value = PcConnectionState.ERROR
                StateManager.updateState { it.copy(pcState = PcConnectionState.ERROR, lastError = "PC: ${t.message}") }
                stopHeartbeat()
            }
        })
    }

    private fun handleMessage(json: JSONObject) {
        val type = json.optString("type")
        when (type) {
            "auth_success" -> {
                _connectionState.value = PcConnectionState.CONNECTED
                val pcName = json.optString("pcName")
                val token = json.optString("token")
                if (token.isNotEmpty()) {
                    securePrefs.edit().putString("pc_pairing_token", token).apply()
                }
                StateManager.updateState { 
                    it.copy(
                        pcState = PcConnectionState.CONNECTED,
                        pcName = pcName,
                        pcPairingState = "PAIRED"
                    ) 
                }
                startHeartbeat()
            }
            "auth_failed" -> {
                _connectionState.value = PcConnectionState.AUTH_FAILED
                StateManager.updateState { it.copy(pcState = PcConnectionState.AUTH_FAILED) }
                disconnect()
            }
            "heartbeat_ack" -> {
                val latency = System.currentTimeMillis() - json.optLong("timestamp")
                StateManager.updateState { 
                    it.copy(
                        pcLatency = latency,
                        pcLastHeartbeat = System.currentTimeMillis()
                    ) 
                }
            }
            "status_update" -> {
                val pcName = json.optString("pcName")
                val ip = json.optString("ip")
                val iface = json.optString("interface")
                StateManager.updateState {
                    it.copy(
                        pcName = pcName,
                        pcIp = ip,
                        pcInterface = iface
                    )
                }
            }
        }
    }

    private fun startHeartbeat() {
        stopHeartbeat()
        heartbeatJob = scope.launch {
            while (isActive) {
                if (_connectionState.value == PcConnectionState.CONNECTED) {
                    val msg = JSONObject().apply {
                        put("type", "heartbeat")
                        put("timestamp", System.currentTimeMillis())
                    }
                    webSocket?.send(msg.toString())
                }
                delay(5000)
            }
        }
    }

    private fun stopHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = null
    }

    fun disconnect() {
        webSocket?.close(1000, "User requested")
        webSocket = null
        _connectionState.value = PcConnectionState.DISCONNECTED
        StateManager.updateState { it.copy(pcState = PcConnectionState.DISCONNECTED, pcLatency = 0) }
        stopHeartbeat()
    }

    suspend fun executeCommand(command: String, params: Map<String, String> = emptyMap()): Boolean {
        if (_connectionState.value != PcConnectionState.CONNECTED) return false
        
        val msg = JSONObject().apply {
            put("type", "command")
            put("id", java.util.UUID.randomUUID().toString())
            put("command", command)
            put("params", JSONObject(params))
            put("timestamp", System.currentTimeMillis())
        }
        return webSocket?.send(msg.toString()) ?: false
    }
}
