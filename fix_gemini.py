import re

with open('app/src/main/java/com/example/v2/ai/GeminiLiveManager.kt', 'r') as f:
    content = f.read()

old_decl = """    private val _errorFlow = MutableSharedFlow<String>()
    val errorFlow: SharedFlow<String> = _errorFlow"""

new_decl = """    private val _errorFlow = MutableSharedFlow<String>()
    val errorFlow: SharedFlow<String> = _errorFlow

    private val _setupCompleteFlow = MutableSharedFlow<Unit>()
    val setupCompleteFlow: SharedFlow<Unit> = _setupCompleteFlow"""

if "val setupCompleteFlow" not in content:
    content = content.replace(old_decl, new_decl)

with open('app/src/main/java/com/example/v2/ai/GeminiLiveManager.kt', 'w') as f:
    f.write(content)
