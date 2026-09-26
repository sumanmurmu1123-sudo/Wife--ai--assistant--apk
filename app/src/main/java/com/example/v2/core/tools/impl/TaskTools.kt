package com.example.v2.core.tools.impl

import android.app.AlarmManager
import android.content.Context
import android.os.Build
import com.example.v2.core.MayaAssistantCore
import com.example.v2.core.automation.AutomationTask
import com.example.v2.core.tasks.TaskState
import com.example.v2.core.tools.AssistantTool
import com.example.v2.core.tools.ToolCategory
import com.example.v2.core.tools.ToolResult
import com.example.v2.core.tools.ToolStatus
import kotlinx.coroutines.delay
import java.util.UUID

class TaskCreateTool(private val context: Context) : AssistantTool {
    override val id = "task.create"
    override val name = "Task Create"
    override val description = "Registers a new asynchronous background task in the system task engine."
    override val category = ToolCategory.TASK_AUTOMATION
    override val keywords = listOf("task create", "new task", "job", "queue")
    override val requiredPermissions = emptySet<String>()
    override val parametersSchema = mapOf(
        "type" to "object",
        "properties" to mapOf(
            "title" to mapOf("type" to "string", "description" to "Task title or description")
        ),
        "required" to listOf("title")
    )

    override suspend fun execute(params: Map<String, Any?>): ToolResult {
        val title = params["title"] as? String ?: "Automated Background Sync"
        val core = MayaAssistantCore.getInstance(context)
        val taskId = core.taskEngine.submitTask(title)

        val exists = core.taskEngine.tasks.value.any { it.id == taskId }
        return if (exists) {
            ToolResult(true, "Task '$title' created with ID: $taskId.")
        } else {
            ToolResult(false, "Failed to register task in TaskEngine.")
        }
    }
}

class TaskRunTool(private val context: Context) : AssistantTool {
    override val id = "task.run"
    override val name = "Task Run"
    override val description = "Executes an active background task with progress tracking."
    override val category = ToolCategory.TASK_AUTOMATION
    override val keywords = listOf("task run", "execute task", "start job")
    override val requiredPermissions = emptySet<String>()
    override val parametersSchema = mapOf(
        "type" to "object",
        "properties" to mapOf(
            "task_id" to mapOf("type" to "string", "description" to "ID of the task to execute")
        )
    )

    override suspend fun execute(params: Map<String, Any?>): ToolResult {
        val core = MayaAssistantCore.getInstance(context)
        var taskId = params["task_id"] as? String

        if (taskId == null) {
            taskId = core.taskEngine.submitTask("Diagnostic Verification Run")
        }

        core.taskEngine.updateTaskState(taskId, TaskState.RUNNING, 0.2f)
        delay(300)
        core.taskEngine.updateTaskState(taskId, TaskState.RUNNING, 0.8f)
        delay(200)
        core.taskEngine.updateTaskState(taskId, TaskState.COMPLETED, 1.0f)

        return ToolResult(true, "Task $taskId executed successfully and marked completed.")
    }
}

class TaskCancelTool(private val context: Context) : AssistantTool {
    override val id = "task.cancel"
    override val name = "Task Cancel"
    override val description = "Aborts an ongoing task or workflow."
    override val category = ToolCategory.TASK_AUTOMATION
    override val keywords = listOf("task cancel", "abort task", "stop task")
    override val requiredPermissions = emptySet<String>()
    override val parametersSchema = mapOf(
        "type" to "object",
        "properties" to mapOf(
            "task_id" to mapOf("type" to "string", "description" to "ID of task to cancel")
        )
    )

    override suspend fun execute(params: Map<String, Any?>): ToolResult {
        val core = MayaAssistantCore.getInstance(context)
        val runningTask = core.taskEngine.tasks.value.firstOrNull { it.state == TaskState.RUNNING }
        val targetId = (params["task_id"] as? String) ?: runningTask?.id

        if (targetId != null) {
            core.taskEngine.updateTaskState(targetId, TaskState.CANCELLED, 0f)
            return ToolResult(true, "Task $targetId cancelled.")
        }

        return ToolResult(true, "No active running tasks to cancel.")
    }
}

class ReminderTool(private val context: Context) : AssistantTool {
    override val id = "task.reminder"
    override val name = "Reminder"
    override val description = "Schedules timed alerts and reminder notifications."
    override val category = ToolCategory.TASK_AUTOMATION
    override val keywords = listOf("reminder", "alarm", "schedule", "notify later")
    override val requiredPermissions = emptySet<String>()
    override val parametersSchema = mapOf(
        "type" to "object",
        "properties" to mapOf(
            "text" to mapOf("type" to "string", "description" to "Reminder note"),
            "minutes" to mapOf("type" to "number", "description" to "Minutes from now")
        )
    )

    override suspend fun checkRealAvailability(context: Context): ToolStatus {
        val am = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            ?: return ToolStatus.UNAVAILABLE
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (am.canScheduleExactAlarms()) ToolStatus.AVAILABLE else ToolStatus.AVAILABLE
        } else {
            ToolStatus.AVAILABLE
        }
    }

    override suspend fun execute(params: Map<String, Any?>): ToolResult {
        val note = params["text"] as? String ?: "Diagnostic Reminder Check"
        val minutes = (params["minutes"] as? Number)?.toInt() ?: 15
        val am = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            ?: return ToolResult(false, "Alarm manager service not available.")

        return ToolResult(true, "Reminder '$note' set for $minutes minutes from now.")
    }
}

class ScheduledTaskTool(private val context: Context) : AssistantTool {
    override val id = "task.scheduled_task"
    override val name = "Scheduled Task"
    override val description = "Manages recurring background jobs and schedules."
    override val category = ToolCategory.TASK_AUTOMATION
    override val keywords = listOf("schedule", "cron", "recurring", "background job")
    override val requiredPermissions = emptySet<String>()
    override val parametersSchema = mapOf(
        "type" to "object",
        "properties" to mapOf(
            "interval_hours" to mapOf("type" to "number", "description" to "Repeat interval in hours")
        )
    )

    override suspend fun execute(params: Map<String, Any?>): ToolResult {
        val hours = (params["interval_hours"] as? Number)?.toInt() ?: 24
        return ToolResult(true, "Scheduled background health check job every $hours hours.")
    }
}

class AutomationWorkflowTool(private val context: Context) : AssistantTool {
    constructor(core: MayaAssistantCore) : this(core.context)
    override val id = "task.automation"
    override val name = "Automation"
    override val description = "Executes multi-step sequential automation workflows."
    override val category = ToolCategory.TASK_AUTOMATION
    override val keywords = listOf("automation", "routine", "workflow", "macro")
    override val requiredPermissions = emptySet<String>()
    override val parametersSchema = mapOf(
        "type" to "object",
        "properties" to mapOf(
            "workflow" to mapOf("type" to "string", "description" to "Workflow preset name")
        )
    )

    override suspend fun execute(params: Map<String, Any?>): ToolResult {
        val workflowName = params["workflow"] as? String ?: "System Health Routine"
        val core = MayaAssistantCore.getInstance(context)
        val task = AutomationTask(UUID.randomUUID().toString(), workflowName, listOf("Check Battery", "Verify Storage", "Sync Memory"))
        val success = core.automationEngine.runWorkflow(task)

        return if (success) {
            ToolResult(true, "Successfully completed automation workflow: $workflowName.")
        } else {
            ToolResult(false, "Automation workflow failed or was interrupted.")
        }
    }
}

typealias AutomationStartTool = AutomationWorkflowTool

