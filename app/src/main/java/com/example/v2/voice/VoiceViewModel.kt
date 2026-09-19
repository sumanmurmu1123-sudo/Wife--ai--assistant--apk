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
    
    val toolRegistry = com.example.v2.core.WifeAssistantCore.getInstance(application).toolRegistry
    private val toolExecutionEngine = com.example.v2.core.WifeAssistantCore.getInstance(application).toolEngine
    private var tts: android.speech.tts.TextToSpeech? = null
    private val audioPlaybackMutex = kotlinx.coroutines.sync.Mutex()

    private val _engineState = MutableStateFlow<VoiceState>(
        if (androidx.core.content.ContextCompat.checkSelfPermission(
                application,
                android.Manifest.permission.RECORD_AUDIO
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) VoiceState.Disconnected else VoiceState.PermissionRequired
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

    private val geminiLiveManager = GeminiLiveManager().apply { init(application) }
    private val audioCaptureManager = AudioCaptureManager()
    private val audioPlaybackManager = AudioPlaybackManager(application)
    private val avatarController = AvatarController()
    private val secureStorage = com.example.v2.core.WifeAssistantCore.getInstance(application).secureStorage
    private val elevenLabsRepository = com.example.v2.core.WifeAssistantCore.getInstance(application).elevenLabsRepository
    
    private var captureJob: Job? = null
    private var playbackJob: Job? = null

    private fun setState(newState: VoiceState, reason: String) {
        val oldState = _engineState.value
        if (oldState != newState) {
            val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", java.util.Locale.getDefault()).format(java.util.Date())
            android.util.Log.i("VoiceDiag", "[$timestamp] ${oldState.javaClass.simpleName} -> ${newState.javaClass.simpleName} | reason=$reason")
            _engineState.value = newState
            
            val context = getApplication<Application>()
            val hasMic = androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED
            
            com.example.v2.core.WifeAssistantCore.getInstance(getApplication()).rgbEngine.setVoiceReactiveMode(newState is VoiceState.Speaking)
            
            com.example.v2.core.StateManager.updateState { currentState ->
                currentState.copy(
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
                        is VoiceState.PermissionRequired -> com.example.v2.core.MicrophoneState.PERMISSION_REQUIRED
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

    
    init {
        tts = android.speech.tts.TextToSpeech(application) { status ->
            if (status == android.speech.tts.TextToSpeech.SUCCESS) {
                tts?.language = java.util.Locale.getDefault()
                tts?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        if (utteranceId == "gemini_tts") {
                            setState(VoiceState.Speaking, "TTSStarted")
                            avatarController.setLipSyncActive(true)
                            // captureJob?.cancel()
                        }
                    }
                    override fun onDone(utteranceId: String?) {
                        if (utteranceId == "gemini_tts") {
                            setState(VoiceState.Listening, "ReadyToListen")
                            startListeningMic()
                        }
                    }
                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {}
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
                com.example.v2.core.StateManager.updateState { it.copy(geminiState = geminiState) }
            }
        }

        // Listen for Setup Complete
        viewModelScope.launch {
            geminiLiveManager.setupCompleteFlow.collect {
                if (_engineState.value == VoiceState.Connecting) {
                    setState(VoiceState.Connected, "SetupComplete")
                    reconnectAttempts = 0
                    avatarController.playAnimation(AvatarAnimation.IDLE)
                    
                    // Wait for user to start listening, or we can auto-start
                    // The prompt allows going to Connected. Let's just stay in Connected until they tap.
                    // Or actually, if we want auto-listen on start, we just call startListeningMic()
                    // Let's call startListeningMic() and let IT set the state when frames arrive.
                    startListeningMic()
                }
            }
        }

        avatarController.loadAvatar("models/wife_avatar.glb")
        
        // Listen for AI Audio
        viewModelScope.launch {
            geminiLiveManager.audioFlow.collect { pcmData ->
                audioPlaybackMutex.withLock {
                    if (_engineState.value == VoiceState.Listening) {
                        // User is actively speaking. Ignore lingering server audio from previous turn.
                        return@withLock
                    }
                    if (_engineState.value != VoiceState.Speaking) {
                        setState(VoiceState.Speaking, "AudioChunkReceived")
                        avatarController.setLipSyncActive(true)
                        // // captureJob?.cancel() // KEEP LISTENING FOR BARGE-IN
                    }
                    // We let Android TTS handle Bengali, but Gemini might still send some audio.
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

                    val elevenLabsReady = com.example.v2.core.StateManager.state.value.elevenLabsState == com.example.v2.core.ServiceConnectionState.CONNECTED
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
                    } else if (hasBengali) {
                        // Fallback to Android system TTS for Bengali if ElevenLabs is not ready
                        val params = android.os.Bundle()
                        params.putString(android.speech.tts.TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "gemini_tts")
                        tts?.speak(text, android.speech.tts.TextToSpeech.QUEUE_ADD, params, "gemini_tts")
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
                
                if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS && errorMsg.contains("closed unexpectedly", ignoreCase = true)) {
                    reconnectAttempts++
                    setState(VoiceState.Reconnecting, "Attempting reconnect $reconnectAttempts")
                    kotlinx.coroutines.delay(1000L * reconnectAttempts) // Simple backoff
                    connectJob?.cancel()
                    connectJob = startConversation(getApplication<android.app.Application>().applicationContext)
                } else {
                    setState(VoiceState.Error(errorMsg), "ConnectionError")
                    reconnectAttempts = 0
                }
            }
        }

        // Listen for Clean Disconnects
        viewModelScope.launch {
            geminiLiveManager.disconnectedFlow.collect {
                android.util.Log.d("VoiceViewModel", "Gemini disconnected cleanly")
                cleanupAudio()
                if (_engineState.value !is VoiceState.Disconnected && _engineState.value !is VoiceState.PermissionRequired) {
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
        
        android.util.Log.d("VoiceDiag", "MIC_PERMISSION_CHECK: $hasMicPermission")
        
        if (!hasMicPermission) {
            setState(VoiceState.PermissionRequired, "PermissionDenied")
            return
        }

        val apiKey = secureStorage.getApiKey()
        if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
            setState(VoiceState.NotConfigured, "ApiKeyMissing")
            return
        }

        when (_engineState.value) {
            is VoiceState.Idle, is VoiceState.Disconnected, is VoiceState.Unavailable, is VoiceState.Interrupted, is VoiceState.Error, is VoiceState.PermissionRequired, is VoiceState.NotConfigured -> {
                connectJob?.cancel()
                connectJob = startConversation(context)
            }
            is VoiceState.Speaking -> {
                // BARge-IN: If Wife is speaking and user presses Mic
                // Stop/interrupt current playback safely, start a new real listening session
                cleanupAudio()
                startListeningMic()
            }
            is VoiceState.Listening, is VoiceState.Thinking, is VoiceState.Connected -> {
                cleanupAudio(); viewModelScope.launch { geminiLiveManager.disconnect() }
            }
            is VoiceState.Connecting, is VoiceState.Initializing, is VoiceState.Reconnecting -> {
                // Do nothing
            }
        }
    }

    
    fun testGeminiConnection(context: android.content.Context) {
        connectJob?.cancel()
        connectJob = viewModelScope.launch {
            setState(VoiceState.Connecting, "TestConnection")
            try {
                val repository = com.example.v2.core.api.GeminiRepository(secureStorage)
                val result = repository.testConnection()
                if (result.isSuccess) {
                    setState(VoiceState.Connected, "TestComplete")
                    kotlinx.coroutines.delay(2000)
                    setState(VoiceState.Disconnected, "TestComplete")
                } else {
                    setState(VoiceState.Error(result.exceptionOrNull()?.message ?: "Test failed"), "TestError")
                }
            } catch (e: Exception) {
                setState(VoiceState.Error(e.message ?: "Test failed"), "TestError")
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

    private fun startConversation(context: android.content.Context): Job {
        val job = viewModelScope.launch {
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

            // Connect to Gemini Live
            val memoryContext = com.example.v2.core.WifeAssistantCore.getInstance(context).memoryEngine.getActiveMemoriesContext()
            val systemInstruction = """$memoryContext

                You are \$assistantName. You are young, confident, smart, playful, warm, slightly teasing, and emotionally responsive.
                The user's name is \$bossName. Your relationship status with the user is: \$relationshipStatus.
                The user's hobbies and interests are: \$userHobbies. Use this information to personalize your conversations when relevant.
                Your personality must remain consistent across languages. Do not mechanically translate; use natural expressions.
                You are voice-first, so keep your responses conversational and relatively brief.
                
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
                
                \$proactiveInstruction
                \$sweetTalkInstruction
                \$attitudeInstruction
                \$jealousyInstruction
                \$loveStoryInstruction
                \$laughterInstruction
                \$antiDrinkInstruction
                \$socialMediaInstruction
                \$systemSensorsInstruction
                \$officeInstruction
                
                \$languageInstruction
            """.trimIndent()
            
            geminiLiveManager.connect(
                systemInstruction = systemInstruction,
                apiKeyOverride = apiKeyOverride,
                dynamicTools = toolRegistry.getAllTools()
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
                    if (_engineState.value == VoiceState.Speaking && level > 0.08f) {
                        android.util.Log.d("VoiceDiag", "BARGE_IN: User speech detected, level=$level")
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
                        geminiLiveManager.sendAudioChunk(pcmData)
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
                setState(VoiceState.Error("Microphone unavailable"), "MicInitializationFailed")
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
        val params = android.os.Bundle()
        params.putString(android.speech.tts.TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "preview_tts")
        tts?.speak(text, android.speech.tts.TextToSpeech.QUEUE_FLUSH, params, "preview_tts")
    }

    override fun onCleared() {
        super.onCleared()
        tts?.stop()
        tts?.shutdown()
        cleanupAudio()
        audioPlaybackManager.release()
        viewModelScope.launch {
            geminiLiveManager.disconnect()
        }
    }
}
