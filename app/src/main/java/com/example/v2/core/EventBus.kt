package com.example.v2.core

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed interface AssistantEvent {
    data object UserSpeechStarted : AssistantEvent
    data object UserSpeechStopped : AssistantEvent
    data object GeminiConnected : AssistantEvent
    data object GeminiDisconnected : AssistantEvent
    data class GeminiError(val message: String) : AssistantEvent
    data class ToolRequested(val toolId: String, val params: Map<String, Any?>) : AssistantEvent
    data class ToolCompleted(val toolId: String, val success: Boolean, val message: String) : AssistantEvent
    data class ToolFailed(val toolId: String, val reason: String) : AssistantEvent
    data class TaskStarted(val taskId: String) : AssistantEvent
    data class TaskCompleted(val taskId: String) : AssistantEvent
    data class RgbChanged(val color: Int, val effect: String) : AssistantEvent
    data class PermissionChanged(val permissionId: String, val granted: Boolean) : AssistantEvent
    data object PcConnected : AssistantEvent
    data object PcDisconnected : AssistantEvent
}

object EventBus {
    private val _events = MutableSharedFlow<AssistantEvent>(extraBufferCapacity = 64)
    val events = _events.asSharedFlow()

    fun publish(event: AssistantEvent) {
        _events.tryEmit(event)
    }
}
