import re

with open('app/src/main/java/com/example/v2/voice/VoiceViewModel.kt', 'r', encoding='utf-8') as f:
    content = f.read()

old_turn = """            geminiLiveManager.turnCompleteFlow.collect {
                if (_engineState.value == VoiceState.Speaking) {
                    avatarController.setLipSyncActive(false)
                    // Return to listening
                    setState(VoiceState.Listening, "StateTransition")
                    startListeningMic()
                }
            }"""

new_turn = """            geminiLiveManager.turnCompleteFlow.collect {
                if (_engineState.value == VoiceState.Speaking || _engineState.value == VoiceState.Thinking) {
                    avatarController.setLipSyncActive(false)
                    // Return to listening
                    setState(VoiceState.Listening, "TurnCompleted")
                    startListeningMic()
                }
            }"""

content = content.replace(old_turn, new_turn)

with open('app/src/main/java/com/example/v2/voice/VoiceViewModel.kt', 'w', encoding='utf-8') as f:
    f.write(content)
