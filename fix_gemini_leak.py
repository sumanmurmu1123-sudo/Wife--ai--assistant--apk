import re

with open('app/src/main/java/com/example/v2/ai/GeminiLiveManager.kt', 'r') as f:
    content = f.read()

old_connect = """    suspend fun connect(systemInstruction: String, apiKeyOverride: String?, dynamicTools: List<AssistantTool> = emptyList()) {
        try {"""

new_connect = """    suspend fun connect(systemInstruction: String, apiKeyOverride: String?, dynamicTools: List<AssistantTool> = emptyList()) {
        disconnect() // Ensure previous session is closed
        try {"""

if "disconnect() // Ensure previous session is closed" not in content:
    content = content.replace(old_connect, new_connect)

with open('app/src/main/java/com/example/v2/ai/GeminiLiveManager.kt', 'w') as f:
    f.write(content)
