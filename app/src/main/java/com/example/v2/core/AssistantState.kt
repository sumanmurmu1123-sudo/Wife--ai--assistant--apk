package com.example.v2.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope

// We will map VoiceViewModel's state into this or merge them.
enum class PermissionState {
    GRANTED, REQUIRED, DENIED, PERMANENTLY_DENIED
}

enum class GeminiConnectionState {
    DISCONNECTED, CONNECTING, CONNECTED, RECONNECTING, FAILED
}

enum class MicrophoneState {
    UNAVAILABLE, PERMISSION_REQUIRED, INITIALIZING, READY, RECORDING, ERROR
}

enum class PlaybackState {
    IDLE, PREPARING, PLAYING, INTERRUPTED, ERROR
}

enum class ServiceConnectionState {
    DISCONNECTED, CONNECTING, CONNECTED, ERROR, NOT_CONFIGURED
}

enum class RgbEngineState {
    OFF, STARTING, ACTIVE, STATIC, UNAVAILABLE, ERROR, STOPPED
}

enum class RgbEngineMode {
    HARDWARE, SOFTWARE, NONE
}

enum class VoiceSessionState {
    DISCONNECTED, CONNECTING, CONNECTED, MIC_INITIALIZING, LISTENING, THINKING, SPEAKING, ERROR
}

enum class PcConnectionState {
    DISCONNECTED, DISCOVERING, CONNECTING, AUTHENTICATING, CONNECTED, RECONNECTING, NETWORK_UNAVAILABLE, AUTH_FAILED, TIMEOUT, ERROR
}

enum class OverlayState {
    VISIBLE, SUSPENDED_FOR_PERMISSION, HIDDEN, ERROR
}

data class AssistantState(
    val voiceSessionState: VoiceSessionState = VoiceSessionState.DISCONNECTED,
    val voiceState: String = "IDLE",
    val geminiState: GeminiConnectionState = GeminiConnectionState.DISCONNECTED,
    val elevenLabsState: ServiceConnectionState = ServiceConnectionState.DISCONNECTED,
    val pcState: PcConnectionState = PcConnectionState.DISCONNECTED,
    val pcName: String? = null,
    val pcIp: String? = null,
    val pcInterface: String? = null,
    val pcLatency: Long = 0L,
    val pcLastHeartbeat: Long = 0L,
    val pcPairingState: String = "UNPAIRED",
    val activeTaskCount: Int = 0,
    val rgbEffect: String = "STATIC",
    val rgbColor: Int = 0xFF00FFFF.toInt(),
    val rgbState: RgbEngineState = RgbEngineState.OFF,
    val rgbMode: RgbEngineMode = RgbEngineMode.NONE,
    val rgbHardwareDetected: Boolean = false,
    val rgbLastError: String? = null,
    val diagnosticSummary: String = "OK",
    val audioLevel: Float = 0f,
    val micPermissionGranted: Boolean = false,
    val micAvailable: Boolean = false,
    val currentLanguage: String = "Detecting...",
    val languageConfidence: Float = 0f,
    val ttsAvailable: Boolean = false,
    
    // Authoritative states
    val permissionState: PermissionState = PermissionState.REQUIRED,
    val micState: MicrophoneState = MicrophoneState.INITIALIZING,
    val playbackState: PlaybackState = PlaybackState.IDLE,
    val networkAvailable: Boolean = true,
    val foregroundServiceRunning: Boolean = false,
    val overlayPermissionGranted: Boolean = false,
    val overlayState: OverlayState = OverlayState.HIDDEN,
    val lastError: String? = null,

    // Hardware Capabilities (Compatibility v4.05)
    val hasMicrophone: Boolean = true,
    val hasSpeaker: Boolean = true,
    val hasBluetooth: Boolean = false,
    val hasCamera: Boolean = false,
    val hasGps: Boolean = false,
    val hasVibrator: Boolean = false,
    val hasBiometrics: Boolean = false,
    val hasTelephony: Boolean = false,
    val hasOverlaySupport: Boolean = false,
    val hasNotificationSupport: Boolean = true,
    val isLowRamDevice: Boolean = false,
    val androidVersion: Int = android.os.Build.VERSION.SDK_INT,
    val manufacturer: String = android.os.Build.MANUFACTURER,
    val model: String = android.os.Build.MODEL,
    
    // Weather System (v4.05)
    val weatherState: WeatherUiState = WeatherUiState.Loading
)

sealed class WeatherUiState {
    object Loading : WeatherUiState()
    object PermissionRequired : WeatherUiState()
    object LocationServicesDisabled : WeatherUiState()
    object LocationUnavailable : WeatherUiState()
    object ApiNotConfigured : WeatherUiState()
    data class Success(val data: WeatherData) : WeatherUiState()
    data class Error(val message: String, val isNetworkError: Boolean = false) : WeatherUiState()
}

data class WeatherData(
    val locationName: String,
    val temperature: Float,
    val feelsLike: Float,
    val condition: String,
    val humidity: Int,
    val windSpeed: Float,
    val precipitationProbability: Int,
    val sunrise: String,
    val sunset: String,
    val lastUpdated: Long
)

object StateManager {
    private val _state = MutableStateFlow(AssistantState())
    val state = _state.asStateFlow()

    private val _toggleVoiceEvent = kotlinx.coroutines.flow.MutableSharedFlow<Unit>(replay = 0)
    val toggleVoiceEvent = _toggleVoiceEvent.asSharedFlow()

    fun updateState(updater: (AssistantState) -> AssistantState) {
        _state.value = updater(_state.value)
    }

    fun triggerVoiceToggle() {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
            _toggleVoiceEvent.emit(Unit)
        }
    }
}
