import re

with open('app/src/main/java/com/example/v2/voice/VoiceViewModel.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Add transition to thinking before sendClientContentMessage
old_send_text = """        viewModelScope.launch {
            geminiLiveManager.sendClientContentMessage(text)
        }"""
new_send_text = """        viewModelScope.launch {
            setState(VoiceState.Thinking, "TextCommandSent")
            geminiLiveManager.sendClientContentMessage(text)
        }"""
content = content.replace(old_send_text, new_send_text)

old_greeting = """                geminiLiveManager.sendClientContentMessage("SYSTEM TRIGGER (FIRST GREETING ENGINE): The user just opened the app. Give them a very cute, warm, and romantic first greeting based on the current time of day. Keep it brief. Do not wait for them to speak first.")
            }"""
new_greeting = """                setState(VoiceState.Thinking, "FirstGreetingTriggered")
                geminiLiveManager.sendClientContentMessage("SYSTEM TRIGGER (FIRST GREETING ENGINE): The user just opened the app. Give them a very cute, warm, and romantic first greeting based on the current time of day. Keep it brief. Do not wait for them to speak first.")
            }"""
content = content.replace(old_greeting, new_greeting)

old_action = """            viewModelScope.launch {
                geminiLiveManager.sendClientContentMessage("The user triggered the action: $actionName")
            }"""
new_action = """            viewModelScope.launch {
                setState(VoiceState.Thinking, "ActionTriggered")
                geminiLiveManager.sendClientContentMessage("The user triggered the action: $actionName")
            }"""
content = content.replace(old_action, new_action)

with open('app/src/main/java/com/example/v2/voice/VoiceViewModel.kt', 'w', encoding='utf-8') as f:
    f.write(content)

