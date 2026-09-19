package com.example.v2.core.diagnostics

import com.example.v2.core.StateManager

data class DiagnosticReport(
    val voiceEngineStatus: String,
    val geminiStatus: String,
    val memoryStatus: String,
    val toolEngineStatus: String,
    val pcConnectionStatus: String,
    val rgbStatus: String,
    val activeTasks: Int
)

class DiagnosticsEngine {
    fun generateReport(): DiagnosticReport {
        val state = StateManager.state.value
        return DiagnosticReport(
            voiceEngineStatus = "State: ${state.voiceState} | Mic: ${state.micState} | Playback: ${state.playbackState}",
            geminiStatus = "Connection: ${state.geminiState} | Network: ${if (state.networkAvailable) "UP" else "DOWN"}",
            memoryStatus = "Active Tasks: ${state.activeTaskCount}",
            toolEngineStatus = "OK",
            pcConnectionStatus = "State: ${state.pcState}",
            rgbStatus = "Effect: ${state.rgbEffect}",
            activeTasks = state.activeTaskCount
        )
    }
}
