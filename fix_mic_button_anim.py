import re

with open('app/src/main/java/com/example/v2/ui/components/MicrophoneButton.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Make sure audioLevel is in signature
old_sig = """fun MicrophoneButton(
    state: VoiceState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {"""

new_sig = """fun MicrophoneButton(
    state: VoiceState,
    audioLevel: Float = 0f,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {"""
content = content.replace(old_sig, new_sig)

# We have ringScale which is an infinite transition. Let's make it state driven based on audioLevel.
# If state is Listening or Speaking, we scale based on audioLevel (1f to 1.5f + some baseline).
old_ring = """    val ringScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (state is VoiceState.Listening || state is VoiceState.Speaking) 1.5f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RingScale"
    )
    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RingAlpha"
    )"""

new_ring = """    // Smooth the audio level for animation
    val smoothedLevel by animateFloatAsState(
        targetValue = audioLevel,
        animationSpec = tween(100, easing = LinearEasing),
        label = "AudioLevel"
    )
    
    val ringScale = if (state is VoiceState.Listening || state is VoiceState.Speaking) 1f + (smoothedLevel * 1.5f) else 1f
    
    // Continuous pulse for connecting/thinking
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )
    
    val ringAlpha = if (state is VoiceState.Connecting || state is VoiceState.Thinking) pulseAlpha else (1f - smoothedLevel).coerceIn(0.1f, 0.5f)"""

content = content.replace(old_ring, new_ring)

with open('app/src/main/java/com/example/v2/ui/components/MicrophoneButton.kt', 'w', encoding='utf-8') as f:
    f.write(content)
