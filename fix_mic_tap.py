import re

with open('app/src/main/java/com/example/v2/voice/VoiceViewModel.kt', 'r', encoding='utf-8') as f:
    content = f.read()

old_when = """        when (_engineState.value) {
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

new_when = """        when (_engineState.value) {
            is VoiceState.PermissionRequired -> {
                // Handled by UI permission request
            }
            is VoiceState.Disconnected, is VoiceState.Unavailable, is VoiceState.Error, is VoiceState.Idle, is VoiceState.Interrupted -> {
                startConversation(context)
            }
            is VoiceState.Connecting, is VoiceState.Initializing, is VoiceState.Reconnecting -> {
                // Do nothing
            }
            is VoiceState.Connected -> {
                setState(VoiceState.Listening, "UserTappedMic")
                startListeningMic()
            }
            is VoiceState.Listening -> {
                captureJob?.cancel()
                audioCaptureManager.stopCapture()
                setState(VoiceState.Connected, "UserStoppedMic")
            }
            is VoiceState.Thinking -> {
                // Do nothing
            }
            is VoiceState.Speaking -> {
                // Currently keeping speaking state, barge-in not fully safe without complex echo cancellation
                // Do nothing
            }
        }"""

content = content.replace(old_when, new_when)

with open('app/src/main/java/com/example/v2/voice/VoiceViewModel.kt', 'w', encoding='utf-8') as f:
    f.write(content)
