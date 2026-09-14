import re

with open('app/src/main/java/com/example/v2/ui/WifeAssistantV2App.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Make sure we observe voiceState in WifeAssistantV2App
if 'val voiceState by voiceViewModel.state.collectAsState()' not in content:
    content = content.replace('val voiceViewModel: VoiceViewModel = viewModel()', 
                              'val voiceViewModel: VoiceViewModel = viewModel()\n        val voiceState by voiceViewModel.state.collectAsState()\n        val context = androidx.compose.ui.platform.LocalContext.current')

old_nav = """            // Floating Bottom Navigation
            if (currentDestination != NavDestination.LOCK_SCREEN) {
                FloatingNavBar(
                    currentDestination = currentDestination,
                    onNavigate = { currentDestination = it },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }"""

new_nav = """            // Floating Bottom Navigation
            if (currentDestination != NavDestination.LOCK_SCREEN) {
                FloatingNavBar(
                    currentDestination = currentDestination,
                    onNavigate = { currentDestination = it },
                    voiceState = voiceState,
                    onMicClick = { voiceViewModel.onMicrophoneTapped(context) },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }"""

content = content.replace(old_nav, new_nav)

with open('app/src/main/java/com/example/v2/ui/WifeAssistantV2App.kt', 'w', encoding='utf-8') as f:
    f.write(content)
