import re

with open('app/src/main/java/com/example/v2/ui/components/MicrophoneButton.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Make sure imports exist
if 'import androidx.compose.material.icons.filled.MicOff' not in content:
    content = content.replace('import androidx.compose.material.icons.filled.Mic', 
                              'import androidx.compose.material.icons.filled.Mic\nimport androidx.compose.material.icons.filled.MicOff\nimport androidx.compose.material.icons.filled.WifiOff\nimport androidx.compose.material.icons.filled.Sync\nimport androidx.compose.material.icons.filled.Link\nimport androidx.compose.material.icons.filled.ErrorOutline')

old_icons = """                when (state) {
                    is VoiceState.Idle, is VoiceState.Connecting, is VoiceState.Connected, is VoiceState.Initializing, is VoiceState.Reconnecting, is VoiceState.Interrupted, is VoiceState.Error, is VoiceState.Disconnected, is VoiceState.Unavailable, is VoiceState.PermissionRequired -> {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Tap to talk",
                            tint = Color.White,
                            modifier = iconModifier.size(32.dp)
                        )
                    }
                    is VoiceState.Listening, is VoiceState.Speaking -> {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Audio Waveform",
                            tint = Cyan,
                            modifier = iconModifier.size(36.dp)
                        )
                    }
                    is VoiceState.Thinking -> {
                        Icon(
                            imageVector = Icons.Default.Autorenew,
                            contentDescription = "Thinking",
                            tint = Violet,
                            modifier = iconModifier.size(32.dp)
                        )
                    }
                }"""

new_icons = """                when (state) {
                    is VoiceState.PermissionRequired -> {
                        Icon(imageVector = Icons.Default.MicOff, contentDescription = "Permission Required", tint = Color.White, modifier = iconModifier.size(32.dp))
                    }
                    is VoiceState.Disconnected, is VoiceState.Unavailable, is VoiceState.Idle, is VoiceState.Interrupted -> {
                        Icon(imageVector = Icons.Default.WifiOff, contentDescription = "Disconnected", tint = Color.White, modifier = iconModifier.size(32.dp))
                    }
                    is VoiceState.Connecting, is VoiceState.Initializing, is VoiceState.Reconnecting -> {
                        Icon(imageVector = Icons.Default.Sync, contentDescription = "Connecting", tint = Color.White, modifier = iconModifier.size(32.dp))
                    }
                    is VoiceState.Connected -> {
                        Icon(imageVector = Icons.Default.Link, contentDescription = "Connected", tint = Color.White, modifier = iconModifier.size(32.dp))
                    }
                    is VoiceState.Listening -> {
                        Icon(imageVector = Icons.Default.Mic, contentDescription = "Listening", tint = Cyan, modifier = iconModifier.size(36.dp))
                    }
                    is VoiceState.Thinking -> {
                        Icon(imageVector = Icons.Default.Autorenew, contentDescription = "Thinking", tint = Violet, modifier = iconModifier.size(32.dp))
                    }
                    is VoiceState.Speaking -> {
                        Icon(imageVector = Icons.Default.GraphicEq, contentDescription = "Speaking", tint = Cyan, modifier = iconModifier.size(36.dp))
                    }
                    is VoiceState.Error -> {
                        Icon(imageVector = Icons.Default.ErrorOutline, contentDescription = "Error", tint = NeonPink, modifier = iconModifier.size(32.dp))
                    }
                }"""

content = content.replace(old_icons, new_icons)

with open('app/src/main/java/com/example/v2/ui/components/MicrophoneButton.kt', 'w', encoding='utf-8') as f:
    f.write(content)
