package com.example.v2.voice

import kotlinx.coroutines.flow.StateFlow

sealed interface VoiceState {
    val displayText: String

    data object Idle : VoiceState { override val displayText = "IDLE" }
    data object MicPermissionRequired : VoiceState { override val displayText = "MIC PERMISSION REQUIRED" }
    data object MicUnavailable : VoiceState { override val displayText = "MIC UNAVAILABLE" }
    data object Connecting : VoiceState { override val displayText = "CONNECTING..." }
    data object Connected : VoiceState { override val displayText = "CONNECTED" }
    data object Listening : VoiceState { override val displayText = "LISTENING..." }
    data object Thinking : VoiceState { override val displayText = "THINKING..." }
    data object Speaking : VoiceState { override val displayText = "SPEAKING..." }
    data object Interrupted : VoiceState { override val displayText = "INTERRUPTED" }
    data object Reconnecting : VoiceState { override val displayText = "RECONNECTING..." }
    data object Disconnected : VoiceState { override val displayText = "DISCONNECTED" }
    data object VoiceUnavailable : VoiceState { override val displayText = "VOICE UNAVAILABLE" }
    data class Error(val message: String) : VoiceState { override val displayText = message }
    data object NotConfigured : VoiceState { override val displayText = "API KEY NOT CONFIGURED" }
}

sealed interface LanguageState {
    data object Auto : LanguageState
    data object Detecting : LanguageState
    data class Detected(val code: String, val name: String, val confidence: Float) : LanguageState
    data class Manual(val code: String, val name: String) : LanguageState
    data object Unsupported : LanguageState
}

interface VoiceAssistantEngine {
    val state: StateFlow<VoiceState>
    suspend fun connect()
    suspend fun startListening()
    suspend fun stopListening()
    fun interrupt()
    suspend fun disconnect()
}
