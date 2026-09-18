package com.example.v2.core.tools

import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class ToolStateManager(
    private val context: Context,
    private val toolRegistry: ToolRegistry
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _toolStates = MutableStateFlow<Map<String, ToolRuntimeState>>(emptyMap())
    val toolStates: StateFlow<Map<String, ToolRuntimeState>> = _toolStates.asStateFlow()

    private val _statistics = MutableStateFlow(ToolStatistics())
    val statistics: StateFlow<ToolStatistics> = _statistics.asStateFlow()

    private val _executionLogs = MutableStateFlow<List<ToolExecutionLog>>(emptyList())
    val executionLogs: StateFlow<List<ToolExecutionLog>> = _executionLogs.asStateFlow()

    // toolId -> active executionId
    private val activeExecutions = ConcurrentHashMap<String, String>()

    init {
        // Register network callback for real-time network-dependent tool updates
        registerNetworkMonitor()

        // Initial scan of tool states
        scope.launch {
            refreshAllToolStates()
        }
    }

    private fun registerNetworkMonitor() {
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            cm?.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    scope.launch {
                        refreshCategoryStates(ToolCategory.INTERNET)
                        refreshCategoryStates(ToolCategory.VOICE_AI)
                    }
                }

                override fun onLost(network: Network) {
                    scope.launch {
                        refreshCategoryStates(ToolCategory.INTERNET)
                        refreshCategoryStates(ToolCategory.VOICE_AI)
                    }
                }
            })
        } catch (e: Exception) {
            Log.w("ToolStateManager", "Network callback registration failed: ${e.message}")
        }
    }

    /**
     * Re-evaluates real runtime availability for all tools without blocking UI.
     */
    suspend fun refreshAllToolStates() = withContext(Dispatchers.Default) {
        val tools = toolRegistry.getAllTools()
        val currentStates = _toolStates.value.toMutableMap()

        for (tool in tools) {
            // If tool is currently running, don't overwrite running state
            val existing = currentStates[tool.id]
            if (existing?.status == ToolStatus.RUNNING) {
                continue
            }

            val (status, missingPerms) = evaluateToolStatus(tool)
            currentStates[tool.id] = (existing ?: ToolRuntimeState(
                toolId = tool.id,
                name = tool.name,
                category = tool.category.normalizedCategory,
                description = tool.description
            )).copy(
                status = status,
                missingPermissions = missingPerms,
                category = tool.category.normalizedCategory,
                name = tool.name,
                description = tool.description
            )
        }

        _toolStates.value = currentStates
        recomputeStatistics(currentStates.values)
    }

    private suspend fun refreshCategoryStates(category: ToolCategory) = withContext(Dispatchers.Default) {
        val tools = toolRegistry.getAllTools().filter { it.category.normalizedCategory == category }
        val currentStates = _toolStates.value.toMutableMap()

        for (tool in tools) {
            val existing = currentStates[tool.id]
            if (existing?.status == ToolStatus.RUNNING) continue

            val (status, missingPerms) = evaluateToolStatus(tool)
            currentStates[tool.id] = (existing ?: ToolRuntimeState(
                toolId = tool.id,
                name = tool.name,
                category = tool.category.normalizedCategory,
                description = tool.description
            )).copy(
                status = status,
                missingPermissions = missingPerms
            )
        }

        _toolStates.value = currentStates
        recomputeStatistics(currentStates.values)
    }

    private suspend fun evaluateToolStatus(tool: AssistantTool): Pair<ToolStatus, List<String>> {
        // 1. Permission check
        val missingPermissions = mutableListOf<String>()
        for (perm in tool.requiredPermissions) {
            if (ContextCompat.checkSelfPermission(context, perm) != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(perm)
            }
        }

        if (missingPermissions.isNotEmpty()) {
            return Pair(ToolStatus.PERMISSION_REQUIRED, missingPermissions)
        }

        // 2. Real tool availability check
        return try {
            val avail = tool.checkRealAvailability(context)
            Pair(avail, emptyList())
        } catch (e: Exception) {
            Log.e("ToolStateManager", "Error checking availability for ${tool.id}", e)
            Pair(ToolStatus.UNAVAILABLE, emptyList())
        }
    }

    private fun recomputeStatistics(states: Collection<ToolRuntimeState>) {
        var available = 0
        var running = 0
        var failed = 0
        var unavailable = 0
        var permissionRequired = 0
        var disabled = 0
        var success = 0

        for (state in states) {
            when (state.status) {
                ToolStatus.AVAILABLE -> available++
                ToolStatus.RUNNING -> running++
                ToolStatus.FAILED, ToolStatus.ERROR -> failed++
                ToolStatus.UNAVAILABLE -> unavailable++
                ToolStatus.PERMISSION_REQUIRED -> permissionRequired++
                ToolStatus.DISABLED -> disabled++
                ToolStatus.SUCCESS -> success++
                ToolStatus.CANCELLED, ToolStatus.TIMEOUT -> failed++
            }
        }

        _statistics.value = ToolStatistics(
            totalTools = states.size,
            availableCount = available,
            runningCount = running,
            failedCount = failed,
            unavailableCount = unavailable,
            permissionRequiredCount = permissionRequired,
            disabledCount = disabled,
            successCount = success
        )
    }

    /**
     * Executes a tool with universal pipeline and real result verification.
     */
    suspend fun executeTool(
        toolId: String,
        params: Map<String, Any?> = emptyMap()
    ): ToolResult = withContext(Dispatchers.Default) {
        val tool = toolRegistry.getTool(toolId)
            ?: return@withContext ToolResult(
                success = false,
                message = "Tool not found: $toolId"
            )

        val executionId = UUID.randomUUID().toString()
        activeExecutions[toolId] = executionId
        val startTime = System.currentTimeMillis()

        // 1. Permission Check
        val missingPermissions = tool.requiredPermissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            updateToolState(toolId) {
                it.copy(
                    status = ToolStatus.PERMISSION_REQUIRED,
                    lastExecutionTime = startTime,
                    lastErrorMessage = "Missing permissions: ${missingPermissions.joinToString()}",
                    missingPermissions = missingPermissions
                )
            }
            return@withContext ToolResult(
                success = false,
                message = "Missing required permissions",
                requiresPermission = true
            )
        }

        // 2. Set State to RUNNING
        updateToolState(toolId) {
            it.copy(
                status = ToolStatus.RUNNING,
                lastExecutionTime = startTime,
                lastExecutionId = executionId,
                lastErrorMessage = null
            )
        }

        // 3. Real Execution with Verification
        val result = try {
            tool.executeWithVerification(context, params)
        } catch (e: Exception) {
            Log.e("ToolStateManager", "Exception executing $toolId", e)
            ToolResult(
                success = false,
                message = "Execution failed: ${sanitizeErrorMessage(e.message ?: "Unknown error")}"
            )
        }

        val duration = System.currentTimeMillis() - startTime

        // 4. Stale Execution Guard
        if (activeExecutions[toolId] != executionId) {
            Log.w("ToolStateManager", "Ignored stale execution result for $toolId (exec: $executionId)")
            return@withContext result
        }

        // 5. Update State
        val finalStatus = if (result.success) ToolStatus.SUCCESS else ToolStatus.FAILED
        updateToolState(toolId) { current ->
            current.copy(
                status = finalStatus,
                lastExecutionDurationMs = duration,
                lastResultMessage = result.message,
                lastErrorMessage = if (!result.success) result.message else null,
                executionCount = current.executionCount + 1,
                successCount = if (result.success) current.successCount + 1 else current.successCount,
                failureCount = if (!result.success) current.failureCount + 1 else current.failureCount
            )
        }

        // 6. Log Execution
        addLog(
            ToolExecutionLog(
                executionId = executionId,
                toolId = toolId,
                toolName = tool.name,
                timestamp = startTime,
                durationMs = duration,
                status = finalStatus,
                message = result.message,
                error = if (!result.success) result.message else null
            )
        )

        activeExecutions.remove(toolId)
        return@withContext result
    }

    /**
     * Executes safe testing on a tool.
     */
    suspend fun testTool(toolId: String): ToolResult {
        return executeTool(toolId, emptyMap())
    }

    private fun updateToolState(toolId: String, updater: (ToolRuntimeState) -> ToolRuntimeState) {
        val currentStates = _toolStates.value.toMutableMap()
        val existing = currentStates[toolId] ?: ToolRuntimeState(
            toolId = toolId,
            name = toolRegistry.getTool(toolId)?.name ?: toolId,
            category = toolRegistry.getTool(toolId)?.category?.normalizedCategory ?: ToolCategory.ANDROID_CONTROL
        )
        currentStates[toolId] = updater(existing)
        _toolStates.value = currentStates
        recomputeStatistics(currentStates.values)
    }

    private fun addLog(log: ToolExecutionLog) {
        val current = _executionLogs.value.toMutableList()
        current.add(0, log)
        if (current.size > 50) {
            current.removeAt(current.lastIndex)
        }
        _executionLogs.value = current
    }

    private fun sanitizeErrorMessage(rawMessage: String): String {
        // Mask any API key or secret token pattern
        return rawMessage
            .replace(Regex("(?i)(key|secret|token|password)[=:]\\s*[^\\s&]+"), "$1=***MASKED***")
            .replace(Regex("AIza[0-9A-Za-z-_]{35}"), "***MASKED_API_KEY***")
    }
}
