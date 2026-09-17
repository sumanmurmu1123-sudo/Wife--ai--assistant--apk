import re

with open("app/src/main/java/com/example/v2/voice/VoiceViewModel.kt", "r") as f:
    content = f.read()

# Replace interruptConversation with cleanupAudio and disconnect
content = content.replace("interruptConversation()", "cleanupAudio(); viewModelScope.launch { geminiLiveManager.disconnect() }")

# Add testGeminiConnection if missing
if "fun testGeminiConnection" not in content:
    test_func = """
    fun testGeminiConnection(context: android.content.Context) {
        connectJob?.cancel()
        connectJob = viewModelScope.launch {
            setState(VoiceState.Connecting, "TestConnection")
            try {
                val prefs = context.getSharedPreferences("wife_v2_prefs", android.content.Context.MODE_PRIVATE)
                val apiKeyOverride = prefs.getString("api_key", "")
                geminiLiveManager.connect(systemInstruction = "Test", apiKeyOverride = apiKeyOverride, dynamicTools = emptyList())
                kotlinx.coroutines.delay(2000)
                if (_engineState.value == VoiceState.Connecting || _engineState.value == VoiceState.Connected) {
                    geminiLiveManager.disconnect()
                    setState(VoiceState.Disconnected, "TestComplete")
                }
            } catch (e: Exception) {
                setState(VoiceState.Error(e.message ?: "Test failed"), "TestError")
            }
        }
    }
"""
    # Insert it before triggerAction
    content = content.replace("fun triggerAction(", test_func + "\n    fun triggerAction(")

with open("app/src/main/java/com/example/v2/voice/VoiceViewModel.kt", "w") as f:
    f.write(content)
