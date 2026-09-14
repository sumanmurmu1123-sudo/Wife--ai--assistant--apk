import re

with open('app/src/main/java/com/example/v2/voice/VoiceViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace("VoiceState.Idle", "VoiceState.Disconnected")

with open('app/src/main/java/com/example/v2/voice/VoiceViewModel.kt', 'w') as f:
    f.write(content)

