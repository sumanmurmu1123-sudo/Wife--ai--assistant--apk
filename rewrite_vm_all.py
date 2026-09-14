import re

with open('app/src/main/java/com/example/v2/voice/VoiceViewModel.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Add setState method and replace _engineState.value assignments
set_state_func = """
    private fun setState(newState: VoiceState, reason: String) {
        val oldState = _engineState.value
        if (oldState != newState) {
            val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", java.util.Locale.getDefault()).format(java.util.Date())
            android.util.Log.i("VoiceStateMachine", "[$timestamp] ${oldState.javaClass.simpleName} -> ${newState.javaClass.simpleName} | reason=$reason")
            _engineState.value = newState
        }
    }
"""
content = content.replace("private var playbackJob: Job? = null", "private var playbackJob: Job? = null\n" + set_state_func)

# Replace all simple assignments
content = re.sub(r'_engineState\.value\s*=\s*(VoiceState\.[A-Za-z0-9_]+(?:\([^)]*\))?)', r'setState(\1, "StateTransition")', content)

# 2. Fix init state
content = re.sub(r'private val _engineState = MutableStateFlow<VoiceState>\(VoiceState\.Idle\)',
"""private val _engineState = MutableStateFlow<VoiceState>(
        if (androidx.core.content.ContextCompat.checkSelfPermission(
                application,
                android.Manifest.permission.RECORD_AUDIO
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) VoiceState.Disconnected else VoiceState.PermissionRequired
    )""", content)

# 3. Replace Idle with Disconnected everywhere
content = content.replace("VoiceState.Idle", "VoiceState.Disconnected")

# 4. Thinking state transitions
content = content.replace(
"""        viewModelScope.launch {
            geminiLiveManager.sendClientContentMessage(text)
        }""",
"""        viewModelScope.launch {
            setState(VoiceState.Thinking, "TextCommandSent")
            geminiLiveManager.sendClientContentMessage(text)
        }"""
)

content = content.replace(
"""                geminiLiveManager.sendClientContentMessage("SYSTEM TRIGGER (FIRST GREETING ENGINE): The user just opened the app. Give them a very cute, warm, and romantic first greeting based on the current time of day. Keep it brief. Do not wait for them to speak first.")
            }""",
"""                setState(VoiceState.Thinking, "FirstGreetingTriggered")
                geminiLiveManager.sendClientContentMessage("SYSTEM TRIGGER (FIRST GREETING ENGINE): The user just opened the app. Give them a very cute, warm, and romantic first greeting based on the current time of day. Keep it brief. Do not wait for them to speak first.")
            }"""
)

content = content.replace(
"""            viewModelScope.launch {
                geminiLiveManager.sendClientContentMessage("The user triggered the action: $actionName")
            }""",
"""            viewModelScope.launch {
                setState(VoiceState.Thinking, "ActionTriggered")
                geminiLiveManager.sendClientContentMessage("The user triggered the action: $actionName")
            }"""
)

# 5. turnComplete
content = content.replace(
"""            geminiLiveManager.turnCompleteFlow.collect {
                if (_engineState.value == VoiceState.Speaking) {
                    avatarController.setLipSyncActive(false)
                    // Return to listening
                    setState(VoiceState.Listening, "StateTransition")
                    startListeningMic()
                }
            }""",
"""            geminiLiveManager.turnCompleteFlow.collect {
                if (_engineState.value == VoiceState.Speaking || _engineState.value == VoiceState.Thinking) {
                    avatarController.setLipSyncActive(false)
                    // Return to listening
                    setState(VoiceState.Listening, "TurnCompleted")
                    startListeningMic()
                }
            }"""
)

# 6. Mic error
content = content.replace(
"""                if (_engineState.value == VoiceState.Listening) {
                    setState(VoiceState.Disconnected, "StateTransition")
                }""",
"""                if (_engineState.value == VoiceState.Listening) {
                    setState(VoiceState.Error("মাইক্রোফোন উপলব্ধ নেই"), "MicInitializationFailed")
                    avatarController.playAnimation(AvatarAnimation.IDLE)
                }"""
)

# 7. Meaningful reasons
content = content.replace('setState(VoiceState.Listening, "StateTransition")', 'setState(VoiceState.Listening, "ReadyToListen")')
content = content.replace('setState(VoiceState.Connected, "StateTransition")', 'setState(VoiceState.Connected, "SetupComplete")')
content = content.replace('setState(VoiceState.Speaking, "StateTransition")', 'setState(VoiceState.Speaking, "AudioChunkReceived")')
content = content.replace('setState(VoiceState.Disconnected, "StateTransition")', 'setState(VoiceState.Disconnected, "UserInterruptedOrIdle")')
content = content.replace('setState(VoiceState.Error(errorMsg), "StateTransition")', 'setState(VoiceState.Error(errorMsg), "ConnectionError")')
content = content.replace('setState(VoiceState.PermissionRequired, "StateTransition")', 'setState(VoiceState.PermissionRequired, "PermissionDenied")')
content = content.replace('setState(VoiceState.Connecting, "StateTransition")', 'setState(VoiceState.Connecting, "ConnectingToGemini")')
content = content.replace('setState(VoiceState.Interrupted, "StateTransition")', 'setState(VoiceState.Disconnected, "UserInterrupted")')

# 8. Fix the when block completely manually by string replacement
old_when = """        when (_engineState.value) {
            is VoiceState.Disconnected, is VoiceState.Unavailable, is VoiceState.Interrupted, is VoiceState.Error, is VoiceState.PermissionRequired -> {
                startConversation(context)
            }
            is VoiceState.Listening, is VoiceState.Thinking, is VoiceState.Speaking, is VoiceState.Connected -> {
                interruptConversation()
            }
            is VoiceState.Connecting, is VoiceState.Initializing, is VoiceState.Reconnecting -> {
                // Do nothing
            }
        }"""
new_when = """        when (_engineState.value) {
            is VoiceState.Listening, is VoiceState.Thinking, is VoiceState.Speaking, is VoiceState.Connected -> {
                interruptConversation()
            }
            is VoiceState.Connecting, is VoiceState.Initializing, is VoiceState.Reconnecting -> {
                // Do nothing
            }
            else -> {
                startConversation(context)
            }
        }"""
if old_when in content:
    content = content.replace(old_when, new_when)
else:
    print("Warning: old_when block not found exactly.")
    # The original file has VoiceState.Idle inside the block! Let's find it.
    old_when_orig = """        when (_engineState.value) {
            is VoiceState.Disconnected, is VoiceState.Interrupted, is VoiceState.Error, is VoiceState.PermissionRequired -> {
                startConversation(context)
            }
            is VoiceState.Listening, is VoiceState.Thinking, is VoiceState.Speaking -> {
                interruptConversation()
            }
            is VoiceState.Connecting -> {
                // Do nothing
            }
        }"""
    if old_when_orig in content:
        content = content.replace(old_when_orig, new_when)

with open('app/src/main/java/com/example/v2/voice/VoiceViewModel.kt', 'w', encoding='utf-8') as f:
    f.write(content)
