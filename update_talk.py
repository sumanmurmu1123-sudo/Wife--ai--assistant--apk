import re

with open('app/src/main/java/com/example/v2/ui/talk/WifeAssistantV2Talk.kt', 'r', encoding='utf-8') as f:
    content = f.read()

old_status = """                val statusText = when (voiceState) {
                    is com.example.v2.voice.VoiceState.Listening -> "Listening..."
                    is com.example.v2.voice.VoiceState.Thinking -> "Thinking..."
                    is com.example.v2.voice.VoiceState.Speaking -> "Speaking..."
                    is com.example.v2.voice.VoiceState.Connecting -> "Connecting..."
                    is com.example.v2.voice.VoiceState.Error -> "Connection error"
                    else -> "Ready"
                }
                androidx.compose.material3.Text(
                    text = statusText,"""

new_status = """                androidx.compose.material3.Text(
                    text = voiceState.displayText,"""

content = content.replace(old_status, new_status)

with open('app/src/main/java/com/example/v2/ui/talk/WifeAssistantV2Talk.kt', 'w', encoding='utf-8') as f:
    f.write(content)
