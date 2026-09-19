package com.example.v2.core.pc

import com.example.v2.core.ServiceConnectionState
import com.example.v2.core.StateManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PcControlEngine {
    private val _connectionState = MutableStateFlow(ServiceConnectionState.DISCONNECTED)
    val connectionState = _connectionState.asStateFlow()

    suspend fun connect(ip: String) {
        _connectionState.value = ServiceConnectionState.CONNECTING
        StateManager.updateState { it.copy(pcState = ServiceConnectionState.CONNECTING) }
        delay(1000) // Simulate connection
        // For now, simulate success
        _connectionState.value = ServiceConnectionState.CONNECTED
        StateManager.updateState { it.copy(pcState = ServiceConnectionState.CONNECTED) }
    }

    suspend fun executeCommand(command: String, params: Map<String, String>): Boolean {
        if (_connectionState.value != ServiceConnectionState.CONNECTED) {
            return false
        }
        // Simulated authorized execution
        delay(500)
        return true
    }

    fun disconnect() {
        _connectionState.value = ServiceConnectionState.DISCONNECTED
        StateManager.updateState { it.copy(pcState = ServiceConnectionState.DISCONNECTED) }
    }
}
