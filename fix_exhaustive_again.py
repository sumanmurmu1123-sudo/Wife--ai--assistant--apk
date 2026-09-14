import re

with open('app/src/main/java/com/example/v2/voice/VoiceViewModel.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# I will just replace the whole when expression
old_when = """        when (_engineState.value) {
            is VoiceState.Disconnected, is VoiceState.Unavailable, is VoiceState.Interrupted, is VoiceState.Error, is VoiceState.PermissionRequired -> {
                startConversation(context)
            }
            is VoiceState.Listening, is VoiceState.Thinking, is VoiceState.Speaking, is VoiceState.Connected -> {
                interruptConversation()
            }
            is VoiceState.Connecting, is VoiceState.Initializing, is VoiceState.Reconnecting -> {
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

content = content.replace(old_when, new_when)
# Let's also add an else branch just in case, this is safest.
new_when2 = """        when (_engineState.value) {
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
content = re.sub(r'when \(_engineState\.value\) \{.*?\}', new_when2, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/v2/voice/VoiceViewModel.kt', 'w', encoding='utf-8') as f:
    f.write(content)
