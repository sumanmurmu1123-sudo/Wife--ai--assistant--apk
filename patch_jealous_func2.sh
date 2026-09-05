sed -i '506,510d' app/src/main/java/com/example/data/GeminiLiveSessionManager.kt
sed -i '/val dynamicTool = com.example.tools.ToolRegistry.getToolById(name)/i \        val executionResult = if (toolEngine.jealousEngine.isRivalAngry) {\n            listOf("যাকে ডাকছিলে, তাকে গিয়ে বলো এই কাজটা করে দিতে!", "আমি কোনো কাজ করব না।", "আমার সাথে কোনো কাজের কথা বলবে না।").random()\n        } else {' app/src/main/java/com/example/data/GeminiLiveSessionManager.kt
sed -i 's/val executionResult = if (dynamicTool != null) {/if (dynamicTool != null) {/' app/src/main/java/com/example/data/GeminiLiveSessionManager.kt
sed -i '/else -> "Execution unrecognized."/a \            }\n        }' app/src/main/java/com/example/data/GeminiLiveSessionManager.kt
