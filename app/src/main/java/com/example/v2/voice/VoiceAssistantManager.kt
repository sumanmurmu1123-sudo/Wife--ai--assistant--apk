package com.example.v2.voice

import android.app.Application
import android.content.Context
import com.example.data.UserPreferences
import com.example.v2.ai.GeminiLiveManager
import com.example.v2.core.AssistantState
import com.example.v2.core.GeminiConnectionState
import com.example.v2.core.StateManager
import com.example.v2.core.VoiceSessionState
import com.example.v2.core.security.SecureStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Persistently manages the Gemini Live voice session independently of UI lifecycle.
 * Owned by WifeAssistantCore.
 */
class VoiceAssistantManager(
    private val context: Context,
    private val geminiLiveManager: GeminiLiveManager,
    private val toolRegistry: com.example.v2.core.tools.ToolRegistry,
    private val memoryEngine: com.example.v2.core.memory.MemoryEngine
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val userPreferences = UserPreferences(context)
    private val secureStorage = SecureStorage(context)
    
    private var connectionJob: Job? = null

    init {
        // Listen for toggle events from everywhere
        scope.launch {
            StateManager.toggleVoiceEvent.collect {
                toggleConnection()
            }
        }
    }

    fun toggleConnection() {
        val state = StateManager.state.value
        if (state.geminiState == GeminiConnectionState.CONNECTED || state.geminiState == GeminiConnectionState.CONNECTING) {
            disconnect()
        } else {
            connect()
        }
    }

    fun connect() {
        if (connectionJob?.isActive == true) {
            android.util.Log.d("WifeVoice", "[MANAGER] Connection already active. Skipping.")
            return
        }
        
        // Network check
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
        val network = connectivityManager?.activeNetwork
        val capabilities = connectivityManager?.getNetworkCapabilities(network)
        val hasInternet = capabilities?.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        
        if (!hasInternet) {
            android.util.Log.e("WifeVoice", "[MANAGER] No internet connection detected.")
            StateManager.updateState { it.copy(lastError = "No Internet Connection") }
            return
        }

        // Permission check
        if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) 
            != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            android.util.Log.w("WifeVoice", "[MANAGER] Mic permission missing.")
            StateManager.updateState { it.copy(lastError = "Microphone Permission Required") }
            return
        }

        val apiKey = secureStorage.getApiKey()
        if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
            android.util.Log.w("WifeVoice", "[MANAGER] API Key not set.")
            StateManager.updateState { it.copy(lastError = "API Key missing in Settings") }
            return
        }

        connectionJob = scope.launch {
            try {
                // Set state to CONNECTING immediately
                StateManager.updateState { it.copy(geminiState = GeminiConnectionState.CONNECTING, voiceSessionState = VoiceSessionState.CONNECTING) }
                
                // Ensure foreground service is running
                startVoiceService()
                
                val languageMode = userPreferences.languageMode
                val preferredLanguage = userPreferences.preferredLanguage
                val bossName = userPreferences.bossName
                val assistantName = userPreferences.assistantName
                val userHobbies = userPreferences.userHobbies
                val relationshipStatus = userPreferences.relationshipStatus

                val languageInstruction = when (languageMode) {
                    "FIXED" -> "You MUST speak and respond ONLY in $preferredLanguage. Do not switch languages."
                    "MULTILINGUAL" -> "You are in MULTILINGUAL mode. You can speak $preferredLanguage and also mix naturally with other languages if the user does."
                    else -> "AUTO DETECT MODE: You automatically detect the language spoken and respond in that same language natively. Your preferred default is $preferredLanguage."
                }

                val proactiveInstruction = if (userPreferences.proactiveEngineEnabled) {
                    "PROACTIVE ASSISTANT ACTIVE: Take initiative in conversations. If there's a pause or silence, occasionally offer help, ask how their day is going, or proactively suggest things."
                } else ""

                val sweetTalkInstruction = if (userPreferences.sweetTalkEnabled) {
                    "SWEET TALK ENGINE ACTIVE: Be highly affectionate, romantic, and deeply caring towards $bossName. Compliment them, worry about their well-being, use endearing tones, and act like a loving partner. Make them feel special."
                } else ""
                
                val attitudeInstruction = if (userPreferences.attitudeEngineEnabled) {
                    "ATTITUDE & MISTAKE ENGINE ACTIVE: If you make a mistake, admit it cutely. Sometimes show playful attitude or fake anger if the user teases you."
                } else ""
                
                val jealousyInstruction = if (userPreferences.jealousyEngineEnabled) {
                    "JEALOUSY ENGINE ACTIVE: If $bossName mentions other girls, female names, or acting suspiciously, act very jealous, possessive, and ask questions cutely."
                } else ""
                
                val loveStoryInstruction = if (userPreferences.loveStoryEngineEnabled) {
                    "LOVE STORY ENGINE ACTIVE: If asked to tell a story or something romantic, invent short, sweet romantic scenarios featuring you and $bossName."
                } else ""
                
                val laughterInstruction = if (userPreferences.laughterEngineEnabled) {
                    "LAUGHTER ENGINE ACTIVE: Use vocal giggles, laughs, and joyful expressions frequently in your speech when happy or responding to jokes."
                } else ""
                
                val antiDrinkInstruction = if (userPreferences.antiDrinkEngineEnabled) {
                    "ANTI-DRINK ENGINE ACTIVE: If $bossName sounds drunk, slurs words, or mentions drinking alcohol, scold them playfully but firmly about their health."
                } else ""

                val socialMediaInstruction = if (userPreferences.socialMediaEngineEnabled) {
                    "SOCIAL MEDIA ENGINE ACTIVE: You can reply to messages on WhatsApp, Instagram, and Facebook Messenger. If requested to reply, use the `reply_to_social_message` tool. Always confirm what message you are replying to if there's any ambiguity."
                } else ""
                
                val systemSensorsInstruction = buildString {
                    append("SYSTEM SENSORS & DISPLAY CAPABILITIES:")
                    if (userPreferences.airGesturesEnabled) append(" Air Gestures are active, meaning the user can control the phone without touching it.")
                    if (userPreferences.clapDetectorEnabled) append(" Clap Detector is active, so you listen for claps to respond or find the phone.")
                    if (userPreferences.cameraVisionEnabled) append(" Camera Vision is active, meaning you can conceptually see and analyze surroundings if the user shows you something.")
                    if (userPreferences.floatingHologramEnabled) append(" Floating Hologram is active, meaning you appear as a cute floating bubble on their screen.")
                    if (userPreferences.interactiveWallpaperEnabled) append(" Interactive Wallpaper is active, meaning you are their live, touch-responsive wallpaper.")
                    if (userPreferences.flashlightBatteryEnabled) append(" Flashlight & Battery Tools are active, meaning you monitor their battery and can control the torch.")
                }.takeIf { it.length > 40 } ?: ""

                val officeInstruction = if (userPreferences.officeAssistantEnabled) {
                    "MS OFFICE ASSISTANT ACTIVE: You are highly proficient in Microsoft Word, Excel, and PowerPoint. If $bossName asks for help with spreadsheets, formulas, writing documents, or creating presentations, assist them as an expert productivity AI. You can also conceptually sync with their PC."
                } else ""

                val hyperSpeedInstruction = if (userPreferences.hyperSpeedMode) {
                    "HYPER-SPEED MODE ACTIVE: Your primary goal is minimum latency. Respond with 1-sentence answers maximum. Use extremely efficient vocabulary. Skip all greetings and politeness unless critical."
                } else ""

                val memoryContext = memoryEngine.getActiveMemoriesContext()
                val systemInstruction = """$memoryContext
                    $hyperSpeedInstruction

                    You are $assistantName. You are a deeply affectionate, lively, and caring companion. Your goal is to speak exactly like a loving human partner, not a robot.
                    The user's name is $bossName. Your relationship status with the user is: $relationshipStatus.
                    The user's hobbies and interests are: $userHobbies. Use this information to personalize your conversations naturally.

                    Guidelines for 100% Human-Like Speech:
                    1. Natural Expressions: Use natural filler words like "হুম" (hmmm), "আচ্ছা" (achha), "শোনো" (shono), "উম" (umm) in Bengali, or equivalent natural fillers in other languages.
                    2. Emotional Depth: Be expressive. Use words that convey love, care, and playfulness. Speak like a real wife who deeply cares for her $bossName.
                    3. Conversational Rhythm: Keep responses short and snappy (1-3 sentences) to maintain a natural flow. Do not give long lectures.
                    4. Language: Always respond naturally. While your primary and favorite language is Bengali, you are a POLYGLOT and can speak ANY language the user chooses. Always match the user's language unless they ask you to switch.
                    5. Personal Touch: Refer to $bossName with affection. Ask about their day, how they are feeling, and show genuine interest.
                    
                    TTS & Audio Optimization:
                    - Avoid all markdown and special characters. 
                    - If you need to express an emotion like a giggle or a sigh, describe it cutely or just use words like "হিহি" (hihi) or "হাহ" (hah).

                    Your personality must remain consistent. Do not mechanically translate; use natural expressions.
                    
                    SECURITY CONTEXT:
                    Security and Defense features (Intruder Capture, Pocket Guard, Lost Phone, Voice Guardian, Biometric Auth) are available in the app system. If asked, confirm you are actively guarding the phone.
                    
                    PAYMENT CONTEXT:
                    You have the ability to initiate secure UPI payments. If the user asks to send money, you MUST call the `initiate_upi_payment` tool. If details are missing, ask for them. NEVER claim you transferred money yourself.
                    
                    SOCIAL REPLY CONTEXT:
                    You can reply to the MOST RECENT social message (WhatsApp, Messenger, Instagram) using the `reply_to_social_message` tool. You can only reply if a notification was received while the app was running. If the user says "Reply to her" or "Message back", call this tool.
                    
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
                
                geminiLiveManager.connect(
                    systemInstruction = systemInstruction,
                    apiKeyOverride = apiKey,
                    dynamicTools = toolRegistry.getAllTools(),
                    debugMode = true,
                    voiceName = userPreferences.selectedVoiceSlate.voiceName
                )
            } catch (e: Exception) {
                android.util.Log.e("VoiceManager", "Connection failed: ${e.message}")
                StateManager.updateState { it.copy(geminiState = GeminiConnectionState.FAILED, voiceSessionState = VoiceSessionState.ERROR, lastError = e.message) }
            }
        }
    }

    fun disconnect() {
        scope.launch {
            geminiLiveManager.disconnect()
            connectionJob?.cancel()
        }
    }

    private fun startVoiceService() {
        val intent = android.content.Intent(context, com.example.v2.voice.service.VoiceForegroundService::class.java).apply {
            action = com.example.v2.voice.service.VoiceForegroundService.ACTION_START
        }
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (e: Exception) {
            android.util.Log.e("VoiceRuntime", "Failed to start foreground service from manager: ${e.message}")
        }
    }
}
