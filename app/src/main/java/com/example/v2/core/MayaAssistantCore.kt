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
import com.example.v2.core.sync.*
import com.example.data.AppDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Dispatchers

class MayaAssistantCore private constructor(val context: Context) {
    val database = AppDatabase.getDatabase(context)
    val cloudAuthRepository: CloudAuthRepository = FirebaseAuthRepository()
    val cloudSyncRepository: CloudSyncRepository = FirestoreSyncRepository()
    val cloudSyncManager = CloudSyncManager(context, cloudAuthRepository, cloudSyncRepository, database.memoryDao())

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
    val geminiRepository by lazy { GeminiRepository(secureStorage) }
    val weatherRepository by lazy { WeatherRepository(secureStorage) }
    val locationProvider by lazy { LocationProvider(context) }
    val elevenLabsRepository by lazy { com.example.v2.core.api.ElevenLabsRepository(secureStorage) }
    val backendRepository by lazy { com.example.v2.core.network.BackendRepository(com.example.data.UserPreferences(context).backendUrl) }
    val geminiLiveManager = com.example.v2.ai.GeminiLiveManager().apply { init(context) }
    val voiceAssistantManager = com.example.v2.voice.VoiceAssistantManager(context, geminiLiveManager, toolRegistry, memoryEngine, backendRepository)
    val diagnosticsEngine = DiagnosticsEngine()

    init {
        // Register all built-in tools across the 11 categories in background to not block main thread
        GlobalScope.launch(Dispatchers.Default) {
            ToolInitializer.registerAllTools(toolRegistry, context)
            hardwareManager.updateCapabilities()
        }
        rgbEngine.initialize(context)
    }

    companion object {
        @Volatile
        private var INSTANCE: MayaAssistantCore? = null

        fun getInstance(context: Context): MayaAssistantCore {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MayaAssistantCore(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
