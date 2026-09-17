package com.example.v2.core.tools.impl

import com.example.v2.core.WifeAssistantCore
import com.example.v2.core.automation.AutomationTask
import com.example.v2.core.tools.AssistantTool
import com.example.v2.core.tools.ToolCategory
import com.example.v2.core.tools.ToolResult
import java.util.UUID

class AutomationStartTool(private val core: WifeAssistantCore) : AssistantTool {
    override val id = "automation.start"
    override val name = "Start Device Automation"
    override val description = "Starts a predefined multi-step device workflow/automation."
    override val category = ToolCategory.AUTOMATION
    override val keywords = listOf("automation", "workflow", "routine", "task")
    override val requiredPermissions = emptySet<String>()

    override val parametersSchema = mapOf(
        "type" to "object",
        "properties" to mapOf(
            "workflow_name" to mapOf("type" to "string", "description" to "The name of the workflow to run (e.g., 'morning_routine')")
        ),
        "required" to listOf("workflow_name")
    )

    override suspend fun execute(params: Map<String, Any?>): ToolResult {
        val workflowName = params["workflow_name"] as? String ?: return ToolResult(false, "Missing workflow name")
        
        // Register a long-running Task in TaskEngine
        val taskId = core.taskEngine.submitTask("Automation: $workflowName")
        core.taskEngine.updateTaskState(taskId, com.example.v2.core.tasks.TaskState.RUNNING, 0.1f)
        
        // Execute automation
        val automationTask = AutomationTask(UUID.randomUUID().toString(), workflowName, listOf("step1", "step2"))
        val success = core.automationEngine.runWorkflow(automationTask)
        
        core.taskEngine.updateTaskState(taskId, if (success) com.example.v2.core.tasks.TaskState.COMPLETED else com.example.v2.core.tasks.TaskState.FAILED, 1.0f)
        
        return if (success) {
            ToolResult(true, "Successfully completed automation: $workflowName")
        } else {
            ToolResult(false, "Automation $workflowName failed or was cancelled.")
        }
    }
}
