import re

with open('app/src/main/java/com/example/v2/voice/VoiceViewModel.kt', 'r', encoding='utf-8') as f:
    content = f.read()

old_catch = """            } catch (e: Exception) {
                e.printStackTrace()
                android.util.Log.e("VoiceViewModel", "Mic unavailable: ${e.message}")
                if (_engineState.value == VoiceState.Listening) {
                    setState(VoiceState.Error("মাইক্রোফোন উপলব্ধ নেই"), "MicInitializationFailed")
                    avatarController.playAnimation(AvatarAnimation.IDLE)
                }
            }"""

new_catch = """            } catch (e: Exception) {
                e.printStackTrace()
                android.util.Log.e("VoiceViewModel", "Mic unavailable: ${e.message}")
                // Always show error if mic fails, regardless of current state
                setState(VoiceState.Error("মাইক্রোফোন চালু করা যাচ্ছে না"), "MicInitializationFailed")
                avatarController.playAnimation(AvatarAnimation.IDLE)
            }"""
content = content.replace(old_catch, new_catch)

with open('app/src/main/java/com/example/v2/voice/VoiceViewModel.kt', 'w', encoding='utf-8') as f:
    f.write(content)
