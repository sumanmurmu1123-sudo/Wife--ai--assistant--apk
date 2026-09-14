import re

with open('app/src/main/java/com/example/v2/ai/GeminiLiveManager.kt', 'r', encoding='utf-8') as f:
    content = f.read()

old_connect = """    suspend fun connect(systemInstruction: String = "", apiKeyOverride: String? = null, dynamicTools: List<com.example.v2.core.tools.AssistantTool> = emptyList()) {
        try {"""

new_connect = """    suspend fun connect(systemInstruction: String = "", apiKeyOverride: String? = null, dynamicTools: List<com.example.v2.core.tools.AssistantTool> = emptyList()) {
        disconnect()
        try {"""

content = content.replace(old_connect, new_connect)

with open('app/src/main/java/com/example/v2/ai/GeminiLiveManager.kt', 'w', encoding='utf-8') as f:
    f.write(content)
