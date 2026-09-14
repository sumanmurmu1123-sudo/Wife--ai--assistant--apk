import re

with open('app/src/main/java/com/example/v2/voice/VoiceViewModel.kt', 'r', encoding='utf-8') as f:
    content = f.read()

old_play = """                // We let Android TTS handle Bengali, but Gemini might still send some audio.
                audioPlaybackManager.playChunk(pcmData)"""

new_play = """                // We let Android TTS handle Bengali, but Gemini might still send some audio.
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
                _audioLevel.value = level"""

content = content.replace(old_play, new_play)

with open('app/src/main/java/com/example/v2/voice/VoiceViewModel.kt', 'w', encoding='utf-8') as f:
    f.write(content)
