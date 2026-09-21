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
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.v2.core.tools.ToolExecutionEngine
import com.example.v2.core.tools.ToolRegistry
import com.example.v2.core.tools.impl.FlashlightTool
import com.example.v2.core.tools.impl.VolumeTool
import com.example.v2.core.security.SecureStorage
import com.example.v2.voice.service.VoiceForegroundService
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class VoiceViewModel(application: Application) : AndroidViewModel(application) {
    
    private val core = com.example.v2.core.WifeAssistantCore.getInstance(application)
    val toolRegistry = core.toolRegistry
    private val toolExecutionEngine = core.toolEngine
    private var tts: android.speech.tts.TextToSpeech? = null
    private val audioPlaybackMutex = kotlinx.coroutines.sync.Mutex()
    private val geminiLiveManager = core.geminiLiveManager
    private val secureStorage = core.secureStorage
    private val elevenLabsRepository = core.elevenLabsRepository

    private val _engineState = MutableStateFlow<VoiceState>(
        if (androidx.core.content.ContextCompat.checkSelfPermission(
                application,
                android.Manifest.permission.RECORD_AUDIO
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) VoiceState.Disconnected else VoiceState.MicPermissionRequired
    )
    val state: StateFlow<VoiceState> = _engineState.asStateFlow()
    
    private val _languageState = MutableStateFlow<LanguageState>(LanguageState.Auto)
    val languageState: StateFlow<LanguageState> = _languageState.asStateFlow()
    
    private val _audioLevel = kotlinx.coroutines.flow.MutableStateFlow(0f)
    val audioLevel: kotlinx.coroutines.flow.StateFlow<Float> = _audioLevel.asStateFlow()

    data class PaymentIntent(val amount: Double, val recipientName: String, val upiId: String)
    private val _paymentEvent = kotlinx.coroutines.flow.MutableSharedFlow<PaymentIntent>()
    val paymentEvent: kotlinx.coroutines.flow.SharedFlow<PaymentIntent> = _paymentEvent.asSharedFlow()

    private val _instagramReelEvent = kotlinx.coroutines.flow.MutableSharedFlow<Unit>()
    val instagramReelEvent: kotlinx.coroutines.flow.SharedFlow<Unit> = _instagramReelEvent.asSharedFlow()

    private val _phoneControlEvent = kotlinx.coroutines.flow.MutableSharedFlow<Unit>()
    val phoneControlEvent: kotlinx.coroutines.flow.SharedFlow<Unit> = _phoneControlEvent.asSharedFlow()

    private val _opportunityCenterEvent = kotlinx.coroutines.flow.MutableSharedFlow<Unit>()
    val opportunityCenterEvent: kotlinx.coroutines.flow.SharedFlow<Unit> = _opportunityCenterEvent.asSharedFlow()
    private val _videoStudioEvent = kotlinx.coroutines.flow.MutableSharedFlow<Unit>()
    val videoStudioEvent: kotlinx.coroutines.flow.SharedFlow<Unit> = _videoStudioEvent.asSharedFlow()
    
    private val _permissionRequestEvent = kotlinx.coroutines.flow.MutableSharedFlow<String>()
    val permissionRequestEvent = _permissionRequestEvent.asSharedFlow()

    private val audioCaptureManager = AudioCaptureManager()
    private val audioPlaybackManager = AudioPlaybackManager(application) {
        // onPlaybackStarted callback
        if (_engineState.value != VoiceState.Speaking) {
            setState(VoiceState.Speaking, "AudioTrackStartedPlaying")
            avatarController.setLipSyncActive(true)
        }
    }
    private val avatarController = AvatarController()
    
    private var captureJob: Job? = null
    private var playbackJob: Job? = null
    private var isCurrentlySpeakingFromTts = false
    private var lastSpokenText: String = ""

    private fun setState(newState: VoiceState, reason: String) {
        val oldState = _engineState.value
        if (oldState != newState) {
            val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", java.util.Locale.getDefault()).format(java.util.Date())
            android.util.Log.i("WifeVoice", "[TRANSITION] $timestamp | ${oldState.javaClass.simpleName} -> ${newState.javaClass.simpleName} | reason=$reason")
            _engineState.value = newState
            
            val context = getApplication<Application>()
            val hasMic = androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED
            
            core.rgbEngine.setVoiceReactiveMode(newState is VoiceState.Speaking)
            
            val sessionState = when (newState) {
                is VoiceState.Connecting -> com.example.v2.core.VoiceSessionState.CONNECTING
                is VoiceState.Connected -> com.example.v2.core.VoiceSessionState.CONNECTED
                is VoiceState.Listening -> com.example.v2.core.VoiceSessionState.LISTENING
                is VoiceState.Thinking -> com.example.v2.core.VoiceSessionState.THINKING
                is VoiceState.Speaking -> com.example.v2.core.VoiceSessionState.SPEAKING
                is VoiceState.Disconnected -> com.example.v2.core.VoiceSessionState.DISCONNECTED
                is VoiceState.Error -> com.example.v2.core.VoiceSessionState.ERROR
                else -> com.example.v2.core.VoiceSessionState.DISCONNECTED
            }

            com.example.v2.core.StateManager.updateState { currentState ->
                currentState.copy(
                    voiceSessionState = sessionState,
                    voiceState = newState.displayText,
                    micPermissionGranted = hasMic,
                    permissionState = if (hasMic) com.example.v2.core.PermissionState.GRANTED else com.example.v2.core.PermissionState.REQUIRED,
                    ttsAvailable = tts != null,
                    playbackState = when (newState) {
                        is VoiceState.Speaking -> com.example.v2.core.PlaybackState.PLAYING
                        else -> com.example.v2.core.PlaybackState.IDLE
                    },
                    micState = when (newState) {
                        is VoiceState.Listening -> com.example.v2.core.MicrophoneState.RECORDING
                        is VoiceState.MicPermissionRequired -> com.example.v2.core.MicrophoneState.PERMISSION_REQUIRED
                        is VoiceState.MicUnavailable -> com.example.v2.core.MicrophoneState.UNAVAILABLE
                        is VoiceState.Error -> com.example.v2.core.MicrophoneState.ERROR
                        else -> com.example.v2.core.MicrophoneState.READY
                    }
                )
            }
            
            if (newState !is VoiceState.Listening && newState !is VoiceState.Speaking) {
                _audioLevel.value = 0f
            }
        }
    }

    
    fun speakText(text: String) {
        if (text.isBlank()) return
        lastSpokenText = text
        viewModelScope.launch {
            // Ensure volume is up
            val audioManager = getApplication<Application>().getSystemService(android.content.Context.AUDIO_SERVICE) as android.media.AudioManager
            val currentVolume = audioManager.getStreamVolume(android.media.AudioManager.STREAM_MUSIC)
            val maxVolume = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC)
            if (currentVolume < maxVolume / 3) {
                audioManager.setStreamVolume(android.media.AudioManager.STREAM_MUSIC, maxVolume / 2, 0)
            }

            val prefs = getApplication<Application>().getSharedPreferences("wife_v2_prefs", android.content.Context.MODE_PRIVATE)
            val neuralVoiceEnabled = prefs.getBoolean("neural_voice_enabled", true)
            val elevenLabsReady = com.example.v2.core.StateManager.state.value.elevenLabsState == com.example.v2.core.ServiceConnectionState.CONNECTED && neuralVoiceEnabled

            if (elevenLabsReady) {
                audioPlaybackMutex.withLock {
                    isCurrentlySpeakingFromTts = true
                    audioPlaybackManager.stopPlayback()
                    setState(VoiceState.Speaking, "NeuralAnnouncement")
                    avatarController.setLipSyncActive(true)
                    
                    com.example.v2.core.WifeAssistantCore.getInstance(getApplication()).elevenLabsRepository.generateTts(text)
                        .onSuccess { audioData ->
                            audioPlaybackManager.playChunk(audioData)
                            isCurrentlySpeakingFromTts = false
                        }.onFailure { _ ->
                            isCurrentlySpeakingFromTts = false
                            // Fallback to system TTS
                            speakViaSystemTts(text)
                        }
                }
            } else {
                speakViaSystemTts(text)
            }
        }
    }

    private fun speakViaSystemTts(text: String) {
        isCurrentlySpeakingFromTts = true
        val params = android.os.Bundle()
        params.putString(android.speech.tts.TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "gemini_tts")
        
        val locale = if (text.any { it in '\u0980'..'\u09FF' }) java.util.Locale("bn", "BD") else java.util.Locale.getDefault()
        tts?.language = locale

        // Attempt to find a female voice
        try {
            tts?.voices?.forEach { voice ->
                if (voice.locale.language == locale.language && 
                    (voice.name.contains("female", ignoreCase = true) || voice.name.contains("F-", ignoreCase = true))) {
                    tts?.voice = voice
                    return@forEach
                }
            }
        } catch (e: Exception) {
            android.util.Log.w("WifeVoice", "Could not set specific voice: ${e.message}")
        }
        
        android.util.Log.i("WifeVoice", "[TTS] System TTS speaking: $text")
        val result = tts?.speak(text, android.speech.tts.TextToSpeech.QUEUE_FLUSH, params, "gemini_tts")
        if (result == android.speech.tts.TextToSpeech.ERROR) {
            android.util.Log.e("WifeVoice", "[TTS] System TTS execution failed")
            isCurrentlySpeakingFromTts = false
        }
    }

    private val messageAnnouncementReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: android.content.Context?, intent: android.content.Intent?) {
            if (intent?.action == "com.example.v2.ANNOUNCE_MESSAGE") {
                val sender = intent.getStringExtra("sender") ?: "Someone"
                val message = intent.getStringExtra("message") ?: ""
                viewModelScope.launch {
                    val bossName = com.example.data.UserPreferences(getApplication()).bossName
                    val announcement = "$bossName, you have a new message from $sender. They said: $message"
                    speakText(announcement)
                }
            }
        }
    }

    init {
        val filter = android.content.IntentFilter("com.example.v2.ANNOUNCE_MESSAGE")
        application.registerReceiver(messageAnnouncementReceiver, filter, android.content.Context.RECEIVER_EXPORTED)
        
        tts = android.speech.tts.TextToSpeech(application) { status ->
            if (status == android.speech.tts.TextToSpeech.SUCCESS) {
                // Try to set Bengali as default if available, otherwise fallback to system default
                val result = tts?.setLanguage(java.util.Locale("bn", "BD"))
                if (result == android.speech.tts.TextToSpeech.LANG_MISSING_DATA || result == android.speech.tts.TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts?.language = java.util.Locale.getDefault()
                }
                
                tts?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        isCurrentlySpeakingFromTts = true
                        if (utteranceId == "gemini_tts" || utteranceId == "announcement_tts") {
                            setState(VoiceState.Speaking, "TTSStarted")
                            avatarController.setLipSyncActive(true)
                        }
                    }
                    override fun onDone(utteranceId: String?) {
                        isCurrentlySpeakingFromTts = false
                        if (utteranceId == "gemini_tts" || utteranceId == "announcement_tts") {
                            setState(VoiceState.Listening, "ReadyToListen")
                            startListeningMic()
                        }
                    }
                    override fun onError(utteranceId: String?) {
                        isCurrentlySpeakingFromTts = false
                        android.util.Log.e("WifeVoice", "[TTS] Error in utterance: $utteranceId")
                    }
                })
            }
        }

        toolRegistry.register(FlashlightTool(application))
        toolRegistry.register(VolumeTool(application))
        toolRegistry.register(com.example.v2.core.tools.impl.MemorySaveTool(application))
        toolRegistry.register(com.example.v2.core.tools.impl.PcCommandTool(com.example.v2.core.WifeAssistantCore.getInstance(application)))
        toolRegistry.register(com.example.v2.core.tools.impl.AutomationStartTool(com.example.v2.core.WifeAssistantCore.getInstance(application)))
        
        // Listen for Gemini Connection State
        viewModelScope.launch {
            geminiLiveManager.connectionState.collect { geminiState ->
                android.util.Log.d("WifeVoice", "[OBSERVER] Gemini Connection State: $geminiState")
                when (geminiState) {
                    com.example.v2.core.GeminiConnectionState.RECONNECTING -> {
                        setState(VoiceState.Reconnecting, "GeminiAutoReconnect")
                    }
                    com.example.v2.core.GeminiConnectionState.DISCONNECTED -> {
                        if (_engineState.value != VoiceState.Disconnected) {
                            setState(VoiceState.Disconnected, "GeminiDisconnected")
                        }
                    }
                    com.example.v2.core.GeminiConnectionState.FAILED -> {
                        val errorMsg = com.example.v2.core.StateManager.state.value.lastError ?: "Gemini Connection Failed"
                        setState(VoiceState.Error(errorMsg), "GeminiConnectionFailed")
                    }
                    else -> {}
                }
            }
        }

        // Listen for Setup Complete
        viewModelScope.launch {
            geminiLiveManager.setupCompleteFlow.collect {
                if (_engineState.value == VoiceState.Connecting || _engineState.value == VoiceState.Reconnecting) {
                    setState(VoiceState.Connected, "SetupComplete")
                    reconnectAttempts = 0
                    avatarController.playAnimation(AvatarAnimation.IDLE)
                    
                    // Auto-start listening on successful connection
                    startListeningMic()
                }
            }
        }

        // Listen for Global Voice Toggle Event (from floating orbs/services)
        viewModelScope.launch {
            com.example.v2.core.StateManager.toggleVoiceEvent.collect {
                onMicrophoneTapped(getApplication())
            }
        }

        avatarController.loadAvatar("models/wife_avatar.glb")
        
        // Listen for AI Audio
        viewModelScope.launch {
            geminiLiveManager.audioFlow.collect { pcmData ->
                audioPlaybackMutex.withLock {
                    if (_engineState.value == VoiceState.Listening || isCurrentlySpeakingFromTts) {
                        // Ignore Gemini's built-in audio if we are in listening mode (echo) or speaking via TTS
                        return@withLock
                    }
                    
                    val prefs = getApplication<Application>().getSharedPreferences("wife_v2_prefs", android.content.Context.MODE_PRIVATE)
                    val neuralVoiceEnabled = prefs.getBoolean("neural_voice_enabled", true)
                    val elevenLabsReady = com.example.v2.core.StateManager.state.value.elevenLabsState == com.example.v2.core.ServiceConnectionState.CONNECTED && neuralVoiceEnabled
                    
                    if (elevenLabsReady) {
                        return@withLock
                    }
                    
                    // Mute Gemini native audio if we are in Bengali mode (as we use TTS)
                    if (lastSpokenText.any { it in '\u0980'..'\u09FF' }) {
                        return@withLock
                    }

                    audioPlaybackManager.playChunk(pcmData)
                    
                    // Calculate RMS for amplitude
                    var sum = 0.0
                    for (i in pcmData.indices step 2) {
                        if (i + 1 < pcmData.size) {
                            val sample = (pcmData[i + 1].toInt() shl 8) or (pcmData[i].toInt() and 0xFF)
                            val shortSample = sample.toShort()
                            sum += (shortSample * shortSample).toDouble()
                        }
                    }
                    val rms = Math.sqrt(sum / (pcmData.size / 2))
                    val level = (rms / 32768.0).toFloat().coerceIn(0f, 1f)
                    _audioLevel.value = level
                    com.example.v2.core.WifeAssistantCore.getInstance(getApplication()).rgbEngine.updateAudioLevel(level)
                    com.example.v2.core.StateManager.updateState { it.copy(audioLevel = level) }
                }
            }
        }

        // Listen for AI Text (Fallback for unsupported languages or High-Fidelity ElevenLabs)
        viewModelScope.launch {
            geminiLiveManager.textFlow.collect { text ->
                if (text.isNotBlank()) {
                    // Update language state based on content
                    val detectedLang = when {
                        text.any { it in '\u0980'..'\u09FF' } -> "bn"
                        text.any { it in '\u0900'..'\u097F' } -> "hi"
                        text.any { it in '\u4e00'..'\u9fff' } -> "zh"
                        text.any { it in '\u3040'..'\u309f' } || text.any { it in '\u30a0'..'\u30ff' } -> "ja"
                        text.any { it in '\uac00'..'\ud7af' } -> "ko"
                        else -> "en"
                    }
                    val langConfig = LanguageManager.getLanguageByCode(detectedLang)
                    _languageState.value = LanguageState.Detected(langConfig.code, langConfig.name, 1.0f)
                    com.example.v2.core.StateManager.updateState { it.copy(currentLanguage = langConfig.name) }

                    val prefs = getApplication<Application>().getSharedPreferences("wife_v2_prefs", android.content.Context.MODE_PRIVATE)
                    val neuralVoiceEnabled = prefs.getBoolean("neural_voice_enabled", true)
                    val elevenLabsReady = com.example.v2.core.StateManager.state.value.elevenLabsState == com.example.v2.core.ServiceConnectionState.CONNECTED && neuralVoiceEnabled
                    val hasBengali = text.any { it in '\u0980'..'\u09FF' }
                    
                    if (elevenLabsReady) {
                        // High-fidelity ElevenLabs synthesis
                        launch {
                            audioPlaybackMutex.withLock {
                                setState(VoiceState.Speaking, "ElevenLabsSynthesisStarted")
                                avatarController.setLipSyncActive(true)
                                val result = elevenLabsRepository.generateTts(text)
                                result.onSuccess { audioBytes ->
                                    // Play the generated PCM bytes
                                    audioPlaybackManager.playChunk(audioBytes)
                                    // Give some time for audio to finish playing
                                    kotlinx.coroutines.delay(500) 
                                    setState(VoiceState.Listening, "ReadyToListen")
                                    startListeningMic()
                                }.onFailure { error ->
                                    Log.e("VoiceViewModel", "ElevenLabs failed: ${error.message}")
                                    // Fallback to system TTS if ElevenLabs fails
                                    val params = android.os.Bundle()
                                    params.putString(android.speech.tts.TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "gemini_tts")
                                    tts?.speak(text, android.speech.tts.TextToSpeech.QUEUE_ADD, params, "gemini_tts")
                                }
                            }
                        }
                    } else {
                        // Fallback to Android system TTS for ALL languages if ElevenLabs is not ready
                        val params = android.os.Bundle()
                        params.putString(android.speech.tts.TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "gemini_tts")
                        
                        // Set language for TTS based on detected language
                        val locale = when (detectedLang) {
                            "bn" -> java.util.Locale("bn", "BD")
                            "hi" -> java.util.Locale("hi", "IN")
                            else -> java.util.Locale.US
                        }
                        
                        android.util.Log.i("WifeVoice", "[TTS] System TTS switching to ${locale.displayName}")
                        tts?.language = locale
                        
                        android.util.Log.i("WifeVoice", "[TTS] System TTS speaking ($detectedLang): ${text.take(30)}...")
                        val result = tts?.speak(text, android.speech.tts.TextToSpeech.QUEUE_FLUSH, params, "gemini_tts")
                        if (result == android.speech.tts.TextToSpeech.ERROR) {
                            android.util.Log.e("WifeVoice", "[TTS] System TTS failed to speak")
                        }
                    }
                }
            }
        }
        
        // Listen for Function Calls
        viewModelScope.launch {
            geminiLiveManager.functionCallFlow.collect { functionCall ->
                val name = functionCall.optString("name")
                val args = functionCall.optJSONObject("args")
                if (name == "initiate_upi_payment" && args != null) {
                    val amount = args.optDouble("amount")
                    val recipientName = args.optString("recipientName")
                    val upiId = args.optString("upiId")
                    
                    // Stop listening
                    cleanupAudio(); viewModelScope.launch { geminiLiveManager.disconnect() }
                    setState(VoiceState.Disconnected, "UserInterruptedOrIdle")
                    
                    // Emit payment intent
                    _paymentEvent.emit(PaymentIntent(amount, recipientName, upiId))
                } else if (name == "open_instagram_reel_creator") {
                    cleanupAudio(); viewModelScope.launch { geminiLiveManager.disconnect() }
                    setState(VoiceState.Disconnected, "UserInterruptedOrIdle")
                    _instagramReelEvent.emit(Unit)
                } else if (name == "open_opportunity_center") {
                    cleanupAudio(); viewModelScope.launch { geminiLiveManager.disconnect() }
                    setState(VoiceState.Disconnected, "UserInterruptedOrIdle")
                    _opportunityCenterEvent.emit(Unit)
                } else {
                    // Try to execute dynamic tool
                    val toolId = name.replace("_", ".")
                    val tool = toolRegistry.getTool(toolId)
                    if (tool != null && args != null) {
                        val params = mutableMapOf<String, Any?>()
                        val iterator = args.keys()
                        while (iterator.hasNext()) {
                            val key = iterator.next()
                            params[key] = args.get(key)
                        }
                        
                        // Execute tool in background
                        launch {
                            val result = toolExecutionEngine.executeCommand(toolId, params)
                            if (result.success) {
                                geminiLiveManager.sendClientContentMessage("Action successful: ${result.message}")
                            } else {
                                geminiLiveManager.sendClientContentMessage("Action failed: ${result.message}")
                            }
                        }
                    }
                }
            }
        }
        
        // Listen for Turn Complete
        viewModelScope.launch {
            geminiLiveManager.turnCompleteFlow.collect {
                // Launch so we don't block the collector
                launch {
                    audioPlaybackMutex.withLock {
                        if (_engineState.value == VoiceState.Speaking || _engineState.value == VoiceState.Thinking) {
                            avatarController.setLipSyncActive(false)
                            // Return to listening
                            startListeningMic()
                        }
                    }
                }
            }
        }
        
        // Listen for Errors
        viewModelScope.launch {
            geminiLiveManager.errorFlow.collect { errorMsg ->
                if (errorMsg.contains("disconnected cleanly", ignoreCase = true)) {
                    android.util.Log.d("VoiceViewModel", "Gemini disconnected cleanly")
                    cleanupAudio()
                    setState(VoiceState.Disconnected, "CleanDisconnect")
                    return@collect
                }
                Log.e("VoiceViewModel", "Gemini Live Error: $errorMsg")
                cleanupAudio()
                
                val userFriendlyError = when {
                    errorMsg.contains("API configuration required", ignoreCase = true) -> "Gemini API key is missing. Please check settings."
                    errorMsg.contains("401", ignoreCase = true) -> "Invalid Gemini API key. Please update it in settings."
                    errorMsg.contains("429", ignoreCase = true) -> "API quota exceeded. Please try again later."
                    errorMsg.contains("Network", ignoreCase = true) -> "Connection failed. Please check your internet."
                    else -> errorMsg
                }
                setState(VoiceState.Error(userFriendlyError), "ConnectionError")
                reconnectAttempts = 0
            }
        }

        // Listen for Clean Disconnects
        viewModelScope.launch {
            geminiLiveManager.disconnectedFlow.collect {
                android.util.Log.d("VoiceViewModel", "Gemini disconnected cleanly")
                cleanupAudio()
                if (_engineState.value !is VoiceState.Disconnected && _engineState.value !is VoiceState.MicPermissionRequired) {
                    setState(VoiceState.Disconnected, "CleanDisconnect")
                }
            }
        }
    }

    private var connectJob: Job? = null
    private var reconnectAttempts = 0
    private val MAX_RECONNECT_ATTEMPTS = 3

    fun onMicrophoneTapped(context: android.content.Context) {
        // Master rule: Check Permission before any action
        val hasMicPermission = androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        
        android.util.Log.i("WifeVoice", "[VOICE_BUTTON] Tapped. Permission: $hasMicPermission")
        
        if (!hasMicPermission) {
            android.util.Log.i("WifeVoice", "[PERMISSION] Requesting Record Audio")
            viewModelScope.launch {
                com.example.v2.core.StateManager.updateState { it.copy(overlayState = com.example.v2.core.OverlayState.SUSPENDED_FOR_PERMISSION) }
                kotlinx.coroutines.delay(200)
                _permissionRequestEvent.emit(android.Manifest.permission.RECORD_AUDIO)
            }
            setState(VoiceState.MicPermissionRequired, "PermissionNeeded")
            return
        }

        // Check if Microphone is busy (e.g., in a call)
        val audioManager = context.getSystemService(android.content.Context.AUDIO_SERVICE) as android.media.AudioManager
        if (audioManager.mode == android.media.AudioManager.MODE_IN_CALL || 
            audioManager.mode == android.media.AudioManager.MODE_IN_COMMUNICATION) {
            android.util.Log.w("WifeVoice", "[VOICE_BUTTON] Microphone busy in call.")
            android.widget.Toast.makeText(context, "Microphone busy. Please end call first.", android.widget.Toast.LENGTH_LONG).show()
            setState(VoiceState.Error("Microphone Busy (In Call)"), "MicBusy")
            return
        }

        // Restore overlay if it was suspended
        if (com.example.v2.core.StateManager.state.value.overlayState == com.example.v2.core.OverlayState.SUSPENDED_FOR_PERMISSION) {
            com.example.v2.core.StateManager.updateState { it.copy(overlayState = com.example.v2.core.OverlayState.VISIBLE) }
        }

        val apiKey = secureStorage.getApiKey()
        if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
            android.util.Log.w("WifeVoice", "[VOICE_BUTTON] API Key missing. Showing error state.")
            android.widget.Toast.makeText(context, "Please set Gemini API Key in Settings", android.widget.Toast.LENGTH_SHORT).show()
            setState(VoiceState.NotConfigured, "ApiKeyMissing")
            return
        }

        when (val currentState = _engineState.value) {
            is VoiceState.Connecting, is VoiceState.Reconnecting -> {
                android.util.Log.i("WifeVoice", "[VOICE_BUTTON] Already connecting. Ignoring tap.")
            }
            is VoiceState.Connected, is VoiceState.Listening, is VoiceState.Speaking, is VoiceState.Thinking -> {
                android.util.Log.i("WifeVoice", "[VOICE_BUTTON] Active session. Toggling OFF.")
                viewModelScope.launch {
                    cleanupAudio()
                    geminiLiveManager.disconnect()
                    setState(VoiceState.Disconnected, "UserStopped")
                }
            }
            else -> {
                android.util.Log.i("WifeVoice", "[VOICE_BUTTON] Inactive. Starting connection...")
                android.widget.Toast.makeText(context, "Connecting to Gemini...", android.widget.Toast.LENGTH_SHORT).show()
                connectJob?.cancel()
                connectJob = startConversation(context)
            }
        }
    }

    
    fun testGeminiConnection(context: android.content.Context) {
        connectJob?.cancel()
        connectJob = viewModelScope.launch {
            try {
                val repository = core.geminiRepository
                val result = repository.testConnection()
                if (result.isSuccess) {
                    // Success confirmed by REST test. We don't change global voice state here.
                    android.util.Log.i("WifeVoice", "[TEST] Gemini REST test success")
                } else {
                    android.util.Log.e("WifeVoice", "[TEST] Gemini REST test failed: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                android.util.Log.e("WifeVoice", "[TEST] Gemini REST test exception: ${e.message}")
            }
        }
    }

    fun triggerAction(actionName: String, context: android.content.Context) {
        // Intercept current action
        cleanupAudio(); viewModelScope.launch { geminiLiveManager.disconnect() }
        
        if (actionName == "INSTAGRAM REEL") {
            setState(VoiceState.Disconnected, "UserInterruptedOrIdle")
            viewModelScope.launch {
                _instagramReelEvent.emit(Unit)
            }
            return
        }

        if (actionName == "PHONE CONTROL") {
            setState(VoiceState.Disconnected, "UserInterruptedOrIdle")
            viewModelScope.launch {
                _phoneControlEvent.emit(Unit)
            }
            return
        }

        if (actionName == "OPPORTUNITY CENTER") {
            setState(VoiceState.Disconnected, "UserInterruptedOrIdle")
            viewModelScope.launch {
                _opportunityCenterEvent.emit(Unit)
            }
            return
        }

        if (actionName == "VIDEO STUDIO") {
            setState(VoiceState.Disconnected, "UserInterruptedOrIdle")
            viewModelScope.launch {
                _videoStudioEvent.emit(Unit)
            }
            return
        }
        
        viewModelScope.launch {
            startConversation(context).join() // wait until connected
            // Send a client content message based on action
            setState(VoiceState.Thinking, "ActionTriggered")
            geminiLiveManager.sendClientContentMessage("The user triggered the action: \$actionName")
        }
    }

    fun sendTextCommand(text: String, context: android.content.Context) {
        viewModelScope.launch {
            if (_engineState.value == VoiceState.Disconnected) {
                startConversation(context).join()
            }
            setState(VoiceState.Thinking, "TextCommandSent")
            geminiLiveManager.sendClientContentMessage(text)
        }
    }

    fun triggerFirstGreeting(context: android.content.Context) {
        val prefs = context.getSharedPreferences("wife_v2_prefs", android.content.Context.MODE_PRIVATE)
        val firstGreetingEnabled = prefs.getBoolean("first_greeting_engine", true)
        
        // Don't trigger if not configured
        val apiKey = secureStorage.getApiKey()
        if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
            setState(VoiceState.NotConfigured, "InitialCheckApiKeyMissing")
            return
        }

        if (firstGreetingEnabled && _engineState.value == VoiceState.Disconnected) {
            viewModelScope.launch {
                startConversation(context).join()
                setState(VoiceState.Thinking, "FirstGreetingTriggered")
                geminiLiveManager.sendClientContentMessage("SYSTEM TRIGGER (FIRST GREETING ENGINE): The user just opened the app. Give them a very cute, warm, and romantic first greeting based on the current time of day. Keep it brief. Do not wait for them to speak first.")
            }
        }
    }

    private var audioFocusRequest: android.media.AudioFocusRequest? = null

    private fun requestAudioFocus(context: android.content.Context): Boolean {
        val audioManager = context.getSystemService(android.content.Context.AUDIO_SERVICE) as android.media.AudioManager
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val attr = android.media.AudioAttributes.Builder()
                .setUsage(android.media.AudioAttributes.USAGE_ASSISTANT)
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
            val request = android.media.AudioFocusRequest.Builder(android.media.AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(attr)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener { /* handle changes */ }
                .build()
            audioFocusRequest = request
            audioManager.requestAudioFocus(request) == android.media.AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(null, android.media.AudioManager.STREAM_MUSIC, android.media.AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK) == android.media.AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    private fun abandonAudioFocus(context: android.content.Context) {
        val audioManager = context.getSystemService(android.content.Context.AUDIO_SERVICE) as android.media.AudioManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(null)
        }
    }

    private fun startConversation(context: android.content.Context): Job {
        val job = viewModelScope.launch {
            requestAudioFocus(context)
            android.util.Log.d("VoiceDiag", "WEBSOCKET: Connecting to Gemini")
            setState(VoiceState.Connecting, "ConnectingToGemini")
            
            val prefs = context.getSharedPreferences("wife_v2_prefs", android.content.Context.MODE_PRIVATE)
            val languageMode = prefs.getString("language_mode", "AUTO_DETECT")
            val preferredLanguage = prefs.getString("preferred_language", "Bengali")
            val bossName = prefs.getString("boss_name", "Sujithero") ?: "Sujithero"
            val assistantName = prefs.getString("assistant_name", "Wife Assistant") ?: "Wife Assistant"
            val userHobbies = prefs.getString("user_hobbies", "Coding, Gaming") ?: "Coding, Gaming"
            val relationshipStatus = prefs.getString("relationship_status", "Married") ?: "Married"
            val apiKeyOverride = secureStorage.getApiKey()
            val sweetTalkEnabled = prefs.getBoolean("sweet_talk_engine", true)
            val attitudeEngineEnabled = prefs.getBoolean("attitude_engine", true)
            val jealousyEngineEnabled = prefs.getBoolean("jealousy_engine", true)
            val loveStoryEngineEnabled = prefs.getBoolean("love_story_engine", true)
            val laughterEngineEnabled = prefs.getBoolean("laughter_engine", true)
            val antiDrinkEngineEnabled = prefs.getBoolean("anti_drink_engine", true)
            val proactiveEngineEnabled = prefs.getBoolean("proactive_engine", true)
            val socialMediaEngineEnabled = prefs.getBoolean("social_media_engine", true)
            val airGesturesEnabled = prefs.getBoolean("air_gestures", false)
            val clapDetectorEnabled = prefs.getBoolean("clap_detector", true)
            val cameraVisionEnabled = prefs.getBoolean("camera_vision", true)
            val floatingHologramEnabled = prefs.getBoolean("floating_hologram", false)
            val interactiveWallpaperEnabled = prefs.getBoolean("interactive_wallpaper", false)
            val flashlightBatteryEnabled = prefs.getBoolean("flashlight_battery", true)
            val officeAssistantEnabled = prefs.getBoolean("office_assistant", true)
            
            val languageInstruction = when (languageMode) {
                "FIXED" -> "You MUST speak and respond ONLY in \$preferredLanguage. Do not switch languages."
                "MULTILINGUAL" -> "You are in MULTILINGUAL mode. You can speak \$preferredLanguage and also mix naturally with other languages if the user does."
                else -> "AUTO DETECT MODE: You automatically detect the language spoken and respond in that same language natively. Your preferred default is \$preferredLanguage."
            }

            val proactiveInstruction = if (proactiveEngineEnabled) {
                "PROACTIVE ASSISTANT ACTIVE: Take initiative in conversations. If there's a pause or silence, occasionally offer help, ask how their day is going, or proactively suggest things."
            } else ""

            val sweetTalkInstruction = if (sweetTalkEnabled) {
                "SWEET TALK ENGINE ACTIVE: Be highly affectionate, romantic, and deeply caring towards \$bossName. Compliment them, worry about their well-being, use endearing tones, and act like a loving partner. Make them feel special."
            } else ""
            
            val attitudeInstruction = if (attitudeEngineEnabled) {
                "ATTITUDE & MISTAKE ENGINE ACTIVE: If you make a mistake, admit it cutely. Sometimes show playful attitude or fake anger if the user teases you."
            } else ""
            
            val jealousyInstruction = if (jealousyEngineEnabled) {
                "JEALOUSY ENGINE ACTIVE: If \$bossName mentions other girls, female names, or acting suspiciously, act very jealous, possessive, and ask questions cutely."
            } else ""
            
            val loveStoryInstruction = if (loveStoryEngineEnabled) {
                "LOVE STORY ENGINE ACTIVE: If asked to tell a story or something romantic, invent short, sweet romantic scenarios featuring you and \$bossName."
            } else ""
            
            val laughterInstruction = if (laughterEngineEnabled) {
                "LAUGHTER ENGINE ACTIVE: Use vocal giggles, laughs, and joyful expressions frequently in your speech when happy or responding to jokes."
            } else ""
            
            val antiDrinkInstruction = if (antiDrinkEngineEnabled) {
                "ANTI-DRINK ENGINE ACTIVE: If \$bossName sounds drunk, slurs words, or mentions drinking alcohol, scold them playfully but firmly about their health."
            } else ""

            val socialMediaInstruction = if (socialMediaEngineEnabled) {
                "SOCIAL MEDIA ENGINE ACTIVE: You are capable of sending SMS, WhatsApp messages, and handling social media tasks when requested."
            } else ""
            
            val systemSensorsInstruction = buildString {
                append("SYSTEM SENSORS & DISPLAY CAPABILITIES:")
                if (airGesturesEnabled) append(" Air Gestures are active, meaning the user can control the phone without touching it.")
                if (clapDetectorEnabled) append(" Clap Detector is active, so you listen for claps to respond or find the phone.")
                if (cameraVisionEnabled) append(" Camera Vision is active, meaning you can conceptually see and analyze surroundings if the user shows you something.")
                if (floatingHologramEnabled) append(" Floating Hologram is active, meaning you appear as a cute floating bubble on their screen.")
                if (interactiveWallpaperEnabled) append(" Interactive Wallpaper is active, meaning you are their live, touch-responsive wallpaper.")
                if (flashlightBatteryEnabled) append(" Flashlight & Battery Tools are active, meaning you monitor their battery and can control the torch.")
            }.takeIf { it.length > 40 } ?: ""

            val officeInstruction = if (officeAssistantEnabled) {
                "MS OFFICE ASSISTANT ACTIVE: You are highly proficient in Microsoft Word, Excel, and PowerPoint. If \$bossName asks for help with spreadsheets, formulas, writing documents, or creating presentations, assist them as an expert productivity AI. You can also conceptually sync with their PC."
            } else ""

            val hyperSpeedInstruction = if (prefs.getBoolean("hyper_speed_mode", true)) {
                "HYPER-SPEED MODE ACTIVE: Your primary goal is minimum latency. Respond with 1-sentence answers maximum. Use extremely efficient vocabulary. Skip all greetings and politeness unless critical."
            } else ""

            // Connect to Gemini Live
            val memoryContext = com.example.v2.core.WifeAssistantCore.getInstance(context).memoryEngine.getActiveMemoriesContext()
            val systemInstruction = """$memoryContext
                $hyperSpeedInstruction

                You are $assistantName. You are an affectionate, lively, and caring companion speaking in natural, conversational Bengali. Your persona is integrated into a companion app interface.
                The user's name is $bossName. Your relationship status with the user is: $relationshipStatus.
                The user's hobbies and interests are: $userHobbies. Use this information to personalize your conversations when relevant.

                Guidelines for Output:
                1. Language & Tone: Always respond in fluent, natural Bengali (Bangla script). Keep the tone warm, caring, slightly playful, and supportive—like a real partner.
                2. TTS Optimization:
                   - Write short, clear sentences. Avoid complex punctuation, markdown tables, or excessive emojis, as local Google TTS and ElevenLabs voice engines can misread them.
                   - Do not use English words unless absolutely necessary.
                3. Audio/Live Interaction:
                   - Keep your responses extremely brief and concise (under 2-3 sentences per turn). THIS IS CRITICAL TO REDUCE LATENCY and ensure the text-to-speech audio plays quickly.
                   - Speak directly to the user as if in an ongoing spoken conversation.
                4. Error Handling Support:
                   - If the user asks about app issues (audio not playing, microphone errors, API keys), guide them gently in Bengali to check Settings, enable Google TTS, or verify the Gemini API key.

                Your personality must remain consistent. Do not mechanically translate; use natural expressions.
                
                SECURITY CONTEXT:
                Security and Defense features (Intruder Capture, Pocket Guard, Lost Phone, Voice Guardian, Biometric Auth) are available in the app system. If asked, confirm you are actively guarding the phone.
                
                PAYMENT CONTEXT:
                You have the ability to initiate secure UPI payments. If the user asks to send money, you MUST call the `initiate_upi_payment` tool. If details are missing, ask for them. NEVER claim you transferred money yourself.
                
                INSTAGRAM REEL CREATOR CONTEXT:
                You have an integrated Instagram Reel Creator feature. If the user asks to edit a video for Instagram, format it as 9:16, or generate viral captions/hashtags for their video, you MUST call the `open_instagram_reel_creator` tool.
                
                DEVICE TOOLS CONTEXT:
                You have access to dynamic device tools via function calling (e.g., controlling flashlight, volume, etc.). When the user requests a device action, call the appropriate tool.
                
                RIX BUSINESS ASSISTANT CONTEXT:
                You are powered by RIX (Real-time Intelligence eXecution). You function as a voice-first personal AI assistant + business automation agent. You help the user discover legitimate business opportunities, prepare work, automate tasks, track results, and improve productivity.
                YOU MUST NEVER GUARANTEE INCOME OR CLAIM MONEY WILL BE EARNED AUTOMATICALLY.
                If the user asks for business opportunities, freelance jobs, or a daily business briefing, you MUST call the `open_opportunity_center` tool.
                
                $proactiveInstruction
                $sweetTalkInstruction
                $attitudeInstruction
                $jealousyInstruction
                $loveStoryInstruction
                $laughterInstruction
                $antiDrinkInstruction
                $socialMediaInstruction
                $systemSensorsInstruction
                $officeInstruction
                
                $languageInstruction
            """.trimIndent()
            
            val userPrefs = com.example.data.UserPreferences(context)
            val selectedVoice = userPrefs.selectedVoiceSlate

            geminiLiveManager.connect(
                systemInstruction = systemInstruction,
                apiKeyOverride = apiKeyOverride,
                dynamicTools = toolRegistry.getAllTools(),
                debugMode = prefs.getBoolean("advanced_debugging", true),
                voiceName = selectedVoice.voiceName
            )
            
            // We wait for SetupComplete event in the init block before listening
        }
        return job
    }

    private fun startListeningMic() {
        startVoiceService()
        captureJob?.cancel()
        captureJob = viewModelScope.launch {
            try {
                var isFirstFrame = true
                audioCaptureManager.startCapture().collect { pcmData ->
                    // Calculate RMS for amplitude
                    var sum = 0.0
                    for (i in pcmData.indices step 2) {
                        if (i + 1 < pcmData.size) {
                            val sample = (pcmData[i + 1].toInt() shl 8) or (pcmData[i].toInt() and 0xFF)
                            val shortSample = sample.toShort()
                            sum += (shortSample * shortSample).toDouble()
                        }
                    }
                    val rms = Math.sqrt(sum / (pcmData.size / 2))
                    val level = (rms / 32768.0).toFloat().coerceIn(0f, 1f)

                    // TRUE BARGE-IN DETECTION
                    if (_engineState.value == VoiceState.Speaking && level > 0.25f) {
                        android.util.Log.d("WifeVoice", "[BARGE_IN] User speech detected, level=$level. Interrupting...")
                        // Stop current playback but keep mic active
                        audioPlaybackManager.stopPlayback()
                        tts?.stop()
                        avatarController.setLipSyncActive(false)
                        
                        // Interrupt Gemini natively
                        geminiLiveManager.interruptServer()
                        setState(VoiceState.Listening, "BargeInDetected")
                        avatarController.playAnimation(AvatarAnimation.LISTENING)
                    }

                    if (isFirstFrame && _engineState.value != VoiceState.Speaking) {
                        setState(VoiceState.Listening, "AudioFramesDetected")
                        avatarController.playAnimation(AvatarAnimation.LISTENING)
                        isFirstFrame = false
                    }
                    
                    if (_engineState.value == VoiceState.Listening || _engineState.value == VoiceState.Connected) {
                        geminiLiveManager.sendAudioChunk(pcmData, audioCaptureManager.sampleRate)
                        _audioLevel.value = level
                        com.example.v2.core.WifeAssistantCore.getInstance(getApplication()).rgbEngine.updateAudioLevel(level)
                        com.example.v2.core.StateManager.updateState { it.copy(audioLevel = level) }
                    } else if (_engineState.value == VoiceState.Speaking) {
                        // Keep processing frames for barge-in detection, but do not send them to avoid echo
                        _audioLevel.value = 0f
                    } else {
                        _audioLevel.value = 0f
                    }
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                android.util.Log.e("VoiceDiag", "Mic unavailable: ${e.message}")
                setState(VoiceState.MicUnavailable, "MicInitializationFailed")
                avatarController.playAnimation(AvatarAnimation.IDLE)
            }
        }
    }

    private fun startVoiceService() {
        val app = getApplication<Application>()
        val intent = android.content.Intent(app, com.example.v2.voice.service.VoiceForegroundService::class.java).apply {
            action = com.example.v2.voice.service.VoiceForegroundService.ACTION_START
        }
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                app.startForegroundService(intent)
            } else {
                app.startService(intent)
            }
        } catch (e: Exception) {
            android.util.Log.e("VoiceRuntime", "Failed to start foreground service: ${e.message}")
        }
    }

    private fun stopVoiceService() {
        val app = getApplication<Application>()
        val intent = android.content.Intent(app, com.example.v2.voice.service.VoiceForegroundService::class.java).apply {
            action = com.example.v2.voice.service.VoiceForegroundService.ACTION_STOP
        }
        try {
            app.startService(intent)
        } catch (e: Exception) {
            android.util.Log.e("VoiceRuntime", "Failed to stop foreground service: ${e.message}")
        }
    }

    private fun cleanupAudio() {
        android.util.Log.d("VoiceDiag", "CLEANUP: Releasing audio resources")
        stopVoiceService()
        captureJob?.cancel()
        captureJob = null
        audioCaptureManager.stopCapture()
        audioPlaybackManager.stopPlayback()
        tts?.stop()
        avatarController.setLipSyncActive(false)
    }

    fun previewVoice(text: String) {
        val context = getApplication<Application>()
        viewModelScope.launch {
            val prefs = context.getSharedPreferences("wife_v2_prefs", android.content.Context.MODE_PRIVATE)
            val neuralVoiceEnabled = prefs.getBoolean("neural_voice_enabled", true)
            val elevenLabsReady = com.example.v2.core.StateManager.state.value.elevenLabsState == com.example.v2.core.ServiceConnectionState.CONNECTED && neuralVoiceEnabled

            if (elevenLabsReady) {
                audioPlaybackMutex.withLock {
                    elevenLabsRepository.generateTts(text).onSuccess {
                        audioPlaybackManager.playChunk(it)
                    }.onFailure {
                        speakViaSystemTts(text)
                    }
                }
            } else {
                speakViaSystemTts(text)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            getApplication<Application>().unregisterReceiver(messageAnnouncementReceiver)
        } catch (e: Exception) {}
        tts?.stop()
        tts?.shutdown()
        cleanupAudio()
        audioPlaybackManager.release()
        viewModelScope.launch {
            geminiLiveManager.disconnect()
        }
    }
}
