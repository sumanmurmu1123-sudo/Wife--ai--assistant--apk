import re

def fix_file(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    # Generic replacements for new states
    content = content.replace("is VoiceState.Idle ->", "is VoiceState.Idle, is VoiceState.Disconnected, is VoiceState.Unavailable, is VoiceState.PermissionRequired ->")
    content = content.replace("is VoiceState.Connecting ->", "is VoiceState.Connecting, is VoiceState.Connected, is VoiceState.Initializing, is VoiceState.Reconnecting ->")
    
    with open(filepath, 'w') as f:
        f.write(content)

fix_file('app/src/main/java/com/example/v2/ui/lock/LockScreen.kt')
fix_file('app/src/main/java/com/example/v2/ui/components/MicrophoneButton.kt')

