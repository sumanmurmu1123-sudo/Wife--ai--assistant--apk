import re

with open('app/src/main/java/com/example/v2/voice/VoiceViewModel.kt', 'r') as f:
    content = f.read()

# Replace the text flow collect block
old_text_block = """        // Listen for AI Text (Fallback for unsupported languages)
        viewModelScope.launch {
            geminiLiveManager.textFlow.collect { text ->
                if (text.isNotBlank()) {
                    if (_engineState.value != VoiceState.Speaking) {
                        _engineState.value = VoiceState.Speaking
                        avatarController.setLipSyncActive(true)
                        captureJob?.cancel()
                    }
                    val params = android.os.Bundle()
                    params.putString(android.speech.tts.TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "gemini_tts")
                    tts?.speak(text, android.speech.tts.TextToSpeech.QUEUE_ADD, params, "gemini_tts")
                }
            }
        }"""

new_text_block = """        // Listen for AI Text (Fallback for unsupported languages)
        viewModelScope.launch {
            geminiLiveManager.textFlow.collect { text ->
                if (text.isNotBlank()) {
                    if (_engineState.value != VoiceState.Speaking) {
                        _engineState.value = VoiceState.Speaking
                        avatarController.setLipSyncActive(true)
                        captureJob?.cancel()
                    }
                    // Only use Android TTS if the text contains Bengali characters (since Gemini audio doesn't support it well)
                    // or if it's a known fallback scenario.
                    val hasBengali = text.any { it in '\\u0980'..'\\u09FF' }
                    if (hasBengali) {
                        val params = android.os.Bundle()
                        params.putString(android.speech.tts.TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "gemini_tts")
                        tts?.speak(text, android.speech.tts.TextToSpeech.QUEUE_ADD, params, "gemini_tts")
                    }
                }
            }
        }"""

content = content.replace(old_text_block, new_text_block)

with open('app/src/main/java/com/example/v2/voice/VoiceViewModel.kt', 'w') as f:
    f.write(content)
