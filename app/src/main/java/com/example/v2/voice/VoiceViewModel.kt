package com.example.v2.voice

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.v2.ai.GeminiLiveManager
import com.example.v2.audio.AudioCaptureManager
import com.example.v2.audio.AudioPlaybackManager
import com.example.v2.audio.VoiceActivityManager
import com.example.v2.avatar.AvatarAnimation
import com.example.v2.avatar.AvatarController
import com.example.v2.avatar.AvatarState
import com.example.v2.avatar.AvatarViewModel
import com.example.v2.language.LanguageManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

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

data class GeminiMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class VoiceViewModel(application: Application) : AndroidViewModel(application) {
    
    private val core = com.example.v2.core.MayaAssistantCore.getInstance(application)
    private val userPreferences = com.example.data.UserPreferences(application)
    val toolRegistry = core.toolRegistry
    private val toolExecutionEngine = core.toolEngine
    private var tts: android.speech.tts.TextToSpeech? = null
    private val audioPlaybackMutex = kotlinx.coroutines.sync.Mutex()
    private val geminiLiveManager = core.geminiLiveManager
    private val secureStorage = core.secureStorage
    private val elevenLabsRepository = core.elevenLabsRepository
    private val voiceActivityManager = VoiceActivityManager()
    val avatarViewModel = AvatarViewModel()

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
    
    private val _audioLevel = MutableStateFlow(0f)
    val audioLevel: StateFlow<Float> = _audioLevel.asStateFlow()

    val currentExpression: StateFlow<String> = avatarViewModel.expression

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

    private val _messages = MutableStateFlow<List<GeminiMessage>>(emptyList())
    val messages: StateFlow<List<GeminiMessage>> = _messages.asStateFlow()

    private var isFirstAiTextInTurn = true

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
    private var lastSpeechTimestamp = 0L

    private fun setState(newState: VoiceState, reason: String) {
        val oldState = _engineState.value
        if (oldState != newState) {
            val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", java.util.Locale.getDefault()).format(java.util.Date())
            android.util.Log.i("MayaVoice", "[TRANSITION] $timestamp | ${oldState.javaClass.simpleName} -> ${newState.javaClass.simpleName} | reason=$reason")
            _engineState.value = newState
            
            val context = getApplication<Application>()
            val hasMic = androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED
            
            core.rgbEngine.setVoiceReactiveMode(newState is VoiceState.Speaking)
            
            val sessionState = when (newState) {
                is VoiceState.Connecting -> com.example.v2.core.VoiceSessionState.CONNECTING
                is VoiceState.Connected -> {
                    avatarViewModel.setState(AvatarState.IDLE)
                    com.example.v2.core.VoiceSessionState.CONNECTED
                }
                is VoiceState.Listening -> {
                    avatarViewModel.setState(AvatarState.LISTENING)
                    com.example.v2.core.VoiceSessionState.LISTENING
                }
                is VoiceState.Thinking -> {
                    avatarViewModel.setState(AvatarState.THINKING)
                    com.example.v2.core.VoiceSessionState.THINKING
                }
                is VoiceState.Speaking -> {
                    avatarViewModel.setState(AvatarState.SPEAKING)
                    com.example.v2.core.VoiceSessionState.SPEAKING
                }
                is VoiceState.UserInterrupted -> {
                    avatarViewModel.setState(AvatarState.INTERRUPTED)
                    com.example.v2.core.VoiceSessionState.LISTENING
                }
                is VoiceState.Disconnected -> {
                    avatarViewModel.setState(AvatarState.IDLE)
                    com.example.v2.core.VoiceSessionState.DISCONNECTED
                }
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

            val prefs = getApplication<Application>().getSharedPreferences("maya_v2_prefs", android.content.Context.MODE_PRIVATE)
            val neuralVoiceEnabled = prefs.getBoolean("neural_voice_enabled", true)
            val elevenLabsReady = com.example.v2.core.StateManager.state.value.elevenLabsState == com.example.v2.core.ServiceConnectionState.CONNECTED && neuralVoiceEnabled

            if (elevenLabsReady) {
                audioPlaybackMutex.withLock {
                    isCurrentlySpeakingFromTts = true
                    audioPlaybackManager.stopPlayback()
                    setState(VoiceState.Speaking, "NeuralAnnouncement")
                    avatarController.setLipSyncActive(true)
                    
                    com.example.v2.core.MayaAssistantCore.getInstance(getApplication()).elevenLabsRepository.generateTts(text)
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
            android.util.Log.w("MayaVoice", "Could not set specific voice: ${e.message}")
        }
        
        android.util.Log.i("MayaVoice", "[TTS] System TTS speaking: $text")
        val result = tts?.speak(text, android.speech.tts.TextToSpeech.QUEUE_FLUSH, params, "gemini_tts")
        if (result == android.speech.tts.TextToSpeech.ERROR) {
            android.util.Log.e("MayaVoice", "[TTS] System TTS execution failed")
            isCurrentlySpeakingFromTts = false
        } else {
            // Safety timeout to reset isCurrentlySpeakingFromTts if onDone is not called
            viewModelScope.launch {
                kotlinx.coroutines.delay(15000)
                if (isCurrentlySpeakingFromTts) {
                    isCurrentlySpeakingFromTts = false
                    android.util.Log.w("MayaVoice", "[TTS] Safety timeout reached, resetting speak state")
                }
            }
        }
    }

    private val messageAnnouncementReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: android.content.Context?, intent: android.content.Intent?) {
            if (intent?.action == "com.example.v2.ANNOUNCE_MESSAGE") {
                val prefs = getApplication<Application>().getSharedPreferences("maya_v2_prefs", android.content.Context.MODE_PRIVATE)
                val autoReplyEnabled = prefs.getBoolean("auto_reply_enabled", false) // Default to false (remove auto reply)
                if (!autoReplyEnabled) return

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
                // Try to set Bengali as default if available
                val localeBN = java.util.Locale("bn", "BD")
                val result = tts?.setLanguage(localeBN)
                if (result == android.speech.tts.TextToSpeech.LANG_MISSING_DATA || result == android.speech.tts.TextToSpeech.LANG_NOT_SUPPORTED) {
                    android.util.Log.w("MayaVoice", "[TTS] Bengali not supported, falling back to system default")
                    tts?.language = java.util.Locale.getDefault()
                } else {
                    android.util.Log.i("MayaVoice", "[TTS] Bengali language set successfully")
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
                        android.util.Log.e("MayaVoice", "[TTS] Error in utterance: $utteranceId")
                    }
                })
            }
        }

        toolRegistry.register(FlashlightTool(application))
        toolRegistry.register(VolumeTool(application))
        toolRegistry.register(com.example.v2.core.tools.impl.MemorySaveTool(application))
        toolRegistry.register(com.example.v2.core.tools.impl.PcCommandTool(com.example.v2.core.MayaAssistantCore.getInstance(application)))
        toolRegistry.register(com.example.v2.core.tools.impl.AutomationStartTool(com.example.v2.core.MayaAssistantCore.getInstance(application)))
        
        // Listen for Gemini Connection State
        viewModelScope.launch {
            geminiLiveManager.connectionState.collect { geminiState ->
                android.util.Log.d("MayaVoice", "[OBSERVER] Gemini Connection State: $geminiState")
                when (geminiState) {
                    com.example.v2.core.GeminiConnectionState.CONNECTING, com.example.v2.core.GeminiConnectionState.RECONNECTING -> {
                        setState(VoiceState.Connecting, "GeminiConnecting")
                    }
                    com.example.v2.core.GeminiConnectionState.CONNECTED -> {
                        if (_engineState.value is VoiceState.Connecting) {
                             setState(VoiceState.Connected, "GeminiConnected")
                        }
                    }
                    com.example.v2.core.GeminiConnectionState.DISCONNECTED -> {
                        if (_engineState.value != VoiceState.Disconnected && _engineState.value !is VoiceState.MicPermissionRequired) {
                            cleanupAudio()
                            setState(VoiceState.Disconnected, "GeminiDisconnected")
                        }
                    }
                    com.example.v2.core.GeminiConnectionState.FAILED -> {
                        val state = com.example.v2.core.StateManager.state.value
                        val errorMsg = state.lastError ?: "Gemini Connection Failed"
                        cleanupAudio()
                        setState(VoiceState.Error(errorMsg), "GeminiConnectionFailed")
                    }
                    else -> {}
                }
            }
        }

        // Listen for Setup Complete
        viewModelScope.launch {
            geminiLiveManager.setupCompleteFlow.collect {
                if (_engineState.value == VoiceState.Connecting) {
                    setState(VoiceState.Connected, "SetupComplete")
                    reconnectAttempts = 0
                    avatarController.playAnimation(AvatarAnimation.IDLE)
                    
                    // Auto-start listening on successful connection
                    startListeningMic()
                }
            }
        }

        // Global Voice Toggle is now handled by VoiceAssistantManager
        avatarController.loadAvatar("models/wife_avatar.glb")
        
        // Listen for AI Audio
        viewModelScope.launch {
            geminiLiveManager.audioFlow.collect { pcmData ->
                audioPlaybackMutex.withLock {
                    val prefs = getApplication<Application>().getSharedPreferences("maya_v2_prefs", android.content.Context.MODE_PRIVATE)
                    val neuralVoiceEnabled = prefs.getBoolean("neural_voice_enabled", true)
                    val elevenLabsReady = com.example.v2.core.StateManager.state.value.elevenLabsState == com.example.v2.core.ServiceConnectionState.CONNECTED && neuralVoiceEnabled
                    
                    if (elevenLabsReady) return@withLock
                    
                    // Update level for reactive UI
                    var sum = 0.0
                    for (i in pcmData.indices step 2) {
                        if (i + 1 < pcmData.size) {
                            val sample = (pcmData[i + 1].toInt() shl 8) or (pcmData[i].toInt() and 0xFF)
                            sum += (sample.toShort() * sample.toShort()).toDouble()
                        }
                    }
                    val rms = Math.sqrt(sum / (pcmData.size / 2))
                    val level = (rms / 32768.0).toFloat().coerceIn(0f, 1f)
                    _audioLevel.value = level
                    com.example.v2.core.MayaAssistantCore.getInstance(getApplication()).rgbEngine.updateAudioLevel(level)
                    
                    // Expression update while speaking
                    if (level > 0.1f) {
                        avatarViewModel.setExpression(if (level > 0.5f) "excited" else "happy")
                    } else {
                        avatarViewModel.setExpression("smiling")
                    }
                    
                    audioPlaybackManager.playChunk(pcmData)
                }
            }
        }

        // Listen for AI Text (Fallback for unsupported languages or High-Fidelity ElevenLabs)
        viewModelScope.launch {
            geminiLiveManager.textFlow.collect { text ->
                if (text.isNotBlank()) {
                    handleAiText(text)
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

                    val prefs = getApplication<Application>().getSharedPreferences("maya_v2_prefs", android.content.Context.MODE_PRIVATE)
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
                        
                        android.util.Log.i("MayaVoice", "[TTS] System TTS switching to ${locale.displayName}")
                        tts?.language = locale
                        
                        android.util.Log.i("MayaVoice", "[TTS] System TTS speaking ($detectedLang): ${text.take(30)}...")
                        val result = tts?.speak(text, android.speech.tts.TextToSpeech.QUEUE_FLUSH, params, "gemini_tts")
                        if (result == android.speech.tts.TextToSpeech.ERROR) {
                            android.util.Log.e("MayaVoice", "[TTS] System TTS failed to speak")
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
                isFirstAiTextInTurn = true
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
                
                // Show Toast for the user
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    android.widget.Toast.makeText(getApplication(), "Error: $errorMsg", android.widget.Toast.LENGTH_LONG).show()
                }
                
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
        
        android.util.Log.i("MayaVoice", "[VOICE_BUTTON] Tapped. Permission: $hasMicPermission")
        
        if (!hasMicPermission) {
            android.util.Log.i("MayaVoice", "[PERMISSION] Requesting Record Audio")
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
            android.util.Log.w("MayaVoice", "[VOICE_BUTTON] Microphone busy in call.")
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
            android.util.Log.w("MayaVoice", "[VOICE_BUTTON] API Key missing. Showing error state.")
            android.widget.Toast.makeText(context, "Please set Gemini API Key in Settings", android.widget.Toast.LENGTH_SHORT).show()
            setState(VoiceState.NotConfigured, "ApiKeyMissing")
            return
        }

        when (val currentState = _engineState.value) {
            is VoiceState.Connecting -> {
                android.util.Log.i("MayaVoice", "[VOICE_BUTTON] Connection in progress. Tapping again disconnects.")
                core.voiceAssistantManager.disconnect()
                cleanupAudio()
                setState(VoiceState.Disconnected, "UserCancelledDuringConnection")
            }
            is VoiceState.Connected, is VoiceState.Thinking -> {
                android.util.Log.i("MayaVoice", "[VOICE_BUTTON] Connected but idle. Starting mic...")
                startListeningMic()
            }
            is VoiceState.Listening -> {
                android.util.Log.i("MayaVoice", "[VOICE_BUTTON] Listening. Muting mic but keeping session alive.")
                stopListeningMicOnly()
                setState(VoiceState.Connected, "MicMutedByUser")
            }
            is VoiceState.Speaking -> {
                android.util.Log.i("MayaVoice", "[VOICE_BUTTON] Speaking. Interrupting and listening...")
                viewModelScope.launch {
                    isCurrentlySpeakingFromTts = false
                    audioPlaybackManager.stopPlayback()
                    tts?.stop()
                    geminiLiveManager.interruptServer()
                    startListeningMic()
                }
            }
            else -> {
                android.util.Log.i("MayaVoice", "[VOICE_BUTTON] Inactive. Starting connection via Manager...")
                setState(VoiceState.Connecting, "UserStartedConnection")
                android.widget.Toast.makeText(context, "Connecting to Gemini...", android.widget.Toast.LENGTH_SHORT).show()
                core.voiceAssistantManager.connect()
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
                    android.util.Log.i("MayaVoice", "[TEST] Gemini REST test success")
                } else {
                    android.util.Log.e("MayaVoice", "[TEST] Gemini REST test failed: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                android.util.Log.e("MayaVoice", "[TEST] Gemini REST test exception: ${e.message}")
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
            core.voiceAssistantManager.connect()
            geminiLiveManager.setupCompleteFlow.first()
            // Send a client content message based on action
            setState(VoiceState.Thinking, "ActionTriggered")
            geminiLiveManager.sendClientContentMessage("The user triggered the action: \$actionName")
        }
    }

    fun sendTextCommand(text: String, context: android.content.Context) {
        viewModelScope.launch {
            try {
                if (_engineState.value == VoiceState.Disconnected || _engineState.value is VoiceState.Error) {
                    core.voiceAssistantManager.connect()
                    // Wait for connection with timeout
                    withTimeoutOrNull(15000) {
                        geminiLiveManager.setupCompleteFlow.first()
                    } ?: run {
                        android.util.Log.e("MayaVoice", "Connection timeout during text command")
                        return@launch
                    }
                }
                isFirstAiTextInTurn = true
                addMessage(text, isFromUser = true)
                setState(VoiceState.Thinking, "TextCommandSent")
                geminiLiveManager.sendClientContentMessage(text)
            } catch (e: Exception) {
                android.util.Log.e("MayaVoice", "Error sending text command: ${e.message}")
            }
        }
    }

    private fun handleAiText(text: String) {
        val currentMessages = _messages.value.toMutableList()
        if (!isFirstAiTextInTurn && currentMessages.isNotEmpty() && !currentMessages.last().isFromUser) {
            val lastMsg = currentMessages.last()
            currentMessages[currentMessages.size - 1] = lastMsg.copy(text = lastMsg.text + text)
            _messages.value = currentMessages
        } else {
            val newMessage = GeminiMessage(text = text, isFromUser = false)
            _messages.value = _messages.value + newMessage
            isFirstAiTextInTurn = false
        }
    }

    private fun addMessage(text: String, isFromUser: Boolean) {
        val newMessage = GeminiMessage(text = text, isFromUser = isFromUser)
        _messages.value = _messages.value + newMessage
    }

    fun clearMessages() {
        _messages.value = emptyList()
    }

    fun triggerFirstGreeting(context: android.content.Context) {
        val prefs = context.getSharedPreferences("maya_v2_prefs", android.content.Context.MODE_PRIVATE)
        val firstGreetingEnabled = prefs.getBoolean("first_greeting_engine", true)
        
        // Don't trigger if not configured
        val apiKey = secureStorage.getApiKey()
        if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
            setState(VoiceState.NotConfigured, "InitialCheckApiKeyMissing")
            return
        }

        if (firstGreetingEnabled && (_engineState.value == VoiceState.Disconnected || _engineState.value is VoiceState.Error)) {
            viewModelScope.launch {
                try {
                    core.voiceAssistantManager.connect()
                    // Wait for connection with timeout
                    withTimeoutOrNull(20000) {
                        geminiLiveManager.setupCompleteFlow.first()
                    } ?: return@launch
                    
                    setState(VoiceState.Thinking, "FirstGreetingTriggered")
                    geminiLiveManager.sendClientContentMessage("SYSTEM TRIGGER (FIRST GREETING ENGINE): The user just opened the app. Give them a very cute, warm, and romantic first greeting based on the current time of day. Be expressive, detailed, and loving. Do not wait for them to speak first.")
                } catch (e: Exception) {
                    android.util.Log.e("MayaVoice", "First greeting failed: ${e.message}")
                }
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

    private fun startListeningMic() {
        startVoiceService()
        voiceActivityManager.setThreshold(userPreferences.vadSensitivity)
        captureJob?.cancel()
        captureJob = viewModelScope.launch {
            try {
                var isFirstFrame = true
                val preRollBuffer = java.util.ArrayDeque<ByteArray>(3) // Store last 3 chunks for context
                var isCurrentlyStreaming = false
                
                audioCaptureManager.startCapture().collect { pcmData ->
                    val isSpeech = voiceActivityManager.isSpeechDetected(pcmData)
                    
                    // Calculate level for UI feedback regardless of VAD
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

                    // 1. IS WIFE CURRENTLY SPEAKING?
                    val isMayaSpeaking = _engineState.value == VoiceState.Speaking || isCurrentlySpeakingFromTts
                    
                    if (isMayaSpeaking) {
                        if (level > 0.25f && isSpeech) {
                            android.util.Log.i("MayaVoice", "[BARGE-IN] User interrupted Maya!")
                            isCurrentlySpeakingFromTts = false
                            audioPlaybackManager.stopPlayback()
                            tts?.stop()
                            avatarController.setLipSyncActive(false)
                            
                            geminiLiveManager.interruptServer()
                            setState(VoiceState.UserInterrupted, "BargeInDetected")
                            
                            // Re-start listening cycle for the new turn
                            setState(VoiceState.Listening, "InterruptionToListen")
                            geminiLiveManager.sendAudioChunk(pcmData, audioCaptureManager.sampleRate)
                            isCurrentlyStreaming = true
                            lastSpeechTimestamp = System.currentTimeMillis()
                            return@collect
                        } else {
                            return@collect 
                        }
                    }

                    // 5. NO (Not speaking OR just interrupted): SEND PCM TO LIVE
                    if (isFirstFrame && !isMayaSpeaking) {
                        setState(VoiceState.Listening, "AudioFramesDetected")
                        avatarController.playAnimation(AvatarAnimation.LISTENING)
                        avatarViewModel.setState(AvatarState.LISTENING)
                        isFirstFrame = false
                    }
                    
                    if (_engineState.value == VoiceState.Listening || _engineState.value == VoiceState.Connected || _engineState.value == VoiceState.Thinking) {
                        if (isSpeech || !userPreferences.vadEnabled) {
                            if (!isCurrentlyStreaming) {
                                isCurrentlyStreaming = true
                                android.util.Log.d("MayaVoice", "[MIC] Sending PCM to Live WebSocket.")
                                setState(VoiceState.Listening, "UserStartedSpeaking")
                                avatarViewModel.setExpression("listening")
                            }
                            geminiLiveManager.sendAudioChunk(pcmData, audioCaptureManager.sampleRate)
                        } else if (isCurrentlyStreaming) {
                            isCurrentlyStreaming = false
                            android.util.Log.d("MayaVoice", "[MIC] User stopped speaking.")
                            setState(VoiceState.Thinking, "UserStoppedSpeaking")
                            avatarViewModel.setExpression("neutral")
                        }
                        
                        _audioLevel.value = level
                        com.example.v2.core.StateManager.updateState { 
                            it.copy(audioLevel = level, micState = com.example.v2.core.MicrophoneState.RECORDING) 
                        }
                    } else {
                        _audioLevel.value = 0f
                        com.example.v2.core.StateManager.updateState { 
                            it.copy(micState = com.example.v2.core.MicrophoneState.READY) 
                        }
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

    private fun stopListeningMicOnly() {
        captureJob?.cancel()
        captureJob = null
        audioCaptureManager.stopCapture()
        _audioLevel.value = 0f
    }

    private fun cleanupAudio() {
        android.util.Log.d("VoiceDiag", "CLEANUP: Releasing audio resources")
        stopVoiceService()
        stopListeningMicOnly()
        audioPlaybackManager.stopPlayback()
        tts?.stop()
        avatarController.setLipSyncActive(false)
    }

    fun previewVoice(text: String) {
        val context = getApplication<Application>()
        viewModelScope.launch {
            val prefs = context.getSharedPreferences("maya_v2_prefs", android.content.Context.MODE_PRIVATE)
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
