import re

with open("app/src/main/java/com/example/v2/voice/VoiceViewModel.kt", "r") as f:
    content = f.read()

new_func = """    private fun startListeningMic() {
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
                        
                        // Interrupt Gemini natively if possible, or just send a client content update (Gemini Live handles this natively when we send new audio)
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
    }"""

pattern = r"    private fun startListeningMic\(\) \{.*?(?=    private fun startVoiceService\(\) \{)"
result = re.sub(pattern, new_func + "\n\n", content, flags=re.DOTALL)

with open("app/src/main/java/com/example/v2/voice/VoiceViewModel.kt", "w") as f:
    f.write(result)
