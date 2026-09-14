import re

with open('app/src/main/java/com/example/v2/voice/VoiceViewModel.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# I replaced Idle with Disconnected but in when clause we still need Idle maybe? 
# Wait, let's just add 'is VoiceState.Idle' to the Disconnected list in VoiceViewModel when expression.

old_when = """        when (_engineState.value) {
            is VoiceState.Disconnected, is VoiceState.Unavailable, is VoiceState.Interrupted, is VoiceState.Error, is VoiceState.PermissionRequired -> {
                startConversation(context)
            }"""

new_when = """        when (_engineState.value) {
            is VoiceState.Idle, is VoiceState.Disconnected, is VoiceState.Unavailable, is VoiceState.Interrupted, is VoiceState.Error, is VoiceState.PermissionRequired -> {
                startConversation(context)
            }"""

content = content.replace(old_when, new_when)

with open('app/src/main/java/com/example/v2/voice/VoiceViewModel.kt', 'w', encoding='utf-8') as f:
    f.write(content)

