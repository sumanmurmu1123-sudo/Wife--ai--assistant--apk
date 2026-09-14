import re

with open('app/src/main/java/com/example/v2/voice/VoiceViewModel.kt', 'r') as f:
    content = f.read()

old_code = """    fun onMicrophoneTapped(context: android.content.Context) {
        when (_engineState.value) {
            is VoiceState.Idle, is VoiceState.Interrupted, is VoiceState.Error -> {
                startConversation(context)
            }"""

new_code = """    fun onMicrophoneTapped(context: android.content.Context) {
        // Master rule: Check Permission before any action
        val hasMicPermission = androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        
        if (!hasMicPermission) {
            _engineState.value = VoiceState.PermissionRequired
            return
        }

        when (_engineState.value) {
            is VoiceState.Idle, is VoiceState.Interrupted, is VoiceState.Error, is VoiceState.PermissionRequired -> {
                startConversation(context)
            }"""

if "Master rule: Check Permission" not in content:
    content = content.replace(old_code, new_code)

with open('app/src/main/java/com/example/v2/voice/VoiceViewModel.kt', 'w') as f:
    f.write(content)
