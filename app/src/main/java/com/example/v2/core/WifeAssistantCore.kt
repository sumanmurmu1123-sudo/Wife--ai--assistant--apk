package com.example.v2.core

import android.content.Context
import com.example.v2.core.automation.AutomationEngine
import com.example.v2.core.connectors.ConnectorEngine
import com.example.v2.core.diagnostics.DiagnosticsEngine
import com.example.v2.core.memory.MemoryEngine
import com.example.v2.core.pc.PcControlEngine
import com.example.v2.core.permission.PermissionManager
import com.example.v2.core.rgb.RgbEngine
import com.example.v2.core.security.SecurityManager
import com.example.v2.core.tasks.TaskEngine
import com.example.v2.core.tools.ToolExecutionEngine
import com.example.v2.core.tools.ToolRegistry

class WifeAssistantCore private constructor(context: Context) {
    val memoryEngine = MemoryEngine(context)
    val toolRegistry = ToolRegistry()
    val toolEngine = ToolExecutionEngine(toolRegistry)
    val pcEngine = PcControlEngine()
    val automationEngine = AutomationEngine()
    val rgbEngine = RgbEngine()
    val connectorEngine = ConnectorEngine()
    val taskEngine = TaskEngine()
    val permissionManager = PermissionManager(context)
    val securityManager = SecurityManager()
    val diagnosticsEngine = DiagnosticsEngine()

    companion object {
        @Volatile
        private var INSTANCE: WifeAssistantCore? = null

        fun getInstance(context: Context): WifeAssistantCore {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: WifeAssistantCore(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
