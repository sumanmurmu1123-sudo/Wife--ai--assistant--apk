import re

with open('app/src/main/java/com/example/v2/voice/VoiceViewModel.kt', 'r') as f:
    content = f.read()

old_audio_block = """        // Listen for AI Audio
        viewModelScope.launch {
            geminiLiveManager.audioFlow.collect { pcmData ->
                if (_engineState.value != VoiceState.Speaking) {
                    _engineState.value = VoiceState.Speaking
                    avatarController.setLipSyncActive(true)
                    captureJob?.cancel() // STOP LISTENING to prevent echo/conflict
                }
                audioPlaybackManager.playChunk(pcmData)
            }
        }"""

new_audio_block = """        // Listen for AI Audio
        viewModelScope.launch {
            geminiLiveManager.audioFlow.collect { pcmData ->
                if (_engineState.value != VoiceState.Speaking) {
                    _engineState.value = VoiceState.Speaking
                    avatarController.setLipSyncActive(true)
                    captureJob?.cancel() // STOP LISTENING to prevent echo/conflict
                }
                // We let Android TTS handle Bengali, but Gemini might still send some audio.
                audioPlaybackManager.playChunk(pcmData)
            }
        }"""

content = content.replace(old_audio_block, new_audio_block)

with open('app/src/main/java/com/example/v2/voice/VoiceViewModel.kt', 'w') as f:
    f.write(content)
