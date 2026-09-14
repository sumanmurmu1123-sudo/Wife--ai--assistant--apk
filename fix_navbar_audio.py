import re

with open('app/src/main/java/com/example/v2/ui/components/FloatingNavBar.kt', 'r', encoding='utf-8') as f:
    content = f.read()

old_sig = """fun FloatingNavBar(
    currentDestination: NavDestination,
    onNavigate: (NavDestination) -> Unit,
    voiceState: VoiceState,
    onMicClick: () -> Unit,
    modifier: Modifier = Modifier
) {"""

new_sig = """fun FloatingNavBar(
    currentDestination: NavDestination,
    onNavigate: (NavDestination) -> Unit,
    voiceState: VoiceState,
    audioLevel: Float = 0f,
    onMicClick: () -> Unit,
    modifier: Modifier = Modifier
) {"""
content = content.replace(old_sig, new_sig)

old_mic = """            MicrophoneButton(
                state = voiceState,
                onClick = onMicClick
            )"""

new_mic = """            MicrophoneButton(
                state = voiceState,
                audioLevel = audioLevel,
                onClick = onMicClick
            )"""
content = content.replace(old_mic, new_mic)

with open('app/src/main/java/com/example/v2/ui/components/FloatingNavBar.kt', 'w', encoding='utf-8') as f:
    f.write(content)
