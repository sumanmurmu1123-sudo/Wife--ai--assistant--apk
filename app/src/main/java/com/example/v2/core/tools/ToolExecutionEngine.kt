package com.example.v2.core.tools

import android.util.Log

class ToolExecutionEngine(
    private val registry: ToolRegistry,
    private val stateManager: ToolStateManager? = null
) {

    suspend fun executeCommand(toolId: String, params: Map<String, Any?>): ToolResult {
        Log.d("ToolExecutionEngine", "Executing tool: $toolId with params: $params")

        if (stateManager != null) {
            return stateManager.executeTool(toolId, params)
        }

        val tool = registry.getTool(toolId)
            ?: return ToolResult(
                success = false,
                message = "Tool not found: $toolId"
            )

        return try {
            tool.execute(params)
        } catch (e: Exception) {
            Log.e("ToolExecutionEngine", "Error executing tool $toolId", e)
            ToolResult(
                success = false,
                message = "Execution failed: ${e.message}"
            )
        }
    }
}
