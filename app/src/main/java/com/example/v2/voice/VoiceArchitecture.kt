package com.example.v2.voice

import kotlinx.coroutines.flow.StateFlow

sealed interface VoiceState {
    data object Idle : VoiceState
    data object Connecting : VoiceState
    data object Listening : VoiceState
    data object Thinking : VoiceState
    data object Speaking : VoiceState
    data object Interrupted : VoiceState
    data class Error(val message: String) : VoiceState
}

interface VoiceAssistantEngine {
    val state: StateFlow<VoiceState>
    suspend fun connect()
    suspend fun startListening()
    suspend fun stopListening()
    fun interrupt()
    suspend fun disconnect()
}
