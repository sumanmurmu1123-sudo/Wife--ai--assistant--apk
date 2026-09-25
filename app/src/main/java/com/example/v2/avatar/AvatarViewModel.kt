package com.example.v2.avatar

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AvatarState {
    IDLE, LISTENING, PROCESSING, SPEAKING, INTERRUPTED
}

class AvatarViewModel : ViewModel() {
    private val _state = MutableStateFlow(AvatarState.IDLE)
    val state: StateFlow<AvatarState> = _state.asStateFlow()

    private val _expression = MutableStateFlow("neutral")
    val expression: StateFlow<String> = _expression.asStateFlow()

    private val _audioLevel = MutableStateFlow(0f)
    val audioLevel: StateFlow<Float> = _audioLevel.asStateFlow()

    fun setState(newState: AvatarState) {
        _state.value = newState
        when (newState) {
            AvatarState.IDLE -> _expression.value = "neutral"
            AvatarState.LISTENING -> _expression.value = "curious"
            AvatarState.PROCESSING -> _expression.value = "thinking"
            AvatarState.SPEAKING -> _expression.value = "happy"
            AvatarState.INTERRUPTED -> _expression.value = "surprised"
        }
    }

    fun setExpression(newExpression: String) {
        _expression.value = newExpression
    }

    fun updateAudioLevel(level: Float) {
        _audioLevel.value = level
    }
}
