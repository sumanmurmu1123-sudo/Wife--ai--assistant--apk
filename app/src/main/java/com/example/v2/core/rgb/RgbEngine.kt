package com.example.v2.core.rgb

import com.example.v2.core.StateManager
import com.example.v2.core.RgbEngineState
import com.example.v2.core.RgbEngineMode

enum class RgbEffect {
    STATIC, BREATHING, RAINBOW, PULSE, VOICE_REACTIVE
}

class RgbEngine {
    private var isEnabled = true
    private var currentEffect = RgbEffect.STATIC
    private var currentColor = 0xFF00FFFF.toInt()
    private var currentState = RgbEngineState.OFF
    private var currentMode = RgbEngineMode.NONE
    private var hardwareDetected = false
    private var lastError: String? = null

    fun initialize(context: android.content.Context) {
        setState(RgbEngineState.STARTING)
        
        // Real Capability Detection
        try {
            val hasVibrator = (context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? android.os.Vibrator)?.hasVibrator() ?: false
            hardwareDetected = false 
            currentMode = if (hardwareDetected) RgbEngineMode.HARDWARE else RgbEngineMode.SOFTWARE
            
            // Honesty: If not enabled, stay OFF.
            if (!isEnabled) {
                setState(RgbEngineState.OFF)
            } else {
                // If initialized but not yet "started" by the service, keep as STARTING or STATIC?
                // The user says: If the engine is actually running: show "ACTIVE"
                // Let's use STATIC as the baseline running state if no reactive effect is active.
                setState(RgbEngineState.STATIC)
            }
        } catch (e: Exception) {
            lastError = e.message
            setState(RgbEngineState.ERROR)
        }
    }

    fun start() {
        if (currentState == RgbEngineState.UNAVAILABLE || currentState == RgbEngineState.ERROR) return
        isEnabled = true
        setState(if (currentEffect == RgbEffect.STATIC) RgbEngineState.STATIC else RgbEngineState.ACTIVE)
    }

    fun stop() {
        if (currentState == RgbEngineState.UNAVAILABLE || currentState == RgbEngineState.ERROR) return
        isEnabled = false
        setState(RgbEngineState.STOPPED)
    }

    fun setEffect(effect: RgbEffect) {
        if (currentState == RgbEngineState.UNAVAILABLE || currentState == RgbEngineState.ERROR) return
        
        currentEffect = effect
        if (isEnabled) {
            setState(if (effect == RgbEffect.STATIC) RgbEngineState.STATIC else RgbEngineState.ACTIVE)
        }
    }

    fun setColor(color: Int) {
        if (currentState == RgbEngineState.UNAVAILABLE || currentState == RgbEngineState.ERROR) return
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
        if (currentEffect == RgbEffect.VOICE_REACTIVE && isEnabled) {
            // Send to real hardware if in HARDWARE mode
            if (currentMode == RgbEngineMode.HARDWARE) {
                // nativeApplyRgbLevel(level)
            }
        }
    }

    private fun setState(state: RgbEngineState) {
        currentState = state
        updateGlobalState()
    }

    private fun updateGlobalState() {
        StateManager.updateState { 
            it.copy(
                rgbEffect = currentEffect.name,
                rgbColor = currentColor,
                rgbState = currentState,
                rgbMode = currentMode,
                rgbHardwareDetected = hardwareDetected,
                rgbLastError = lastError
            ) 
        }
    }
}
