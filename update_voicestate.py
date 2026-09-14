import re

with open('app/src/main/java/com/example/v2/voice/VoiceArchitecture.kt', 'r', encoding='utf-8') as f:
    content = f.read()

new_sealed = """sealed interface VoiceState {
    val displayText: String

    data object Idle : VoiceState { override val displayText = "প্রস্তুত" }
    data object PermissionRequired : VoiceState { override val displayText = "মাইক্রোফোন অনুমতি প্রয়োজন" }
    data object Initializing : VoiceState { override val displayText = "সংযোগ হচ্ছে…" }
    data object Connecting : VoiceState { override val displayText = "সংযোগ হচ্ছে…" }
    data object Connected : VoiceState { override val displayText = "সংযুক্ত" }
    data object Listening : VoiceState { override val displayText = "শুনছি… 🎙️" }
    data object Thinking : VoiceState { override val displayText = "ভাবছি… 🧠" }
    data object Speaking : VoiceState { override val displayText = "কথা বলছি… 💕" }
    data object Interrupted : VoiceState { override val displayText = "প্রস্তুত" }
    data object Reconnecting : VoiceState { override val displayText = "আবার সংযোগ হচ্ছে…" }
    data object Disconnected : VoiceState { override val displayText = "সংযোগ বিচ্ছিন্ন" }
    data class Error(val message: String) : VoiceState { override val displayText = "সমস্যা হয়েছে" }
    data object Unavailable : VoiceState { override val displayText = "ভয়েস উপলব্ধ নেই" }
}"""

content = re.sub(r'sealed interface VoiceState \{.*?\}', new_sealed, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/v2/voice/VoiceArchitecture.kt', 'w', encoding='utf-8') as f:
    f.write(content)
