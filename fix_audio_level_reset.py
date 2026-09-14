import re

with open('app/src/main/java/com/example/v2/voice/VoiceViewModel.kt', 'r', encoding='utf-8') as f:
    content = f.read()

old_set_state = """    private fun setState(newState: VoiceState, reason: String) {
        val oldState = _engineState.value
        if (oldState != newState) {
            val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", java.util.Locale.getDefault()).format(java.util.Date())
            android.util.Log.i("VoiceStateMachine", "[$timestamp] ${oldState.javaClass.simpleName} -> ${newState.javaClass.simpleName} | reason=$reason")
            _engineState.value = newState
        }
    }"""

new_set_state = """    private fun setState(newState: VoiceState, reason: String) {
        val oldState = _engineState.value
        if (oldState != newState) {
            val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", java.util.Locale.getDefault()).format(java.util.Date())
            android.util.Log.i("VoiceStateMachine", "[$timestamp] ${oldState.javaClass.simpleName} -> ${newState.javaClass.simpleName} | reason=$reason")
            _engineState.value = newState
            if (newState !is VoiceState.Listening && newState !is VoiceState.Speaking) {
                _audioLevel.value = 0f
            }
        }
    }"""

content = content.replace(old_set_state, new_set_state)

with open('app/src/main/java/com/example/v2/voice/VoiceViewModel.kt', 'w', encoding='utf-8') as f:
    f.write(content)
