package com.example.v2.core.tools

data class ToolRuntimeState(
    val toolId: String,
    val name: String = "",
    val category: ToolCategory = ToolCategory.ANDROID_CONTROL,
    val status: ToolStatus = ToolStatus.AVAILABLE,
    val description: String = "",
    val lastExecutionTime: Long = 0L,
    val lastExecutionDurationMs: Long = 0L,
    val lastExecutionId: String? = null,
    val lastResultMessage: String? = null,
    val lastErrorMessage: String? = null,
    val missingPermissions: List<String> = emptyList(),
    val dependenciesReady: Boolean = true,
    val executionCount: Int = 0,
    val successCount: Int = 0,
    val failureCount: Int = 0
)

data class ToolStatistics(
    val totalTools: Int = 0,
    val availableCount: Int = 0,
    val runningCount: Int = 0,
    val failedCount: Int = 0,
    val unavailableCount: Int = 0,
    val permissionRequiredCount: Int = 0,
    val disabledCount: Int = 0,
    val successCount: Int = 0
)

data class ToolExecutionLog(
    val executionId: String,
    val toolId: String,
    val toolName: String,
    val timestamp: Long,
    val durationMs: Long,
    val status: ToolStatus,
    val message: String,
    val error: String? = null
)
