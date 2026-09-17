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
            voiceEngineStatus = state.voiceState,
            geminiStatus = state.geminiState.name,
            memoryStatus = "OK", // Can query MemoryEngine
            toolEngineStatus = "OK",
            pcConnectionStatus = state.pcState.name,
            rgbStatus = "${state.rgbEffect} (${state.rgbColor})",
            activeTasks = state.activeTaskCount
        )
    }
}
