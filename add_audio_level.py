import re

with open('app/src/main/java/com/example/v2/voice/VoiceViewModel.kt', 'r', encoding='utf-8') as f:
    content = f.read()

if 'val audioLevel: StateFlow<Float>' not in content:
    # Add audioLevel state flow
    state_decl = "    val state: StateFlow<VoiceState> = _engineState.asStateFlow()"
    new_state_decl = """    val state: StateFlow<VoiceState> = _engineState.asStateFlow()
    
    private val _audioLevel = kotlinx.coroutines.flow.MutableStateFlow(0f)
    val audioLevel: kotlinx.coroutines.flow.StateFlow<Float> = _audioLevel.asStateFlow()"""
    
    content = content.replace(state_decl, new_state_decl)
    
    # Calculate audio level in startListeningMic
    old_capture = """                audioCaptureManager.startCapture().collect { pcmData ->
                    if (_engineState.value == VoiceState.Listening) {
                        geminiLiveManager.sendAudioChunk(pcmData)
                    }
                }"""
    
    new_capture = """                audioCaptureManager.startCapture().collect { pcmData ->
                    if (_engineState.value == VoiceState.Listening) {
                        geminiLiveManager.sendAudioChunk(pcmData)
                        
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
                        // Normalize roughly (max short is 32768)
                        val level = (rms / 32768.0).toFloat().coerceIn(0f, 1f)
                        _audioLevel.value = level
                    } else {
                        _audioLevel.value = 0f
                    }
                }"""
    
    content = content.replace(old_capture, new_capture)

with open('app/src/main/java/com/example/v2/voice/VoiceViewModel.kt', 'w', encoding='utf-8') as f:
    f.write(content)

