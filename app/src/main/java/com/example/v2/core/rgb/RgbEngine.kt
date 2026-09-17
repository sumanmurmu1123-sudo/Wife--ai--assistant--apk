package com.example.v2.core.rgb

import com.example.v2.core.StateManager

enum class RgbEffect {
    STATIC, BREATHING, RAINBOW, PULSE, VOICE_REACTIVE
}

class RgbEngine {
    private var isEnabled = true
    private var currentEffect = RgbEffect.STATIC
    private var currentColor = 0xFF00FFFF.toInt()

    fun setEffect(effect: RgbEffect) {
        currentEffect = effect
        updateGlobalState()
    }

    fun setColor(color: Int) {
        currentColor = color
        updateGlobalState()
    }

    fun setVoiceReactiveMode(isActive: Boolean) {
        if (isActive) {
            setEffect(RgbEffect.VOICE_REACTIVE)
        } else {
            setEffect(RgbEffect.STATIC)
        }
    }
    
    fun updateAudioLevel(level: Float) {
        if (currentEffect == RgbEffect.VOICE_REACTIVE) {
            // In a real device, send to RGB hardware or broadcast to UI
        }
    }

    private fun updateGlobalState() {
        StateManager.updateState { 
            it.copy(
                rgbEffect = currentEffect.name,
                rgbColor = currentColor
            ) 
        }
    }
}
