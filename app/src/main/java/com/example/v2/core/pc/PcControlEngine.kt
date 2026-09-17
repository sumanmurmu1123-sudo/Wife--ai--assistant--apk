package com.example.v2.core.pc

import com.example.v2.core.AssistantConnectionState
import com.example.v2.core.StateManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PcControlEngine {
    private val _connectionState = MutableStateFlow(AssistantConnectionState.DISCONNECTED)
    val connectionState = _connectionState.asStateFlow()

    suspend fun connect(ip: String) {
        _connectionState.value = AssistantConnectionState.CONNECTING
        StateManager.updateState { it.copy(pcState = AssistantConnectionState.CONNECTING) }
        delay(1000) // Simulate connection
        // For now, simulate success
        _connectionState.value = AssistantConnectionState.CONNECTED
        StateManager.updateState { it.copy(pcState = AssistantConnectionState.CONNECTED) }
    }

    suspend fun executeCommand(command: String, params: Map<String, String>): Boolean {
        if (_connectionState.value != AssistantConnectionState.CONNECTED) {
            return false
        }
        // Simulated authorized execution
        delay(500)
        return true
    }

    fun disconnect() {
        _connectionState.value = AssistantConnectionState.DISCONNECTED
        StateManager.updateState { it.copy(pcState = AssistantConnectionState.DISCONNECTED) }
    }
}
