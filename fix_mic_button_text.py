import re

with open('app/src/main/java/com/example/v2/ui/components/MicrophoneButton.kt', 'r', encoding='utf-8') as f:
    content = f.read()

old_status = """        val statusText = when (state) {
            is VoiceState.Idle -> "প্রস্তুত"
            is VoiceState.PermissionRequired -> "মাইক্রোফোন অনুমতি প্রয়োজন"
            is VoiceState.Unavailable -> "ভয়েস উপলব্ধ নেই"
            is VoiceState.Disconnected -> "সংযোগ বিচ্ছিন্ন"
            is VoiceState.Initializing, is VoiceState.Connecting -> "সংযোগ হচ্ছে…"
            is VoiceState.Connected -> "সংযুক্ত"
            is VoiceState.Reconnecting -> "আবার সংযোগ হচ্ছে…"
            is VoiceState.Listening -> "শুনছি… 🎙️"
            is VoiceState.Thinking -> "ভাবছি… 🧠"
            is VoiceState.Speaking -> "কথা বলছি… 💕"
            is VoiceState.Interrupted -> "প্রস্তুত"
            is VoiceState.Error -> "সমস্যা হয়েছে"
        }

        Text(
            text = statusText,"""

new_status = """        Text(
            text = state.displayText,"""

content = content.replace(old_status, new_status)

with open('app/src/main/java/com/example/v2/ui/components/MicrophoneButton.kt', 'w', encoding='utf-8') as f:
    f.write(content)
