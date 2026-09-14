import re

with open('app/src/main/java/com/example/v2/voice/VoiceViewModel.kt', 'r') as f:
    lines = f.readlines()

out_lines = []
in_class = False
for i, line in enumerate(lines):
    if "class VoiceViewModel" in line:
        in_class = True
    
    if "_engineState.value = VoiceState" in line:
        # We need to replace this with setState
        state_match = re.search(r'_engineState\.value = (VoiceState\.[A-Za-z0-9_]+(?:\([^)]*\))?)', line)
        if state_match:
            state_val = state_match.group(1)
            indent = line[:len(line) - len(line.lstrip())]
            # Try to infer a reason
            reason = "StateTransition"
            out_lines.append(f'{indent}setState({state_val}, "{reason}")\n')
            continue

    if "private val _engineState = MutableStateFlow<VoiceState>(VoiceState.Idle)" in line:
        out_lines.append('    private val _engineState = MutableStateFlow<VoiceState>(VoiceState.Disconnected)\n')
        continue

    out_lines.append(line)
    
    if "private var playbackJob: Job? = null" in line:
        out_lines.append("""
    private fun setState(newState: VoiceState, reason: String) {
        val oldState = _engineState.value
        if (oldState != newState) {
            val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", java.util.Locale.getDefault()).format(java.util.Date())
            android.util.Log.i("VoiceStateMachine", "[$timestamp] ${oldState.javaClass.simpleName} -> ${newState.javaClass.simpleName} | reason=$reason")
            _engineState.value = newState
        }
    }
""")

with open('app/src/main/java/com/example/v2/voice/VoiceViewModel.kt', 'w') as f:
    f.writelines(out_lines)

