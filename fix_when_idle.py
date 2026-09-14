import re

with open('app/src/main/java/com/example/v2/voice/VoiceViewModel.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# I will just replace the specific line
old_line = "is VoiceState.Disconnected, is VoiceState.Disconnected, is VoiceState.Unavailable, is VoiceState.Interrupted, is VoiceState.Error, is VoiceState.PermissionRequired -> {"
new_line = "is VoiceState.Idle, is VoiceState.Disconnected, is VoiceState.Unavailable, is VoiceState.Interrupted, is VoiceState.Error, is VoiceState.PermissionRequired -> {"

if old_line in content:
    content = content.replace(old_line, new_line)
else:
    # Maybe only one Disconnected?
    old_line_2 = "is VoiceState.Disconnected, is VoiceState.Unavailable, is VoiceState.Interrupted, is VoiceState.Error, is VoiceState.PermissionRequired -> {"
    content = content.replace(old_line_2, new_line)

with open('app/src/main/java/com/example/v2/voice/VoiceViewModel.kt', 'w', encoding='utf-8') as f:
    f.write(content)
