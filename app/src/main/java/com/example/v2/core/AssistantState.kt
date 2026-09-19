package com.example.v2.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

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

data class AssistantState(
    val voiceState: String = "IDLE",
    val geminiState: GeminiConnectionState = GeminiConnectionState.DISCONNECTED,
    val elevenLabsState: ServiceConnectionState = ServiceConnectionState.DISCONNECTED,
    val pcState: ServiceConnectionState = ServiceConnectionState.DISCONNECTED,
    val activeTaskCount: Int = 0,
    val rgbEffect: String = "STATIC",
    val rgbColor: Int = 0xFF00FFFF.toInt(),
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
    val lastError: String? = null
)

object StateManager {
    private val _state = MutableStateFlow(AssistantState())
    val state = _state.asStateFlow()

    fun updateState(updater: (AssistantState) -> AssistantState) {
        _state.value = updater(_state.value)
    }
}
