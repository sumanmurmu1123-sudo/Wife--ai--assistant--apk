package com.example.v2.core.connectors

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ConnectorStatus {
    NOT_CONNECTED, CONNECTING, CONNECTED, ERROR
}

data class Connector(
    val id: String,
    val name: String,
    val status: ConnectorStatus,
    val capabilities: List<String>
)

class ConnectorEngine {
    private val _connectors = MutableStateFlow<List<Connector>>(emptyList())
    val connectors = _connectors.asStateFlow()

    init {
        // Register some default connectors
        _connectors.value = listOf(
            Connector("spotify", "Spotify", ConnectorStatus.NOT_CONNECTED, listOf("MEDIA_PLAYBACK")),
            Connector("home_assistant", "Smart Home", ConnectorStatus.NOT_CONNECTED, listOf("DEVICE_CONTROL"))
        )
    }

    suspend fun connect(id: String): Boolean {
        updateStatus(id, ConnectorStatus.CONNECTING)
        // Simulate auth flow
        kotlinx.coroutines.delay(1000)
        updateStatus(id, ConnectorStatus.CONNECTED)
        return true
    }
    
    private fun updateStatus(id: String, status: ConnectorStatus) {
        val current = _connectors.value.toMutableList()
        val idx = current.indexOfFirst { it.id == id }
        if (idx != -1) {
            current[idx] = current[idx].copy(status = status)
            _connectors.value = current
        }
    }
}
