import re

with open('app/src/main/java/com/example/v2/ui/components/MicrophoneButton.kt', 'r') as f:
    content = f.read()

old_text = """        val statusText = when (state) {
            is VoiceState.Idle, is VoiceState.Disconnected, is VoiceState.Unavailable, is VoiceState.PermissionRequired -> "Tap to talk"
            is VoiceState.Connecting, is VoiceState.Connected, is VoiceState.Initializing, is VoiceState.Reconnecting -> "Connecting…"
            is VoiceState.Listening -> "I'm listening…"
            is VoiceState.Thinking -> "Let me think…"
            is VoiceState.Speaking -> "Speaking…"
            is VoiceState.Interrupted -> "Interrupted"
            is VoiceState.Error -> state.message
        }"""

new_text = """        val statusText = when (state) {
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
        }"""

if "প্রস্তুত" not in content:
    content = content.replace(old_text, new_text)

with open('app/src/main/java/com/example/v2/ui/components/MicrophoneButton.kt', 'w', encoding="utf-8") as f:
    f.write(content)
