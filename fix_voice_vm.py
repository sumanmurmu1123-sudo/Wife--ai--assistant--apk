import re

with open('app/src/main/java/com/example/v2/voice/VoiceViewModel.kt', 'r') as f:
    content = f.read()

# Make connectGeminiLive listen to setup complete
old_connect = """        geminiLiveManager.connect(
            systemInstruction = systemInstruction,
            apiKeyOverride = apiKeyOverride,
            dynamicTools = toolRegistry.getAllTools()
        )

        _engineState.value = VoiceState.Listening
        avatarController.playAnimation(AvatarAnimation.LISTENING)

        // Start capturing mic audio and sending to Gemini
        startListeningMic()
    }"""
new_connect = """        geminiLiveManager.connect(
            systemInstruction = systemInstruction,
            apiKeyOverride = apiKeyOverride,
            dynamicTools = toolRegistry.getAllTools()
        )

        // Wait for setup complete before listening
    }"""
if "Wait for setup complete before listening" not in content:
    content = content.replace(old_connect, new_connect)

# Listen to setupCompleteFlow in init block
old_init_tools = """        toolRegistry.register(VolumeTool(application))"""
new_init_tools = """        toolRegistry.register(VolumeTool(application))
        
        // Listen for Setup Complete
        viewModelScope.launch {
            geminiLiveManager.setupCompleteFlow.collect {
                if (_engineState.value == VoiceState.Connecting) {
                    _engineState.value = VoiceState.Connected
                    avatarController.playAnimation(AvatarAnimation.IDLE)
                    
                    // Proceed to listening
                    _engineState.value = VoiceState.Listening
                    avatarController.playAnimation(AvatarAnimation.LISTENING)
                    startListeningMic()
                }
            }
        }"""
if "Listen for Setup Complete" not in content:
    content = content.replace(old_init_tools, new_init_tools)

with open('app/src/main/java/com/example/v2/voice/VoiceViewModel.kt', 'w') as f:
    f.write(content)
