package com.example.v2.voice

import kotlinx.coroutines.flow.StateFlow

sealed interface VoiceState {
    val displayText: String

    data object Idle : VoiceState { override val displayText = "Disabled" }
    data object PermissionRequired : VoiceState { override val displayText = "Microphone permission required" }
    data object Initializing : VoiceState { override val displayText = "Connecting..." }
    data object Connecting : VoiceState { override val displayText = "Connecting..." }
    data object Connected : VoiceState { override val displayText = "Connected" }
    data object Listening : VoiceState { override val displayText = "Listening..." }
    data object Thinking : VoiceState { override val displayText = "Thinking..." }
    data object Speaking : VoiceState { override val displayText = "Speaking..." }
    data object Interrupted : VoiceState { override val displayText = "Connected" }
    data object Reconnecting : VoiceState { override val displayText = "Reconnecting..." }
    data object Disconnected : VoiceState { override val displayText = "Disconnected" }
    data class Error(val message: String) : VoiceState { override val displayText = message }
    data object Unavailable : VoiceState { override val displayText = "Disabled" }
}

interface VoiceAssistantEngine {
    val state: StateFlow<VoiceState>
    suspend fun connect()
    suspend fun startListening()
    suspend fun stopListening()
    fun interrupt()
    suspend fun disconnect()
}
