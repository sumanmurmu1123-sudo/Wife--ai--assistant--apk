package com.example.tools

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ToolRegistry {
    private val toolMap = HashMap<String, AssistantTool>()

    /**
     * Register a new tool
     */
    fun registerTool(tool: AssistantTool) {
        toolMap[tool.id] = tool
    }

    fun getAllTools(): List<AssistantTool> = toolMap.values.toList()

    fun getToolCount(): Int = toolMap.size
    
    fun getToolById(id: String): AssistantTool? = toolMap[id]

    /**
     * Execute tool by ID (For Gemini Function Calling)
     */
    suspend fun executeById(id: String, params: Map<String, String> = emptyMap()): ToolResult {
        return withContext(Dispatchers.Default) {
            val tool = toolMap[id]
            tool?.execute(params) ?: ToolResult(
                isSuccess = false,
                responseMessage = "Unknown tool ID: $id"
            )
        }
    }

    /**
     * Find and execute based on keyword match (For local voice commands)
     */
    suspend fun findAndExecute(command: String, params: Map<String, String> = emptyMap()): ToolResult {
        return withContext(Dispatchers.Default) {
            val normalizedCommand = command.lowercase().trim()

            // Keyword matching algorithm
            val matchedTool = toolMap.values.firstOrNull { tool ->
                tool.keywords.any { keyword -> normalizedCommand.contains(keyword.lowercase()) }
            }

            matchedTool?.execute(params) ?: ToolResult(
                isSuccess = false,
                responseMessage = "দুঃখিত সুজিত, এই কাজের জন্য কোনো টুল পাওয়া যায়নি।"
            )
        }
    }
}
