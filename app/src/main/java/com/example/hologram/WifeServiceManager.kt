package com.example.hologram

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class WifeServiceState {
    SERVICE_STOPPED,
    SERVICE_STARTING,
    SERVICE_RUNNING,
    SERVICE_ERROR,
    OVERLAY_PERMISSION_REQUIRED,
    SERVICE_UNAVAILABLE
}

object WifeServiceManager {
    private val _serviceState = MutableStateFlow(WifeServiceState.SERVICE_STOPPED)
    val serviceState: StateFlow<WifeServiceState> = _serviceState.asStateFlow()

    fun updateState(newState: WifeServiceState) {
        _serviceState.value = newState
    }
}
