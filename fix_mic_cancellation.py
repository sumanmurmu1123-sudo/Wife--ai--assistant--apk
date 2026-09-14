import re

with open('app/src/main/java/com/example/v2/voice/VoiceViewModel.kt', 'r') as f:
    content = f.read()

old_block = """            } catch (e: Exception) {
                e.printStackTrace()
                android.util.Log.e("VoiceViewModel", "Mic unavailable: ${e.message}")
                // Don't kill the connection if mic fails, maybe we can still send text actions
                if (_engineState.value == VoiceState.Listening) {
                    _engineState.value = VoiceState.Idle
                }
            }"""

new_block = """            } catch (e: kotlinx.coroutines.CancellationException) {
                // Expected when job is cancelled
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                android.util.Log.e("VoiceViewModel", "Mic unavailable: ${e.message}")
                // Don't kill the connection if mic fails, maybe we can still send text actions
                if (_engineState.value == VoiceState.Listening) {
                    _engineState.value = VoiceState.Idle
                }
            }"""

content = content.replace(old_block, new_block)

with open('app/src/main/java/com/example/v2/voice/VoiceViewModel.kt', 'w') as f:
    f.write(content)
