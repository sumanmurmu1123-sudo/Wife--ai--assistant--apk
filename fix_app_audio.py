import re

with open('app/src/main/java/com/example/v2/ui/WifeAssistantV2App.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Make sure audioLevel is collected
if 'val audioLevel by voiceViewModel.audioLevel.collectAsState()' not in content:
    content = content.replace('val voiceState by voiceViewModel.state.collectAsState()', 
                              'val voiceState by voiceViewModel.state.collectAsState()\n        val audioLevel by voiceViewModel.audioLevel.collectAsState()')

old_nav = """                FloatingNavBar(
                    currentDestination = currentDestination,
                    onNavigate = { currentDestination = it },
                    voiceState = voiceState,
                    onMicClick = { voiceViewModel.onMicrophoneTapped(context) },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )"""

new_nav = """                FloatingNavBar(
                    currentDestination = currentDestination,
                    onNavigate = { currentDestination = it },
                    voiceState = voiceState,
                    audioLevel = audioLevel,
                    onMicClick = { voiceViewModel.onMicrophoneTapped(context) },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )"""
content = content.replace(old_nav, new_nav)

with open('app/src/main/java/com/example/v2/ui/WifeAssistantV2App.kt', 'w', encoding='utf-8') as f:
    f.write(content)
