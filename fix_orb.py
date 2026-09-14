import re

with open('app/src/main/java/com/example/v2/voice/VoiceViewModel.kt', 'r', encoding='utf-8') as f:
    content = f.read()

old_err = """                if (_engineState.value == VoiceState.Listening) {
                    setState(VoiceState.Error("মাইক্রোফোন উপলব্ধ নেই"), "MicInitializationFailed")
                }"""
new_err = """                if (_engineState.value == VoiceState.Listening) {
                    setState(VoiceState.Error("মাইক্রোফোন উপলব্ধ নেই"), "MicInitializationFailed")
                    avatarController.playAnimation(AvatarAnimation.IDLE)
                }"""

content = content.replace(old_err, new_err)

with open('app/src/main/java/com/example/v2/voice/VoiceViewModel.kt', 'w', encoding='utf-8') as f:
    f.write(content)
