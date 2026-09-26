package com.example.v2.voice

import android.app.Application
import android.content.Context
import com.example.data.UserPreferences
import com.example.v2.ai.GeminiLiveManager
import com.example.v2.core.AssistantState
import com.example.v2.core.GeminiConnectionState
import com.example.v2.core.StateManager
import com.example.v2.core.VoiceSessionState
import com.example.v2.core.security.SecureStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Persistently manages the Gemini Live voice session independently of UI lifecycle.
 * Owned by MayaAssistantCore.
 */
class VoiceAssistantManager(
    private val context: Context,
    private val geminiLiveManager: GeminiLiveManager,
    private val toolRegistry: com.example.v2.core.tools.ToolRegistry,
    private val memoryEngine: com.example.v2.core.memory.MemoryEngine,
    private val backendRepository: com.example.v2.core.network.BackendRepository
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val userPreferences = UserPreferences(context)
    private val secureStorage = SecureStorage(context)
    
    private var connectionJob: Job? = null

    init {
        scope.launch {
            StateManager.toggleVoiceEvent.collect {
                toggleConnection()
            }
        }
    }

    fun toggleConnection() {
        val state = StateManager.state.value
        if (state.geminiState == GeminiConnectionState.CONNECTED || state.geminiState == GeminiConnectionState.CONNECTING) {
            disconnect()
        } else {
            connect()
        }
    }

    fun connect() {
        if (connectionJob?.isActive == true) {
            android.util.Log.d("MayaVoice", "[MANAGER] Connection already active. Skipping.")
            return
        }
        
        // Network check
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
        val network = connectivityManager?.activeNetwork
        val capabilities = connectivityManager?.getNetworkCapabilities(network)
        val hasInternet = capabilities?.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        
        if (!hasInternet) {
            android.util.Log.e("MayaVoice", "[MANAGER] No internet connection detected.")
            StateManager.updateState { it.copy(lastError = "No Internet Connection") }
            return
        }

        // Permission check
        if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) 
            != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            android.util.Log.w("MayaVoice", "[MANAGER] Mic permission missing.")
            StateManager.updateState { it.copy(lastError = "Microphone Permission Required") }
            return
        }

        connectionJob = scope.launch {
            try {
                StateManager.updateState { it.copy(geminiState = GeminiConnectionState.CONNECTING, voiceSessionState = VoiceSessionState.CONNECTING, lastError = null) }
                
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    android.widget.Toast.makeText(context, "Maya is waking up...", android.widget.Toast.LENGTH_SHORT).show()
                }

                startVoiceService()

                // Mandatory Backend Authentication
                android.util.Log.i("MayaVoice", "[MANAGER] Authenticating with Maya Gateway...")
                val authResult = backendRepository.authenticate(userPreferences.bossName)
                
                authResult.onSuccess { auth ->
                    android.util.Log.i("MayaVoice", "[MANAGER] Auth Success. Session: ${auth.session_token}")
                    secureStorage.saveSessionToken(auth.session_token)
                }.onFailure {
                    throw Exception("Backend Authentication Failed: ${it.message}")
                }

                val backendUrl = userPreferences.backendUrl
                val finalWsUrl = if (backendUrl.startsWith("http")) {
                    backendUrl.replace("http", "ws").removeSuffix("/") + "/ws/maya"
                } else {
                    "ws://$backendUrl/ws/maya"
                }

                geminiLiveManager.connect(
                    systemInstruction = "You are Maya. Speak Bengali/Hindi/English mix.", // Injected by backend actually, but keeping for legacy
                    dynamicTools = toolRegistry.getAllTools(),
                    debugMode = true,
                    voiceName = userPreferences.selectedVoiceSlate.voiceName,
                    backendWsUrl = finalWsUrl
                )
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Unknown Error"
                android.util.Log.e("MayaVoice", "[MANAGER] Connection failed: $errorMsg")
                
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    android.widget.Toast.makeText(context, "Maya Error: $errorMsg", android.widget.Toast.LENGTH_LONG).show()
                }
                
                StateManager.updateState { it.copy(geminiState = GeminiConnectionState.FAILED, voiceSessionState = VoiceSessionState.ERROR, lastError = errorMsg) }
            }
        }
    }

    fun disconnect() {
        scope.launch {
            geminiLiveManager.disconnect()
            connectionJob?.cancel()
            secureStorage.clearSessionToken()
        }
    }

    private fun startVoiceService() {
        val intent = android.content.Intent(context, com.example.v2.voice.service.VoiceForegroundService::class.java).apply {
            action = com.example.v2.voice.service.VoiceForegroundService.ACTION_START
        }
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (e: Exception) {
            android.util.Log.e("MayaVoice", "Service error: ${e.message}")
        }
    }
}

