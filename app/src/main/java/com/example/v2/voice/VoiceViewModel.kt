package com.example.v2.voice

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.v2.ai.GeminiLiveManager
import com.example.v2.audio.AudioCaptureManager
import com.example.v2.audio.AudioPlaybackManager
import com.example.v2.avatar.AvatarAnimation
import com.example.v2.avatar.AvatarController
import com.example.v2.language.LanguageManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VoiceViewModel : ViewModel() {
    
    private val _engineState = MutableStateFlow<VoiceState>(VoiceState.Idle)
    val state: StateFlow<VoiceState> = _engineState.asStateFlow()

    private val geminiLiveManager = GeminiLiveManager()
    private val audioCaptureManager = AudioCaptureManager()
    private val audioPlaybackManager = AudioPlaybackManager()
    private val avatarController = AvatarController()
    
    private var captureJob: Job? = null
    private var playbackJob: Job? = null
    
    init {
        avatarController.loadAvatar("models/wife_avatar.glb")
        
        // Listen for AI Audio
        viewModelScope.launch {
            geminiLiveManager.audioFlow.collect { pcmData ->
                if (_engineState.value == VoiceState.Speaking || _engineState.value == VoiceState.Thinking) {
                    _engineState.value = VoiceState.Speaking
                    avatarController.setLipSyncActive(true)
                    audioPlaybackManager.playChunk(pcmData)
                }
            }
        }
        
        // Listen for Turn Complete
        viewModelScope.launch {
            geminiLiveManager.turnCompleteFlow.collect {
                if (_engineState.value == VoiceState.Speaking) {
                    avatarController.setLipSyncActive(false)
                    // Return to listening
                    _engineState.value = VoiceState.Listening
                }
            }
        }
        
        // Listen for Errors
        viewModelScope.launch {
            geminiLiveManager.errorFlow.collect { errorMsg ->
                Log.e("VoiceViewModel", "Gemini Live Error: \$errorMsg")
                _engineState.value = VoiceState.Error(errorMsg)
                cleanupAudio()
            }
        }
    }

    fun onMicrophoneTapped(context: android.content.Context) {
        when (_engineState.value) {
            is VoiceState.Idle, is VoiceState.Interrupted, is VoiceState.Error -> {
                startConversation(context)
            }
            is VoiceState.Listening, is VoiceState.Thinking, is VoiceState.Speaking -> {
                interruptConversation()
            }
            is VoiceState.Connecting -> {
                // Do nothing
            }
        }
    }

    fun triggerAction(actionName: String, context: android.content.Context) {
        // Intercept current action
        interruptConversation()
        
        viewModelScope.launch {
            startConversation(context)
            // Send a client content message based on action
            geminiLiveManager.sendClientContentMessage("The user triggered the action: \$actionName")
        }
    }

    private fun startConversation(context: android.content.Context) {
        viewModelScope.launch {
            _engineState.value = VoiceState.Connecting
            
            val prefs = context.getSharedPreferences("wife_v2_prefs", android.content.Context.MODE_PRIVATE)
            val languageMode = prefs.getString("language_mode", "AUTO_DETECT")
            val preferredLanguage = prefs.getString("preferred_language", "Bengali")
            val bossName = prefs.getString("boss_name", "Sujithero") ?: "Sujithero"
            
            val languageInstruction = when (languageMode) {
                "FIXED" -> "You MUST speak and respond ONLY in \$preferredLanguage. Do not switch languages."
                "MULTILINGUAL" -> "You are in MULTILINGUAL mode. You can speak \$preferredLanguage and also mix naturally with other languages if the user does."
                else -> "AUTO DETECT MODE: You automatically detect the language spoken and respond in that same language natively. Your preferred default is \$preferredLanguage."
            }

            // Connect to Gemini Live
            val systemInstruction = """
                You are a Wife Assistant. You are young, confident, smart, playful, warm, slightly teasing, and emotionally responsive.
                The user's name is \$bossName.
                Your personality must remain consistent across languages. Do not mechanically translate; use natural expressions.
                You are voice-first, so keep your responses conversational and relatively brief.
                \$languageInstruction
            """.trimIndent()
            
            geminiLiveManager.connect(systemInstruction)
            
            _engineState.value = VoiceState.Listening
            avatarController.playAnimation(AvatarAnimation.LISTENING)
            
            // Start capturing mic audio and sending to Gemini
            captureJob?.cancel()
            captureJob = viewModelScope.launch {
                try {
                    audioCaptureManager.startCapture().collect { pcmData ->
                        if (_engineState.value == VoiceState.Listening) {
                            geminiLiveManager.sendAudioChunk(pcmData)
                        } else if (_engineState.value == VoiceState.Speaking) {
                            // If user speaks while AI is speaking, interrupt!
                            interruptConversation()
                            startConversation(context) // restart listening
                        }
                    }
                } catch (e: Exception) {
                    _engineState.value = VoiceState.Error("Mic unavailable")
                }
            }
        }
    }

    private fun interruptConversation() {
        _engineState.value = VoiceState.Interrupted
        avatarController.playAnimation(AvatarAnimation.IDLE)
        cleanupAudio()
        
        viewModelScope.launch {
            geminiLiveManager.disconnect()
            _engineState.value = VoiceState.Idle
        }
    }
    
    private fun cleanupAudio() {
        captureJob?.cancel()
        captureJob = null
        audioCaptureManager.stopCapture()
        audioPlaybackManager.stopPlayback()
        avatarController.setLipSyncActive(false)
    }

    override fun onCleared() {
        super.onCleared()
        cleanupAudio()
        audioPlaybackManager.release()
        viewModelScope.launch {
            geminiLiveManager.disconnect()
        }
    }
}
