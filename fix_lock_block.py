import re

with open('app/src/main/java/com/example/v2/ui/lock/LockScreen.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Replace the wifeStateText block
content = re.sub(
    r'val wifeStateText = when \(voiceState\) \{.*?\}',
    """val wifeStateText = when (voiceState) {
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
            }""",
    content,
    flags=re.DOTALL
)

# Replace the technical diagnostic badge text block
content = re.sub(
    r'is VoiceState\.Error -> "Gemini: Disconnected".*?is VoiceState\.PermissionRequired -> "Gemini: Connected"',
    """is VoiceState.Disconnected, is VoiceState.Unavailable, is VoiceState.PermissionRequired, is VoiceState.Idle -> "Gemini: Disconnected"
                is VoiceState.Connecting, is VoiceState.Initializing, is VoiceState.Reconnecting -> "Gemini: Connecting"
                is VoiceState.Connected, is VoiceState.Listening, is VoiceState.Thinking, is VoiceState.Speaking -> "Gemini: Connected"
                is VoiceState.Error -> "Gemini: Error"
                is VoiceState.Interrupted -> "Gemini: Connected" """,
    content,
    flags=re.DOTALL
)

with open('app/src/main/java/com/example/v2/ui/lock/LockScreen.kt', 'w', encoding='utf-8') as f:
    f.write(content)
