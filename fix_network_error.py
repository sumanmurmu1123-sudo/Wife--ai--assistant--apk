import re

with open('app/src/main/java/com/example/v2/ai/GeminiLiveManager.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Replace emitting raw error strings with Bengali messages
content = content.replace(
    '                _errorFlow.emit(e.message ?: "Connection error")',
    '                _errorFlow.emit("ইন্টারনেট সংযোগ সমস্যা বা সার্ভার ত্রুটি") // Network/Server error'
)

with open('app/src/main/java/com/example/v2/ai/GeminiLiveManager.kt', 'w', encoding='utf-8') as f:
    f.write(content)
