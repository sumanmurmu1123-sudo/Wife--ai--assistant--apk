import re

with open('app/src/main/java/com/example/v2/voice/VoiceViewModel.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Remove the premature Speaking transition in textFlow
old_text_flow = """        // Listen for AI Text (Fallback for unsupported languages)
        viewModelScope.launch {
            geminiLiveManager.textFlow.collect { text ->
                if (text.isNotBlank()) {
                    if (_engineState.value != VoiceState.Speaking) {
                        setState(VoiceState.Speaking, "AudioChunkReceived")
                        avatarController.setLipSyncActive(true)
                        captureJob?.cancel()
                    }
                    // Only use Android TTS if the text contains Bengali characters"""

new_text_flow = """        // Listen for AI Text (Fallback for unsupported languages)
        viewModelScope.launch {
            geminiLiveManager.textFlow.collect { text ->
                if (text.isNotBlank()) {
                    // Only use Android TTS if the text contains Bengali characters"""
content = content.replace(old_text_flow, new_text_flow)

# Add onStart handler for TTS
old_tts = """                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) {"""
                    
new_tts = """                    override fun onStart(utteranceId: String?) {
                        if (utteranceId == "gemini_tts") {
                            setState(VoiceState.Speaking, "TTSStarted")
                            avatarController.setLipSyncActive(true)
                            captureJob?.cancel()
                        }
                    }
                    override fun onDone(utteranceId: String?) {"""
content = content.replace(old_tts, new_tts)

with open('app/src/main/java/com/example/v2/voice/VoiceViewModel.kt', 'w', encoding='utf-8') as f:
    f.write(content)
