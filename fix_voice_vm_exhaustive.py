import re

with open('app/src/main/java/com/example/v2/voice/VoiceViewModel.kt', 'r') as f:
    content = f.read()

old_when = """        when (_engineState.value) {
            is VoiceState.Idle, is VoiceState.Interrupted, is VoiceState.Error, is VoiceState.PermissionRequired -> {
                startConversation(context)
            }
            is VoiceState.Listening, is VoiceState.Thinking, is VoiceState.Speaking -> {
                interruptConversation()
            }
            is VoiceState.Connecting -> {
                // Do nothing
            }
        }"""

new_when = """        when (_engineState.value) {
            is VoiceState.Idle, is VoiceState.Disconnected, is VoiceState.Unavailable, is VoiceState.Interrupted, is VoiceState.Error, is VoiceState.PermissionRequired -> {
                startConversation(context)
            }
            is VoiceState.Listening, is VoiceState.Thinking, is VoiceState.Speaking, is VoiceState.Connected -> {
                interruptConversation()
            }
            is VoiceState.Connecting, is VoiceState.Initializing, is VoiceState.Reconnecting -> {
                // Do nothing
            }
        }"""

if "is VoiceState.Disconnected" not in content:
    content = content.replace(old_when, new_when)

with open('app/src/main/java/com/example/v2/voice/VoiceViewModel.kt', 'w') as f:
    f.write(content)
