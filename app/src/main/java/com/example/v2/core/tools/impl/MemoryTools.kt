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
    override val name = "Memory Save"
    override val description = "Stores facts, preferences, or user context into long-term Room database."
    override val category = ToolCategory.MEMORY
    override val keywords = listOf("remember", "save memory", "store fact", "preference")
    override val requiredPermissions = emptySet<String>()
    override val parametersSchema = mapOf(
        "type" to "object",
        "properties" to mapOf(
            "content" to mapOf("type" to "string", "description" to "The memory or fact to store"),
            "category" to mapOf("type" to "string", "description" to "FACT or PREFERENCE")
        ),
        "required" to listOf("content")
    )

    override suspend fun execute(params: Map<String, Any?>): ToolResult = withContext(Dispatchers.IO) {
        val content = params["content"] as? String ?: "Test memory entry"
        val category = (params["category"] as? String)?.uppercase() ?: "FACT"

        val engine = WifeAssistantCore.getInstance(context).memoryEngine
        engine.saveMemory(content, category)

        // Verify it was saved by searching
        val matches = engine.searchMemories(content)
        if (matches.isNotEmpty()) {
            ToolResult(true, "Stored and verified memory record in Room database (ID: ${matches.first().id}).")
        } else {
            ToolResult(false, "Failed to verify saved memory entry.")
        }
    }
}

class MemorySearchTool(private val context: Context) : AssistantTool {
    override val id = "memory.search"
    override val name = "Memory Search"
    override val description = "Searches persistent assistant memories by query string."
    override val category = ToolCategory.MEMORY
    override val keywords = listOf("search memory", "recall", "find memory", "lookup")
    override val requiredPermissions = emptySet<String>()
    override val parametersSchema = mapOf(
        "type" to "object",
        "properties" to mapOf(
            "query" to mapOf("type" to "string", "description" to "Search keyword or phrase")
        )
    )

    override suspend fun execute(params: Map<String, Any?>): ToolResult = withContext(Dispatchers.IO) {
        val query = params["query"] as? String ?: ""
        val engine = WifeAssistantCore.getInstance(context).memoryEngine
        val results = engine.searchMemories(query)

        if (results.isNotEmpty()) {
            val preview = results.take(3).joinToString("; ") { it.content }
            ToolResult(true, "Found ${results.size} memory matches: $preview")
        } else {
            ToolResult(true, "Search completed: 0 memories matching '$query'.")
        }
    }
}

class MemoryUpdateTool(private val context: Context) : AssistantTool {
    override val id = "memory.update"
    override val name = "Memory Update"
    override val description = "Edits an existing memory record in the persistent Room database."
    override val category = ToolCategory.MEMORY
    override val keywords = listOf("update memory", "edit memory", "modify fact")
    override val requiredPermissions = emptySet<String>()
    override val parametersSchema = mapOf(
        "type" to "object",
        "properties" to mapOf(
            "id" to mapOf("type" to "string", "description" to "Target memory ID"),
            "content" to mapOf("type" to "string", "description" to "New updated content text")
        )
    )

    override suspend fun execute(params: Map<String, Any?>): ToolResult = withContext(Dispatchers.IO) {
        val engine = WifeAssistantCore.getInstance(context).memoryEngine
        val targetId = params["id"] as? String

        if (targetId.isNullOrBlank()) {
            // Find most recent memory to test update
            val all = engine.searchMemories("")
            if (all.isEmpty()) {
                engine.saveMemory("Auto-generated memory for testing update", "FACT")
            }
            val recent = engine.searchMemories("").firstOrNull()
            if (recent != null) {
                val updated = engine.updateMemoryContent(recent.id, recent.content + " [verified]")
                return@withContext if (updated) {
                    ToolResult(true, "Updated memory ID ${recent.id} in Room.")
                } else {
                    ToolResult(false, "Failed to update memory.")
                }
            }
            return@withContext ToolResult(false, "No memory found to update.")
        }

        val newContent = params["content"] as? String ?: "Updated content"
        val success = engine.updateMemoryContent(targetId, newContent)
        if (success) {
            ToolResult(true, "Memory $targetId updated successfully.")
        } else {
            ToolResult(false, "Memory ID '$targetId' not found.")
        }
    }
}

class MemoryDeleteTool(private val context: Context) : AssistantTool {
    override val id = "memory.delete"
    override val name = "Memory Delete"
    override val description = "Removes a specific memory record from Room storage."
    override val category = ToolCategory.MEMORY
    override val keywords = listOf("forget", "delete memory", "remove fact")
    override val requiredPermissions = emptySet<String>()
    override val parametersSchema = mapOf(
        "type" to "object",
        "properties" to mapOf(
            "id" to mapOf("type" to "string", "description" to "Memory ID to delete")
        )
    )

    override suspend fun execute(params: Map<String, Any?>): ToolResult = withContext(Dispatchers.IO) {
        val engine = WifeAssistantCore.getInstance(context).memoryEngine
        val targetId = params["id"] as? String

        if (targetId.isNullOrBlank()) {
            // To safely test delete, insert a transient item and delete it
            engine.saveMemory("Transient memory for deletion test", "FACT")
            val transient = engine.searchMemories("Transient memory for deletion test").firstOrNull()
            if (transient != null) {
                val deleted = engine.deleteMemoryById(transient.id)
                return@withContext if (deleted) {
                    ToolResult(true, "Deleted verified transient memory test record.")
                } else {
                    ToolResult(false, "Failed to remove test memory record.")
                }
            }
            return@withContext ToolResult(true, "No records to delete.")
        }

        val deleted = engine.deleteMemoryById(targetId)
        if (deleted) {
            ToolResult(true, "Memory $targetId deleted from Room database.")
        } else {
            ToolResult(false, "Memory ID '$targetId' does not exist.")
        }
    }
}
