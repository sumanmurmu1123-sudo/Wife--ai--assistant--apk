package com.example.v2.core.tools.impl

import android.content.Context
import com.example.v2.core.WifeAssistantCore
import com.example.v2.core.tools.AssistantTool
import com.example.v2.core.tools.ToolCategory
import com.example.v2.core.tools.ToolResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MemorySaveTool(private val context: Context) : AssistantTool {
    override val id = "memory.save"
    override val name = "Save to Memory"
    override val description = "Saves an important fact, preference, or context to the assistant's long-term memory."
    override val category = ToolCategory.AI
    override val keywords = listOf("remember", "save", "memory", "fact")
    override val requiredPermissions = emptySet<String>()
    
    override val parametersSchema = mapOf(
        "type" to "object",
        "properties" to mapOf(
            "content" to mapOf("type" to "string", "description" to "The fact or preference to remember"),
            "category" to mapOf("type" to "string", "description" to "Either 'FACT' or 'PREFERENCE'")
        ),
        "required" to listOf("content", "category")
    )

    override suspend fun execute(params: Map<String, Any?>): ToolResult = withContext(Dispatchers.IO) {
        val content = params["content"] as? String
        val category = params["category"] as? String ?: "FACT"
        
        if (content.isNullOrBlank()) {
            return@withContext ToolResult(false, "Missing content to save")
        }
        
        val memoryEngine = WifeAssistantCore.getInstance(context).memoryEngine
        memoryEngine.saveMemory(content, category)
        
        ToolResult(true, "Successfully saved to memory database.")
    }
}
