import re

with open('app/src/main/java/com/example/v2/ui/talk/WifeAssistantV2Talk.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Remove the duplicate MicrophoneButton
old_mic = """            MicrophoneButton(
                state = voiceState,
                onClick = { viewModel.onMicrophoneTapped(context) }
            )
            
            Spacer(modifier = Modifier.height(24.dp))"""

if old_mic in content:
    content = content.replace(old_mic, "")

# The bottom padding might be an issue if it collides with the navbar, but we leave the text field for now
with open('app/src/main/java/com/example/v2/ui/talk/WifeAssistantV2Talk.kt', 'w', encoding='utf-8') as f:
    f.write(content)
