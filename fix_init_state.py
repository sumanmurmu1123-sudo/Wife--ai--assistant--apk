import re

with open('app/src/main/java/com/example/v2/voice/VoiceViewModel.kt', 'r') as f:
    content = f.read()

# Fix initialization
old_init = "    private val _engineState = MutableStateFlow<VoiceState>(VoiceState.Disconnected)"
new_init = """    private val _engineState = MutableStateFlow<VoiceState>(
        if (androidx.core.content.ContextCompat.checkSelfPermission(
                application,
                android.Manifest.permission.RECORD_AUDIO
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) VoiceState.Disconnected else VoiceState.PermissionRequired
    )"""

content = content.replace(old_init, new_init)

with open('app/src/main/java/com/example/v2/voice/VoiceViewModel.kt', 'w') as f:
    f.write(content)
