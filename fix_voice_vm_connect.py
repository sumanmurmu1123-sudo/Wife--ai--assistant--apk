import re

with open('app/src/main/java/com/example/v2/voice/VoiceViewModel.kt', 'r') as f:
    content = f.read()

old_code = """            geminiLiveManager.connect(
                systemInstruction = systemInstruction,
                apiKeyOverride = apiKeyOverride,
                dynamicTools = toolRegistry.getAllTools()
            )
            
            _engineState.value = VoiceState.Listening
            avatarController.playAnimation(AvatarAnimation.LISTENING)
            
            // Start capturing mic audio and sending to Gemini
            startListeningMic()
        }"""

new_code = """            geminiLiveManager.connect(
                systemInstruction = systemInstruction,
                apiKeyOverride = apiKeyOverride,
                dynamicTools = toolRegistry.getAllTools()
            )
            
            // We wait for SetupComplete event in the init block before listening
        }"""

if "We wait for SetupComplete event in the init block before listening" not in content:
    content = content.replace(old_code, new_code)

with open('app/src/main/java/com/example/v2/voice/VoiceViewModel.kt', 'w') as f:
    f.write(content)
