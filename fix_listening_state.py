import re

with open('app/src/main/java/com/example/v2/voice/VoiceViewModel.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Remove early transition in onMicrophoneTapped
old_tap = """            is VoiceState.Connected -> {
                setState(VoiceState.Listening, "UserTappedMic")
                startListeningMic()
            }"""

new_tap = """            is VoiceState.Connected -> {
                startListeningMic()
            }"""
content = content.replace(old_tap, new_tap)

# Remove early transition in turnCompleteFlow
old_turn = """                    // Return to listening
                    setState(VoiceState.Listening, "TurnCompleted")
                    startListeningMic()"""

new_turn = """                    // Return to listening
                    startListeningMic()"""
content = content.replace(old_turn, new_turn)

# Remove early transition in setupCompleteFlow
old_setup = """                    // Proceed to listening
                    setState(VoiceState.Listening, "ReadyToListen")
                    avatarController.playAnimation(AvatarAnimation.LISTENING)
                    startListeningMic()"""

new_setup = """                    // Wait for user to start listening, or we can auto-start
                    // The prompt allows going to Connected. Let's just stay in Connected until they tap.
                    // Or actually, if we want auto-listen on start, we just call startListeningMic()
                    // Let's call startListeningMic() and let IT set the state when frames arrive.
                    startListeningMic()"""
content = content.replace(old_setup, new_setup)

# Update startListeningMic to set Listening on first frame
old_capture = """                audioCaptureManager.startCapture().collect { pcmData ->
                    if (_engineState.value == VoiceState.Listening) {
                        geminiLiveManager.sendAudioChunk(pcmData)"""

new_capture = """                var isFirstFrame = true
                audioCaptureManager.startCapture().collect { pcmData ->
                    if (isFirstFrame) {
                        setState(VoiceState.Listening, "AudioFramesDetected")
                        avatarController.playAnimation(AvatarAnimation.LISTENING)
                        isFirstFrame = false
                    }
                    
                    if (_engineState.value == VoiceState.Listening) {
                        geminiLiveManager.sendAudioChunk(pcmData)"""
content = content.replace(old_capture, new_capture)

with open('app/src/main/java/com/example/v2/voice/VoiceViewModel.kt', 'w', encoding='utf-8') as f:
    f.write(content)
