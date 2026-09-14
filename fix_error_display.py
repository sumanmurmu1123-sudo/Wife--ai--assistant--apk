import re

with open('app/src/main/java/com/example/v2/voice/VoiceArchitecture.kt', 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace(
    'data class Error(val message: String) : VoiceState { override val displayText = "সমস্যা হয়েছে" }',
    'data class Error(val message: String) : VoiceState { override val displayText = message }'
)

with open('app/src/main/java/com/example/v2/voice/VoiceArchitecture.kt', 'w', encoding='utf-8') as f:
    f.write(content)
