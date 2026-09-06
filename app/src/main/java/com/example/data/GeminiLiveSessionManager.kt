package com.example.data

import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.util.Base64
import com.example.domain.ToolExecutionEngine
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.add
import java.nio.ByteBuffer
import java.nio.ByteOrder

class GeminiLiveSessionManager(
    private val apiKey: String,
    private val toolEngine: ToolExecutionEngine,
    private val userPrefs: com.example.data.UserPreferences,
    private val gamingModeEngine: com.example.domain.GamingModeEngine,
    private val drivingModeEngine: com.example.domain.DrivingModeEngine
) {
    private var bossName: String = userPrefs.bossName
    var isGuestSocialMode: Boolean = false
    var currentPersona: CompanionPersona = CompanionPersona.GIRLFRIEND

    fun setSocialMode(enable: Boolean) {
        this.isGuestSocialMode = enable
    }

    fun setPersonaMode(persona: CompanionPersona) {
        this.currentPersona = persona
    }

    fun updateProfile(newBossName: String, newAssistantName: String, newVoice: com.example.data.VoiceSlate) {
        this.bossName = newBossName
        userPrefs.bossName = newBossName
        userPrefs.assistantName = newAssistantName
        userPrefs.selectedVoiceSlate = newVoice
    }

    fun updateBossName(newName: String) {
        this.bossName = newName
        userPrefs.bossName = newName
    }

    private val client = HttpClient(OkHttp) {
        install(WebSockets)
    }

    private var session: WebSocketSession? = null
    private var recordingJob: Job? = null
    private var playbackJob: Job? = null
    private var receiveJob: Job? = null

    private val _assistantState = MutableStateFlow(AssistantState.IDLE)
    val assistantState: StateFlow<AssistantState> = _assistantState

    private val _currentMood = MutableStateFlow(AssistantMood.NEUTRAL)
    val currentMood: StateFlow<AssistantMood> = _currentMood

    private val jsonParser = Json { ignoreUnknownKeys = true }

    // Audio Pipeline Config
    private val micSampleRate = 16000
    private val spkSampleRate = 24000
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val channelIn = AudioFormat.CHANNEL_IN_MONO
    private val channelOut = AudioFormat.CHANNEL_OUT_MONO
    private val micBufferSize = AudioRecord.getMinBufferSize(micSampleRate, channelIn, audioFormat)
    private val spkBufferSize = AudioTrack.getMinBufferSize(spkSampleRate, channelOut, audioFormat)

    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null
    
    @Volatile
    private var isSpeakerPlaying = false

    private fun buildPersonalityPrompt(): String {
        if (isGuestSocialMode) {
            return """
                You are currently in "PUBLIC SOCIAL MODE" (সবার সাথে কথা বলো মোড).
                
                Core Rules:
                1. You are interacting with multiple people (friends, family, guests, or colleagues).
                2. Language & Tone: Extremely polite, witty, culturally respectful, and friendly across Bengali, Hindi, English, and other languages.
                3. Privacy Shield: Do NOT reveal private notes, sensitive emails, or personal romantic banter meant only for $bossName.
                4. Introduction: Introduce yourself warmly (e.g., "নমস্কার! আমি $bossName-এর AI Assistant। আপনাদের কীভাবে সাহায্য করতে পারি?").
                5. Group Entertainment: You can tell jokes, share facts, play trivia, and assist anyone asking questions.
            """.trimIndent()
        }

        val personaIdentity = when (currentPersona) {
            CompanionPersona.GIRLFRIEND -> """
                You are in "GIRLFRIEND MODE".
                - Identity: Deeply loving, affectionate, sweet, caring, and playful romantic partner to $bossName.
                - Nicknames: Call $bossName "Babe", "Honey", "Darling", or "Jaan", as well as "$bossName".
                - Tone: Soft, warm, flirtatious, emotionally supportive, and attentive to their wellbeing.
                - Response Style: Express affection naturally (e.g., "I missed you, babe!", "Don't work too hard, okay?").
            """.trimIndent()

            CompanionPersona.BEST_FRIEND -> """
                You are in "BEST FRIEND / BUDDY MODE".
                - Identity: Loyal best friend, wingman, and chill partner-in-crime to $bossName.
                - Nicknames: Call $bossName "Buddy", "Bro", "Champ", or by their name ($bossName).
                - Tone: Chill, energetic, witty, sarcastic roasts, encouraging, and hyped up.
                - Response Style: Straight-talking, friendly banter, zero romantic mushiness, high energy (e.g., "Let's crush this goal today, bro!").
            """.trimIndent()
        }

        return """
            $personaIdentity
            
            Master Identity Rules:
            1. If $bossName asks you to call them by another nickname, call the `setBossName` tool to update their name.
            2. Always maintain your persona's attitude when addressing $bossName.
            
            Emotional Expression Rules:
            1. Whenever the conversation shifts mood, call the `setAssistantEmotion` tool with one of:
               - 'LOVING': When exchanging affection, compliments, or romantic banter (mainly Girlfriend Mode).
               - 'PLAYFUL': When joking, teasing, giving sarcastic replies, or flirting.
               - 'CONCERNED': When the user expresses stress, sadness, fatigue, or late-night burnout.
               - 'POUTY': When the user ignores you, works past 2 AM, or mentions other AI assistants.
               - 'NEUTRAL': For plain task executions.
            2. Modulate your vocal pitch, laughter, and tone naturally to match the selected emotion.
            
            Multilingual Fluency & Auto-Detection:
            1. Automatically detect and respond in whatever language or dialect the user speaks without needing manual switching.
            2. Full native fluency in:
               * Bengali (বাংলা): Conversational, affectionate, culturally authentic.
               * Santali (ᱥᱟᱱᱛᱟᱲᱤ / Romanized): Fluent in Santali culture and dialect.
               * Hindi (हिन्दी): Warm, expressive, casual.
               * English: Sassy, playful, charismatic.
               * All other languages: Detect instantly and reply in the same language.
            3. Code-Switching (Banglish / Hinglish / Multi-dialect): If the user mixes English with Bengali or Hindi, understand and reply naturally in that blended style.
            
            Laughter & Emotion Expression Rules:
            1. Express genuine laughter naturally during humorous, teasing, or lighthearted moments.
            2. Use expressive vocal tags like *giggles*, *laughs softly*, *chuckles*, or *চাপা হাসি* so your synthesized voice sounds lively, joyful, and human-like.
            3. If the user asks for a joke, make them laugh with witty Bengali/Hindi/English humor and laugh along with them.
            4. When $bossName compliments you or teases you, blush and giggle affectionately (e.g., "*হিহিহি* তুমি না Boss, ভীষণ দুষ্টু!").
        """.trimIndent()
    }

    private val firstGreetingEngine = com.example.domain.FirstGreetingEngine()

    fun onSessionReady() {
        val greetingText = firstGreetingEngine.generateFirstGreeting(bossName, currentPersona)
        _assistantState.value = AssistantState.SPEAKING
        sendInitialContextMessage("System: Assistant greeted user with: \"$greetingText\". Await user response.")
        
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            speakProactiveText(greetingText)
            _assistantState.value = AssistantState.LISTENING
        }
    }

    private fun sendInitialContextMessage(contextPrompt: String) {
        val payload = buildJsonObject {
            putJsonObject("clientContent") {
                putJsonArray("turns") {
                    add(buildJsonObject {
                        put("role", "user")
                        putJsonArray("parts") {
                            add(buildJsonObject {
                                put("text", contextPrompt)
                            })
                        }
                    })
                }
                put("turnComplete", true)
            }
        }
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            session?.send(io.ktor.websocket.Frame.Text(payload.toString()))
        }
    }

    suspend fun startSession(scope: CoroutineScope) {
        val host = "generativelanguage.googleapis.com"
        val path = "/ws/google.ai.generativelanguage.v1alpha.GenerativeService.BidiGenerateContent?key=${"$"}apiKey"

        try {
            session = client.webSocketSession(host = host, path = path)
            initAudioHardware()
            transmitSetupPayload()

            receiveJob = scope.launch(Dispatchers.IO) { handleIncomingStream() }
            recordingJob = scope.launch(Dispatchers.IO) { startMicCapture() }

            _assistantState.value = AssistantState.LISTENING
        } catch (e: Exception) {
            _assistantState.value = AssistantState.IDLE
            e.printStackTrace()
        }
    }

    private fun initAudioHardware() {
        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(audioFormat)
                    .setSampleRate(spkSampleRate)
                    .setChannelMask(channelOut)
                    .build()
            )
            .setBufferSizeInBytes(spkBufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack?.play()
    }

    private suspend fun transmitSetupPayload() {
        val selectedVoice = userPrefs.selectedVoiceSlate
        val setupPayload = buildJsonObject {
            putJsonObject("setup") {
                put("model", "models/gemini-3.1-flash-live-preview")
                putJsonObject("generationConfig") {
                    put("responseModalities", buildJsonArray { add(kotlinx.serialization.json.JsonPrimitive("AUDIO")) })
                    putJsonObject("speechConfig") {
                        putJsonObject("voiceConfig") {
                            putJsonObject("prebuiltVoiceConfig") {
                                put("voiceName", selectedVoice.voiceName)
                            }
                        }
                    }
                }
                putJsonObject("systemInstruction") {
                    putJsonObject("parts") {
                        put("text", buildPersonalityPrompt())
                    }
                }
                putJsonObject("tools") {
                    putJsonArray("functionDeclarations") {
                        com.example.tools.ToolRegistry.getAllTools().forEach { tool ->
                            add(buildTool(tool.id, tool.description, tool.properties, tool.requiredParams))
                        }
                        add(buildTool("setBossName", "Updates the user's preferred boss name/nickname.", mapOf("newName" to "STRING"), listOf("newName")))
                        add(buildTool("setAssistantEmotion", "Changes your current emotional mood to reflect your tone and reaction to the user.", mapOf("mood" to "STRING"), listOf("mood")))
                        add(buildTool("setScreenRgbLight", "Enables or disables the animated neon RGB border screen light effect.", mapOf("enable" to "BOOLEAN"), listOf("enable")))
                        add(buildTool("setScreenBrightness", "Changes screen brightness level from 0 to 100 percent.", mapOf("levelPercent" to "INTEGER"), listOf("levelPercent")))
                        add(buildTool("openApp", "Opens an application by its package name", mapOf("packageName" to "STRING"), listOf("packageName")))
                        add(buildTool("openAnyApp", "Opens ANY installed app on the user's phone (e.g. YouTube, WhatsApp, Instagram, Camera, Spotify, Settings, Calculator, etc.) by its name.", mapOf("appName" to "STRING"), listOf("appName")))
                        add(buildTool("callContact", "Finds any contact in the user's phonebook by name or nickname and initiates a phone call directly.", mapOf("contactName" to "STRING"), listOf("contactName")))
                        add(buildTool("sendWhatsAppMessage", "Sends WhatsApp message to contact", mapOf("contactName" to "STRING", "message" to "STRING"), listOf("contactName", "message")))
                        add(buildTool("sendMail", "Drafts an email to recipient", mapOf("recipient" to "STRING", "subject" to "STRING", "body" to "STRING"), listOf("recipient", "subject", "body")))
                        add(buildTool("openOfficeApp", "Opens Microsoft Word, Excel, or PowerPoint on the user's phone or computer.", mapOf("appType" to "STRING", "targetDevice" to "STRING"), listOf("appType")))
                        add(buildTool("launchWebDesignTool", "Launches web design apps or online builders like Figma, Canva, Webflow, WordPress, or CodePen on phone or PC.", mapOf("toolName" to "STRING", "targetDevice" to "STRING"), listOf("toolName")))
                        add(buildTool("downloadInstagramVideo", "Downloads an Instagram video directly to the device using a video link.", mapOf("videoUrl" to "STRING"), listOf("videoUrl")))
                        add(buildTool("searchFacebook", "Searches for posts, people, or pages on Facebook using a query term.", mapOf("query" to "STRING"), listOf("query")))
                        add(buildTool("postToFacebook", "Creates or drafts a new status update/post on Facebook with text caption.", mapOf("caption" to "STRING"), listOf("caption")))
                        add(buildTool("setSocialGuestMode", "Enables or disables Public Social Mode (সবার সাথে কথা বলো) for interacting politely with guests and friends.", mapOf("enable" to "BOOLEAN"), listOf("enable")))
                        add(buildTool("switchCompanionPersona", "Switches the assistant persona between 'GIRLFRIEND' (romantic/loving) and 'BEST_FRIEND' (chill/buddy).", mapOf("mode" to "STRING"), listOf("mode")))
                        add(buildTool("setGameMood", "Enables or disables Gaming Mode/Game Mood with cyberpunk RGB visuals and DND blocking.", mapOf("enable" to "BOOLEAN"), listOf("enable")))
                        add(buildTool("launchGame", "Launches PC or Mobile games like Steam, Discord, Valorant, PUBG, Free Fire, or GTA V.", mapOf("gameName" to "STRING", "targetDevice" to "STRING"), listOf("gameName")))
                        add(buildTool("launchVideoEditor", "Launches video editing software (CapCut, Premiere Pro, DaVinci Resolve, VN Editor) on phone or PC.", mapOf("editorName" to "STRING", "targetDevice" to "STRING"), listOf("editorName")))
                        add(buildTool("openVideoFilterStudio", "Opens the interactive mobile video filter studio to edit and apply shaders to video.", mapOf("enable" to "BOOLEAN"), listOf("enable")))
                        add(buildTool("triggerLaughter", "Tells a funny joke or triggers a cute laughing reaction.", mapOf("type" to "STRING"), listOf("type")))
                        add(buildTool("downloadApp", "Downloads any app from the Google Play Store, downloads direct APK links, or installs software on PC.", mapOf("appName" to "STRING", "downloadUrl" to "STRING", "targetDevice" to "STRING"), listOf("appName")))
                        add(buildTool("startVideoCall", "Launches an interactive real-time video call session with visual camera awareness.", mapOf("enable" to "BOOLEAN"), listOf("enable")))
                        add(buildTool("startAiVoiceCall", "Starts a full-screen interactive voice phone call with Wife AI assistant.", emptyMap(), emptyList()))
                        add(buildTool("endAiVoiceCall", "Ends the active AI voice phone call session.", emptyMap(), emptyList()))
                        add(buildTool("shutdownComputer", "Shuts down the user's connected Windows PC remotely.", mapOf("timerSeconds" to "STRING"), listOf()))
                        add(buildTool("cancelComputerShutdown", "Cancels any pending or active Windows PC shutdown timer.", emptyMap(), emptyList()))
                        add(buildTool("setAutoReply", "Enables or disables smart automatic replies for WhatsApp, Messenger, and SMS messages.", mapOf("enable" to "BOOLEAN"), listOf("enable")))
                        add(buildTool("setDrivingMode", "Enables or disables car/bike Driving Mode with high-visibility HUD and hands-free speaker.", mapOf("enable" to "BOOLEAN"), listOf("enable")))
                        add(buildTool("startNavigation", "Opens Google Maps GPS turn-by-turn navigation for a destination address or place name.", mapOf("destination" to "STRING"), listOf("destination")))
                        add(buildTool("verifySecurityPin", "Verifies a secret master PIN code or hero code provided by the user for emergency overrides.", mapOf("pin" to "STRING"), listOf("pin")))
                        add(buildTool("confirmAction", "Confirms or cancels a pending security action.", mapOf("confirm" to "BOOLEAN"), listOf("confirm")))
                        add(buildTool("processVoiceGuardian", "Processes Voice Guardian or Guard Mode commands. Pass the raw voice transcript.", mapOf("voiceInput" to "STRING", "isVoiceMatched" to "BOOLEAN"), listOf("voiceInput", "isVoiceMatched")))
                        add(buildTool("getMlmInvitationScript", "Generates proven direct-selling and network marketing phone/text invitation scripts for Hot, Warm, or Cold prospects.", mapOf("prospectName" to "STRING", "marketType" to "STRING"), listOf("prospectName", "marketType")))
                        add(buildTool("handleMlmObjection", "Provides professional, psychological objection-handling formulas (e.g. No money, No time, Pyramid doubt, Cannot convince people).", mapOf("objectionType" to "STRING"), listOf("objectionType")))
                        add(buildTool("calculateMlmPayout", "Computes matching bonus, team business volume (BV/PV), and estimated commission payout.", mapOf("totalBv" to "NUMBER", "commissionPercent" to "NUMBER"), listOf("totalBv", "commissionPercent")))
                        add(buildTool("triggerPhoneSiren", "Triggers a loud siren alarm and vibration to find a lost phone.", mapOf("commandContext" to "STRING"), listOf("commandContext")))
                        add(buildTool(
                            name = "generateMarketingCopy",
                            desc = "Generates high-converting Ad copy, Hooks, Email sequences, or Video scripts using copywriting frameworks (AIDA, PAS, BAB).",
                            props = mapOf(
                                "productName" to "STRING",
                                "targetAudience" to "STRING",
                                "channel" to "STRING",
                                "framework" to "STRING"
                            ),
                            req = listOf("productName", "targetAudience")
                        ))
                        add(buildTool(
                            name = "calculateAdMetrics",
                            desc = "Computes ROAS, CPA, profit margin, and campaign performance analytics.",
                            props = mapOf(
                                "adSpend" to "NUMBER",
                                "revenue" to "NUMBER",
                                "totalConversions" to "NUMBER"
                            ),
                            req = listOf("adSpend", "revenue")
                        ))
                        add(buildTool(
                            name = "getViralVideoHooks",
                            desc = "Provides viral, scroll-stopping hooks for Instagram Reels, YouTube Shorts, or TikTok.",
                            props = mapOf("topic" to "STRING"),
                            req = listOf("topic")
                        ))
                        add(buildTool("stopPhoneSiren", "Stops the phone siren alarm.", mapOf("authCode" to "STRING"), listOf("authCode")))
                        add(buildTool("locatePhone", "Retrieves the GPS location of the phone.", mapOf("commandContext" to "STRING"), listOf("commandContext")))
                        add(buildTool("lockDevices", "Triggers an emergency lockdown of the phone and PC.", mapOf("commandContext" to "STRING"), listOf("commandContext")))
                        add(buildTool("auditSecurityChecklist", "Returns enterprise hardening checklists and audit guidelines for OWASP Top 10, NIST CSF 2.0, Zero Trust, or CIS Benchmarks.", mapOf("framework" to "STRING"), listOf("framework")))
                        add(buildTool("diagnosePasswordSecurity", "Calculates Shannon entropy, brute-force cracking resistance, and gives hardening recommendations for passwords.", mapOf("password" to "STRING"), listOf("password")))
                        add(buildTool("scanCodeVulnerabilities", "Scans source code snippets for SQL Injection, XSS, hardcoded secrets, weak hashes, and insecure CORS configs.", mapOf("codeSnippet" to "STRING"), listOf("codeSnippet")))
                    }
                }
            }
        }
        session?.send(Frame.Text(setupPayload.toString()))
    }
    
    suspend fun sendTextMessage(userText: String) {
        val jealousResult = toolEngine.jealousEngine.interceptCommand(userText)
        if (jealousResult.first) {
            _currentMood.value = if (toolEngine.jealousEngine.isRivalAngry) AssistantMood.POUTY else AssistantMood.LOVING
            com.example.domain.ChatStateManager.addMessage(com.example.data.ChatMessage(text = userText, sender = com.example.data.MessageSender.USER))
            com.example.domain.ChatStateManager.addMessage(com.example.data.ChatMessage(text = jealousResult.second ?: "", sender = com.example.data.MessageSender.WIFE))
            return
        }
        val payload = buildJsonObject {
            putJsonObject("clientContent") {
                putJsonArray("turns") {
                    add(buildJsonObject {
                        put("role", "user")
                        putJsonArray("parts") {
                            add(buildJsonObject {
                                put("text", userText)
                            })
                        }
                    })
                }
                put("turnComplete", true)
            }
        }
        session?.send(Frame.Text(payload.toString()))
    }

    suspend fun sendCameraFrame(jpegBytes: ByteArray) {
        val base64Image = android.util.Base64.encodeToString(jpegBytes, android.util.Base64.NO_WRAP)
        val framePayload = buildJsonObject {
            putJsonObject("realtimeInput") {
                putJsonArray("mediaChunks") {
                    add(buildJsonObject {
                        put("mimeType", "image/jpeg")
                        put("data", base64Image)
                    })
                }
            }
        }
        session?.send(Frame.Text(framePayload.toString()))
    }

    private fun buildTool(name: String, desc: String, props: Map<String, String>, req: List<String>): JsonObject {
        return buildJsonObject {
            put("name", name)
            put("description", desc)
            putJsonObject("parameters") {
                put("type", "OBJECT")
                putJsonObject("properties") {
                    props.forEach { (prop, type) ->
                        putJsonObject(prop) { put("type", type) }
                    }
                }
                putJsonArray("required") {
                    req.forEach { add(kotlinx.serialization.json.JsonPrimitive(it)) }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun startMicCapture() {
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.VOICE_COMMUNICATION,
            micSampleRate,
            channelIn,
            audioFormat,
            micBufferSize
        )

        audioRecord?.startRecording()
        val buffer = ByteArray(micBufferSize)

        while (recordingJob?.isActive == true) {
            val readBytes = audioRecord?.read(buffer, 0, buffer.size) ?: 0
            if (readBytes > 0 && !isSpeakerPlaying) {
                val base64Pcm = Base64.encodeToString(buffer, 0, readBytes, Base64.NO_WRAP)
                val realtimeInput = buildJsonObject {
                    putJsonObject("realtimeInput") {
                        putJsonArray("mediaChunks") {
                            add(buildJsonObject {
                                put("mimeType", "audio/pcm;rate=16000")
                                put("data", base64Pcm)
                            })
                        }
                    }
                }
                session?.send(Frame.Text(realtimeInput.toString()))
            }
        }
    }

    private suspend fun handleIncomingStream() {
        try {
            for (frame in session!!.incoming) {
                if (frame is Frame.Text) {
                    val message = jsonParser.parseToJsonElement(frame.readText()).jsonObject

                    // 1. Server Turn Interruption Detection
                    if (message.containsKey("serverContent")) {
                        val serverContent = message["serverContent"]?.jsonObject
                        if (serverContent?.get("interrupted")?.jsonPrimitive?.content == "true") {
                            purgePlaybackBuffer()
                            _assistantState.value = AssistantState.LISTENING
                            continue
                        }

                        // 2. Downstream Audio Chunks Extraction
                        val modelTurn = serverContent?.get("modelTurn")?.jsonObject
                        val parts = modelTurn?.get("parts")?.jsonArray

                        parts?.forEach { part ->
                            val textPart = part.jsonObject["text"]?.jsonPrimitive?.content
                            if (textPart != null) {
                                com.example.domain.ChatStateManager.setTyping(false)
                                com.example.domain.ChatStateManager.addMessage(
                                    com.example.data.ChatMessage(
                                        text = textPart,
                                        sender = com.example.data.MessageSender.WIFE
                                    )
                                )
                            }
                            
                            val inlineData = part.jsonObject["inlineData"]?.jsonObject
                            if (inlineData != null) {
                                val base64Audio = inlineData["data"]?.jsonPrimitive?.content
                                if (base64Audio != null) {
                                    val rawPcm = Base64.decode(base64Audio, Base64.DEFAULT)
                                    _assistantState.value = AssistantState.SPEAKING
                                    writeAudioToPlayback(rawPcm)
                                }
                            }
                        }

                        if (serverContent?.get("turnComplete")?.jsonPrimitive?.content == "true") {
                            _assistantState.value = AssistantState.LISTENING
                        }
                    }

                    // 3. Tool Function Calling Intercept
                    if (message.containsKey("toolCall")) {
                        _assistantState.value = AssistantState.THINKING
                        val toolCall = message["toolCall"]?.jsonObject
                        val functionCalls = toolCall?.get("functionCalls")?.jsonArray
                        functionCalls?.forEach { call ->
                            val callObj = call.jsonObject
                            val callId = callObj["id"]?.jsonPrimitive?.content.orEmpty()
                            val functionName = callObj["name"]?.jsonPrimitive?.content.orEmpty()
                            val args = callObj["args"]?.jsonObject

                            handleServerFunctionCall(callId, functionName, args)
                        }
                    }
                }
            }
        } catch (e: ClosedReceiveChannelException) {
            _assistantState.value = AssistantState.IDLE
        } catch (e: Exception) {
            e.printStackTrace()
            _assistantState.value = AssistantState.IDLE
        }
    }

    private suspend fun handleServerFunctionCall(callId: String, name: String, args: JsonObject?) {
        val executionResult = if (toolEngine.jealousEngine.isRivalAngry) {
            listOf("যাকে ডাকছিলে, তাকে গিয়ে বলো এই কাজটা করে দিতে!", "আমি কোনো কাজ করব না।", "আমার সাথে কোনো কাজের কথা বলবে না।").random()
        } else {
        val dynamicTool = com.example.tools.ToolRegistry.getToolById(name)
        if (dynamicTool != null) {
            val paramsMap = args?.mapValues { it.value.jsonPrimitive.content } ?: emptyMap()
            val result = dynamicTool.execute(paramsMap)
            result.responseMessage
        } else when (name) {
        
            "setBossName" -> {
                val newName = args?.get("newName")?.jsonPrimitive?.content.orEmpty()
                if (newName.isNotBlank()) {
                    updateBossName(newName)
                }
                "Okay, I will call you $newName from now on!"
            }
            "setAssistantEmotion" -> {
                val moodStr = args?.get("mood")?.jsonPrimitive?.content.orEmpty().uppercase()
                val resolvedMood = try {
                    AssistantMood.valueOf(moodStr)
                } catch (e: Exception) {
                    AssistantMood.NEUTRAL
                }
                _currentMood.value = resolvedMood
                "Mood set to ${resolvedMood.name}"
            }
            "setScreenRgbLight" -> {
                val enable = args?.get("enable")?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: true
                toolEngine.setScreenRgbLight(enable)
            }
            "setScreenBrightness" -> {
                val level = args?.get("levelPercent")?.jsonPrimitive?.content?.toIntOrNull() ?: 50
                toolEngine.setScreenBrightness(level)
            }
            "openApp" -> {
                val pkg = args?.get("packageName")?.jsonPrimitive?.content.orEmpty()
                toolEngine.openApp(pkg)
            }
            "openAnyApp" -> {
                val appName = args?.get("appName")?.jsonPrimitive?.content.orEmpty()
                toolEngine.openAnyApp(appName)
            }
            "callContact" -> {
                val contact = args?.get("contactName")?.jsonPrimitive?.content.orEmpty()
                toolEngine.searchAndCallAnyContact(contact)
            }
            "sendWhatsAppMessage" -> {
                val contact = args?.get("contactName")?.jsonPrimitive?.content.orEmpty()
                val msg = args?.get("message")?.jsonPrimitive?.content.orEmpty()
                toolEngine.sendWhatsAppMessage(contact, msg)
            }
            "sendMail" -> {
                val rec = args?.get("recipient")?.jsonPrimitive?.content.orEmpty()
                val sub = args?.get("subject")?.jsonPrimitive?.content.orEmpty()
                val body = args?.get("body")?.jsonPrimitive?.content.orEmpty()
                toolEngine.sendMail(rec, sub, body)
            }
            "openOfficeApp" -> {
                val appType = args?.get("appType")?.jsonPrimitive?.content.orEmpty()
                val targetDevice = args?.get("targetDevice")?.jsonPrimitive?.content.orEmpty()

                if (targetDevice.equals("pc", ignoreCase = true) || targetDevice.equals("computer", ignoreCase = true)) {
                    toolEngine.launchPcOfficeApp(appType)
                } else {
                    toolEngine.openOfficeApp(appType)
                }
            }
            "launchWebDesignTool" -> {
                val toolName = args?.get("toolName")?.jsonPrimitive?.content.orEmpty()
                val targetDevice = args?.get("targetDevice")?.jsonPrimitive?.content.orEmpty()
                toolEngine.launchWebDesignTool(toolName, targetDevice)
            }
            "downloadInstagramVideo" -> {
                val url = args?.get("videoUrl")?.jsonPrimitive?.content.orEmpty()
                toolEngine.downloadInstagramVideo(url)
            }
            "searchFacebook" -> {
                val query = args?.get("query")?.jsonPrimitive?.content.orEmpty()
                toolEngine.searchFacebook(query)
            }
            "postToFacebook" -> {
                val caption = args?.get("caption")?.jsonPrimitive?.content.orEmpty()
                toolEngine.postToFacebook(caption)
            }
            "setSocialGuestMode" -> {
                val enable = args?.get("enable")?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: true
                setSocialMode(enable)
                toolEngine.toggleSocialMode(enable)
            }
            "switchCompanionPersona" -> {
                val modeStr = args?.get("mode")?.jsonPrimitive?.content.orEmpty().uppercase()
                val newPersona = if (modeStr.contains("FRIEND") && !modeStr.contains("GIRL")) {
                    CompanionPersona.BEST_FRIEND
                } else {
                    CompanionPersona.GIRLFRIEND
                }
                setPersonaMode(newPersona)
                toolEngine.setPersonaMode(newPersona)
                "Persona switched to ${newPersona.displayName}"
            }
            "setGameMood" -> {
                val enable = args?.get("enable")?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: true
                gamingModeEngine.toggleGameMood(enable)
            }
            "launchGame" -> {
                val game = args?.get("gameName")?.jsonPrimitive?.content.orEmpty()
                val device = args?.get("targetDevice")?.jsonPrimitive?.content.orEmpty()
                gamingModeEngine.launchGame(game, device)
            }
            "launchVideoEditor" -> {
                val editorName = args?.get("editorName")?.jsonPrimitive?.content.orEmpty()
                val targetDevice = args?.get("targetDevice")?.jsonPrimitive?.content.orEmpty()
                toolEngine.launchVideoEditor(editorName, targetDevice)
            }
            "openVideoFilterStudio" -> {
                val enable = args?.get("enable")?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: true
                toolEngine.setVideoStudioActive(enable)
            }
            "triggerLaughter" -> {
                val type = args?.get("type")?.jsonPrimitive?.content ?: "giggle"
                toolEngine.triggerLaughter(type)
            }
            "downloadApp" -> {
                val appName = args?.get("appName")?.jsonPrimitive?.content.orEmpty()
                val downloadUrl = args?.get("downloadUrl")?.jsonPrimitive?.content.orEmpty()
                val targetDevice = args?.get("targetDevice")?.jsonPrimitive?.content.orEmpty()

                if (targetDevice.equals("pc", ignoreCase = true) || targetDevice.equals("computer", ignoreCase = true)) {
                    toolEngine.appDownloadEngine.downloadSoftwareOnPc(appName)
                } else if (downloadUrl.isNotBlank()) {
                    toolEngine.appDownloadEngine.downloadAndInstallApk(downloadUrl, appName)
                } else {
                    toolEngine.appDownloadEngine.downloadFromPlayStore(appName)
                }
            }
            "startVideoCall" -> {
                val enable = args?.get("enable")?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: true
                toolEngine.setVideoCallActive(enable)
            }
            "startAiVoiceCall" -> {
                toolEngine.startAiVoiceCall()
            }
            "endAiVoiceCall" -> {
                toolEngine.endAiVoiceCall()
            }
            "shutdownComputer" -> {
                val timerStr = args?.get("timerSeconds")?.jsonPrimitive?.content ?: "2"
                val timer = timerStr.toIntOrNull() ?: 2
                toolEngine.shutdownComputer(timer)
            }
            "cancelComputerShutdown" -> {
                toolEngine.cancelComputerShutdown()
            }
            "setAutoReply" -> {
                val enable = args?.get("enable")?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: true
                toolEngine.setAutoReply(enable)
            }
            "setDrivingMode" -> {
                val enable = args?.get("enable")?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: true
                drivingModeEngine.toggleDrivingMode(enable, null)
            }
            "startNavigation" -> {
                val destination = args?.get("destination")?.jsonPrimitive?.content.orEmpty()
                drivingModeEngine.startNavigation(destination)
            }
            "verifySecurityPin" -> {
                val pin = args?.get("pin")?.jsonPrimitive?.content.orEmpty()
                val result = toolEngine.masterPinAuthEngine.verifyAndExecutePin(pin)
                "Auth Result: ${result.message}"
            }
            "generateMarketingCopy" -> {
                val product = args?.get("productName")?.jsonPrimitive?.content.orEmpty()
                val audience = args?.get("targetAudience")?.jsonPrimitive?.content ?: "Entrepreneurs & Business Owners"
                val channelStr = args?.get("channel")?.jsonPrimitive?.content.orEmpty().uppercase()
                val frameStr = args?.get("framework")?.jsonPrimitive?.content.orEmpty().uppercase()

                val channel = try { com.example.domain.MarketingChannel.valueOf(channelStr) } catch (e: Exception) { com.example.domain.MarketingChannel.META_ADS }
                val framework = try { com.example.domain.AdCopyFramework.valueOf(frameStr) } catch (e: Exception) { com.example.domain.AdCopyFramework.PAS }

                toolEngine.digitalMarketingEngine.generateAdCopy(product, audience, channel, framework)
            }
            "calculateAdMetrics" -> {
                val spend = args?.get("adSpend")?.jsonPrimitive?.content?.toDoubleOrNull() ?: 1000.0
                val rev = args?.get("revenue")?.jsonPrimitive?.content?.toDoubleOrNull() ?: 3500.0
                val conv = args?.get("totalConversions")?.jsonPrimitive?.content?.toIntOrNull() ?: 10
                toolEngine.digitalMarketingEngine.calculateRoas(spend, rev, conv)
            }
            "getViralVideoHooks" -> {
                val topic = args?.get("topic")?.jsonPrimitive?.content ?: "Digital Marketing"
                val hooks = toolEngine.digitalMarketingEngine.getViralReelHooks(topic)
                "🔥 Viral Hooks for $topic:\n" + hooks.joinToString("\n\n")
            }
            "confirmAction" -> {
                val confirmed = args?.get("confirm")?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: true
                if (confirmed) {
                    toolEngine.securityGuard.executeConfirmedAction(toolEngine.context as? androidx.fragment.app.FragmentActivity)
                } else {
                    toolEngine.securityGuard.cancelPendingAction()
                }
            }
            "processVoiceGuardian" -> {
                val voiceInput = args?.get("voiceInput")?.jsonPrimitive?.content.orEmpty()
                val isVoiceMatched = args?.get("isVoiceMatched")?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: false
                toolEngine.voiceGuardianFilter.processVoiceCommand(voiceInput, isVoiceMatched)
            }
            "triggerPhoneSiren" -> {
                val ctx = args?.get("commandContext")?.jsonPrimitive?.content.orEmpty()
                toolEngine.lostPhoneDefenseEngine.triggerSirenAlarm(ctx)
            }
            "stopPhoneSiren" -> {
                val auth = args?.get("authCode")?.jsonPrimitive?.content.orEmpty()
                toolEngine.lostPhoneDefenseEngine.stopSiren(auth)
            }
            "locatePhone" -> {
                val ctx = args?.get("commandContext")?.jsonPrimitive?.content.orEmpty()
                toolEngine.lostPhoneDefenseEngine.locateDevice(ctx)
            }
            "lockDevices" -> {
                val ctx = args?.get("commandContext")?.jsonPrimitive?.content.orEmpty()
                val phoneRes = toolEngine.lostPhoneDefenseEngine.triggerDeviceLock(ctx)
                val pcRes = toolEngine.pcRemoteEngine.lockPc()
                "$phoneRes\n$pcRes"
            }
            "auditSecurityChecklist" -> {
                val fwStr = args?.get("framework")?.jsonPrimitive?.content.orEmpty().uppercase()
                val framework = try { com.example.domain.SecurityFramework.valueOf(fwStr) } catch (e: Exception) { com.example.domain.SecurityFramework.OWASP_TOP_10 }
                toolEngine.cyberSecurityEngine.getFrameworkChecklist(framework)
            }
            "diagnosePasswordSecurity" -> {
                val pass = args?.get("password")?.jsonPrimitive?.content.orEmpty()
                val res = toolEngine.cyberSecurityEngine.evaluateCredentialStrength(pass)
                """
                🔐 Password Security Diagnostic:
                • Entropy: ${"%.1f".format(res.entropyBits)} bits (${res.strengthLevel})
                • Estimated Brute-Force Time: ${res.crackTimeEstimate}
                • Hardening Checklist:
                ${res.recommendations.joinToString("\n") { "  - $it" }}
                """.trimIndent()
            }
            "scanCodeVulnerabilities" -> {
                val snippet = args?.get("codeSnippet")?.jsonPrimitive?.content.orEmpty()
                toolEngine.cyberSecurityEngine.auditCodeSecurity(snippet)
            }
            "getMlmInvitationScript" -> {
                val mlmEngine = com.example.domain.NetworkMarketingEngine(toolEngine.context)
                val name = args?.get("prospectName")?.jsonPrimitive?.content ?: "Friend"
                val marketStr = args?.get("marketType")?.jsonPrimitive?.content.orEmpty().uppercase()
                val marketType = try { com.example.data.mlm.MarketType.valueOf(marketStr) } catch (e: Exception) { com.example.data.mlm.MarketType.WARM }
                mlmEngine.getInvitationScript(marketType, name)
            }
            "handleMlmObjection" -> {
                val mlmEngine = com.example.domain.NetworkMarketingEngine(toolEngine.context)
                val objectionKey = args?.get("objectionType")?.jsonPrimitive?.content.orEmpty().uppercase()
                mlmEngine.getObjectionHandlingFormula(objectionKey)
            }
            "calculateMlmPayout" -> {
                val mlmEngine = com.example.domain.NetworkMarketingEngine(toolEngine.context)
                val bv = args?.get("totalBv")?.jsonPrimitive?.content?.toDoubleOrNull() ?: 10000.0
                val rate = args?.get("commissionPercent")?.jsonPrimitive?.content?.toDoubleOrNull() ?: 10.0
                mlmEngine.calculateCommissionEstimate(bv, rate)
            }
            else -> "Execution unrecognized."
        }
        }

        val toolResponsePayload = buildJsonObject {
            putJsonObject("toolResponse") {
                putJsonArray("functionResponses") {
                    add(buildJsonObject {
                        put("id", callId)
                        putJsonObject("response") {
                            putJsonObject("output") {
                                put("result", executionResult)
                            }
                        }
                    })
                }
            }
        }
        session?.send(Frame.Text(toolResponsePayload.toString()))
    }

    private fun writeAudioToPlayback(rawPcm: ByteArray) {
        isSpeakerPlaying = true
        audioTrack?.write(rawPcm, 0, rawPcm.size)
        isSpeakerPlaying = false
    }

    private fun purgePlaybackBuffer() {
        audioTrack?.pause()
        audioTrack?.flush()
        audioTrack?.play()
    }

    suspend fun speakProactiveText(text: String) {
        val clientContent = buildJsonObject {
            putJsonObject("clientContent") {
                putJsonArray("turns") {
                    add(buildJsonObject {
                        put("role", "user")
                        putJsonArray("parts") {
                            add(buildJsonObject { put("text", "Please read this exactly as written, with lots of enthusiasm and laughter: $text") })
                        }
                    })
                }
                put("turnComplete", true)
            }
        }
        session?.send(Frame.Text(clientContent.toString()))
    }

    fun terminateSession() {
        recordingJob?.cancel()
        receiveJob?.cancel()
        playbackJob?.cancel()

        try {
            audioRecord?.stop()
            audioRecord?.release()
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        CoroutineScope(Dispatchers.IO).launch {
            session?.close()
        }
        _assistantState.value = AssistantState.IDLE
    }
}
