import re

with open('app/src/main/java/com/example/v2/ui/components/FloatingNavBar.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Add VoiceState import
if 'import com.example.v2.voice.VoiceState' not in content:
    content = content.replace('import androidx.compose.ui.unit.sp', 'import androidx.compose.ui.unit.sp\nimport com.example.v2.voice.VoiceState')

old_sig = """fun FloatingNavBar(
    currentDestination: NavDestination,
    onNavigate: (NavDestination) -> Unit,
    modifier: Modifier = Modifier
) {"""

new_sig = """fun FloatingNavBar(
    currentDestination: NavDestination,
    onNavigate: (NavDestination) -> Unit,
    voiceState: VoiceState,
    onMicClick: () -> Unit,
    modifier: Modifier = Modifier
) {"""

content = content.replace(old_sig, new_sig)

old_center_button = """        // Center Talk Button (Floating above)
        Box(
            modifier = Modifier
                .offset(y = (-16).dp)
                .size(64.dp)
                .shadow(8.dp, CircleShape, spotColor = NeonPink)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(listOf(Color(0xFF2A2035), Color(0xFF100A1A)))
                )
                .border(2.dp, Brush.sweepGradient(listOf(NeonPink, Cyan, NeonPink)), CircleShape)
                .clickable { onNavigate(NavDestination.TALK) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Talk",
                tint = if (currentDestination == NavDestination.TALK) NeonPink else Color.White,
                modifier = Modifier.size(28.dp)
            )
        }"""

new_center_button = """        // Center Talk Button (Floating above)
        Box(
            modifier = Modifier
                .offset(y = (-32).dp),
            contentAlignment = Alignment.Center
        ) {
            MicrophoneButton(
                state = voiceState,
                onClick = onMicClick
            )
        }"""

content = content.replace(old_center_button, new_center_button)

with open('app/src/main/java/com/example/v2/ui/components/FloatingNavBar.kt', 'w', encoding='utf-8') as f:
    f.write(content)
