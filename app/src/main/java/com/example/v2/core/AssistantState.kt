package com.example.v2.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// We will map VoiceViewModel's state into this or merge them.
enum class AssistantConnectionState {
    DISCONNECTED, CONNECTING, CONNECTED, ERROR, NOT_CONFIGURED
}

data class AssistantState(
    val voiceState: String = "IDLE",
    val geminiState: AssistantConnectionState = AssistantConnectionState.DISCONNECTED,
    val elevenLabsState: AssistantConnectionState = AssistantConnectionState.DISCONNECTED,
    val pcState: AssistantConnectionState = AssistantConnectionState.DISCONNECTED,
    val activeTaskCount: Int = 0,
    val rgbEffect: String = "STATIC",
    val rgbColor: Int = 0xFF00FFFF.toInt(),
    val diagnosticSummary: String = "OK"
)

object StateManager {
    private val _state = MutableStateFlow(AssistantState())
    val state = _state.asStateFlow()

    fun updateState(updater: (AssistantState) -> AssistantState) {
        _state.value = updater(_state.value)
    }
}
