import re

with open('app/src/main/java/com/example/v2/voice/VoiceViewModel.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Fix mic failure
old_mic_fail = """                if (_engineState.value == VoiceState.Listening) {
                    setState(VoiceState.Disconnected, "StateTransition")
                }"""
new_mic_fail = """                if (_engineState.value == VoiceState.Listening) {
                    setState(VoiceState.Error("মাইক্রোফোন উপলব্ধ নেই"), "MicInitializationFailed")
                }"""
content = content.replace(old_mic_fail, new_mic_fail)

# General transitions
content = content.replace('setState(VoiceState.Listening, "StateTransition")', 'setState(VoiceState.Listening, "ReadyToListen")')
content = content.replace('setState(VoiceState.Connected, "StateTransition")', 'setState(VoiceState.Connected, "SetupComplete")')
content = content.replace('setState(VoiceState.Speaking, "StateTransition")', 'setState(VoiceState.Speaking, "AudioChunkReceived")')
content = content.replace('setState(VoiceState.Disconnected, "StateTransition")', 'setState(VoiceState.Disconnected, "UserInterruptedOrIdle")')
content = content.replace('setState(VoiceState.Error(errorMsg), "StateTransition")', 'setState(VoiceState.Error(errorMsg), "ConnectionError")')
content = content.replace('setState(VoiceState.PermissionRequired, "StateTransition")', 'setState(VoiceState.PermissionRequired, "PermissionDenied")')
content = content.replace('setState(VoiceState.Connecting, "StateTransition")', 'setState(VoiceState.Connecting, "ConnectingToGemini")')
content = content.replace('setState(VoiceState.Interrupted, "StateTransition")', 'setState(VoiceState.Disconnected, "UserInterrupted")')

with open('app/src/main/java/com/example/v2/voice/VoiceViewModel.kt', 'w', encoding='utf-8') as f:
    f.write(content)

