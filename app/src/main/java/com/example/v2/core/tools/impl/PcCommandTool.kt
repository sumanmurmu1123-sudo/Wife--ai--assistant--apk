package com.example.v2.core.tools.impl

import com.example.v2.core.WifeAssistantCore
import com.example.v2.core.tools.AssistantTool
import com.example.v2.core.tools.ToolCategory
import com.example.v2.core.tools.ToolResult

class PcCommandTool(private val core: WifeAssistantCore) : AssistantTool {
    override val id = "pc.execute"
    override val name = "Execute PC Command"
    override val description = "Executes an authorized command on the connected PC (e.g., lock screen, open browser)."
    override val category = ToolCategory.PC_CONTROL
    override val keywords = listOf("pc", "computer", "execute", "command", "lock")
    override val requiredPermissions = emptySet<String>()

    override val parametersSchema = mapOf(
        "type" to "object",
        "properties" to mapOf(
            "command" to mapOf("type" to "string", "description" to "The command name to execute")
        ),
        "required" to listOf("command")
    )

    override suspend fun execute(params: Map<String, Any?>): ToolResult {
        val command = params["command"] as? String ?: return ToolResult(false, "Missing command")
        
        // Ensure PC is connected
        if (core.pcEngine.connectionState.value != com.example.v2.core.AssistantConnectionState.CONNECTED) {
            return ToolResult(false, "PC is disconnected. I cannot run commands.")
        }
        
        // Require security confirmation
        val approved = core.securityManager.requestActionApproval(
            "pc_execute_$command", 
            "Execute PC Command: $command", 
            com.example.v2.core.security.SecuritySeverity.HIGH
        )
        if (!approved) {
            return ToolResult(false, "User denied execution.", requiresPermission = true)
        }
        
        val success = core.pcEngine.executeCommand(command, emptyMap())
        return if (success) {
            ToolResult(true, "Successfully executed $command on PC.")
        } else {
            ToolResult(false, "Failed to execute $command on PC.")
        }
    }
}
