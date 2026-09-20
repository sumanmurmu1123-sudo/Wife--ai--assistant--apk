package com.example.v2.core

import android.content.Context
import com.example.v2.core.automation.AutomationEngine
import com.example.v2.core.connectors.ConnectorEngine
import com.example.v2.core.diagnostics.DiagnosticsEngine
import com.example.v2.core.diagnostics.HardwareCapabilityManager
import com.example.v2.core.memory.MemoryEngine
import com.example.v2.core.pc.PcControlEngine
import com.example.v2.core.permission.PermissionManager
import com.example.v2.core.rgb.RgbEngine
import com.example.v2.core.security.SecurityManager
import com.example.v2.core.security.SecureStorage
import com.example.v2.core.api.GeminiRepository
import com.example.v2.core.api.WeatherRepository
import com.example.v2.core.location.LocationProvider
import com.example.v2.core.tasks.TaskEngine
import com.example.v2.core.tools.ToolExecutionEngine
import com.example.v2.core.tools.ToolInitializer
import com.example.v2.core.tools.ToolRegistry
import com.example.v2.core.tools.ToolStateManager

class WifeAssistantCore private constructor(val context: Context) {
    val memoryEngine = MemoryEngine(context)
    val toolRegistry = ToolRegistry()
    val toolStateManager = ToolStateManager(context, toolRegistry)
    val toolEngine = ToolExecutionEngine(toolRegistry, toolStateManager)
    val pcEngine = PcControlEngine(context)
    val automationEngine = AutomationEngine()
    val rgbEngine = RgbEngine()
    val connectorEngine = ConnectorEngine()
    val taskEngine = TaskEngine()
    val permissionManager = PermissionManager(context)
    val hardwareManager = HardwareCapabilityManager(context)
    val securityManager = SecurityManager()
    val secureStorage = SecureStorage(context)
    val geminiRepository = GeminiRepository(secureStorage)
    val weatherRepository = WeatherRepository(secureStorage)
    val locationProvider = LocationProvider(context)
    val elevenLabsRepository = com.example.v2.core.api.ElevenLabsRepository(secureStorage)
    val geminiLiveManager = com.example.v2.ai.GeminiLiveManager().apply { init(context) }
    val diagnosticsEngine = DiagnosticsEngine()

    init {
        // Register all built-in tools across the 11 categories
        ToolInitializer.registerAllTools(toolRegistry, context)
        hardwareManager.updateCapabilities()
        rgbEngine.initialize(context)
    }

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
