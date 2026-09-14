import re

with open('app/src/main/java/com/example/v2/ui/lock/LockScreen.kt', 'r') as f:
    content = f.read()

old_text = """            val wifeStateText = when (voiceState) {
                is VoiceState.Idle, is VoiceState.Disconnected, is VoiceState.Unavailable, is VoiceState.PermissionRequired -> "প্রস্তুত"
                is VoiceState.Listening -> "শুনছি… \uD83C\uDF99\uFE0F"
                is VoiceState.Thinking -> "ভাবছি… \uD83E\uDDE0"
                is VoiceState.Speaking -> "কথা বলছি…"
                is VoiceState.Connecting, is VoiceState.Connected, is VoiceState.Initializing, is VoiceState.Reconnecting -> "সংযোগ হচ্ছে…"
                is VoiceState.Error -> "সংযোগ বিচ্ছিন্ন"
                is VoiceState.Interrupted -> "প্রস্তুত"
                else -> "অজানা"
            }"""

new_text = """            val wifeStateText = when (voiceState) {
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

# The python script might not match exactly if there are unicode differences in my sed output vs the file. Let's just do a regex replace.
