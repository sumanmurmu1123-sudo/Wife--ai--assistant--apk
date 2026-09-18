package com.example.v2.core.tools

import android.content.Context

enum class ToolStatus(val displayName: String) {
    UNAVAILABLE("Unavailable"),
    PERMISSION_REQUIRED("Permission Required"),
    DISABLED("Disabled"),
    AVAILABLE("Available"),
    RUNNING("Running"),
    SUCCESS("Success"),
    FAILED("Failed"),
    ERROR("Error"),
    CANCELLED("Cancelled"),
    TIMEOUT("Timeout")
}

enum class ToolCategory(val displayName: String, val chipName: String) {
    VOICE_AI("Voice & AI", "VOICE"),
    ANDROID_CONTROL("Android Control", "ANDROID"),
    AUTOMATION("Automation", "AUTOMATION"),
    PHONE_SAFETY("Phone Safety", "PHONE"),
    PC_CONTROL("PC Control", "PC"),
    MEMORY("Memory", "MEMORY"),
    TASK_AUTOMATION("Task & Automation", "TASKS"),
    INTERNET("Internet", "INTERNET"),
    GOVERNMENT_JOBS("Government Jobs", "JOBS"),
    MEDIA_CREATIVE("Media & Creative", "MEDIA"),
    SYSTEM_SECURITY("System & Security", "SYSTEM"),

    // Backward-compatible mappings
    SYSTEM_CONTROL("System Control", "ANDROID"),
    DEVICE_CONTROL("Device Control", "ANDROID"),
    VOICE("Voice & AI", "VOICE"),
    MEDIA("Media & Creative", "MEDIA"),
    SMART_HOME("Smart Home", "AUTOMATION"),
    COMMUNICATION("Communication", "PHONE"),
    PRODUCTIVITY("Productivity", "TASKS"),
    FILES("Files", "ANDROID"),
    WEB("Internet", "INTERNET"),
    SEARCH("Internet", "INTERNET"),
    CALENDAR("Tasks", "TASKS"),
    REMINDERS("Reminders", "TASKS"),
    NOTIFICATIONS("Notifications", "SYSTEM"),
    LOCATION("Phone Safety", "PHONE"),
    FINANCE("Finance", "SYSTEM"),
    ENTERTAINMENT("Media & Creative", "MEDIA"),
    AI("Voice & AI", "VOICE"),
    DEVELOPER("Diagnostics", "SYSTEM"),
    NETWORK("Phone Safety", "PHONE"),
    SECURITY("System & Security", "SYSTEM");

    val normalizedCategory: ToolCategory
        get() = when (this) {
            VOICE, AI -> VOICE_AI
            SYSTEM_CONTROL, DEVICE_CONTROL, FILES -> ANDROID_CONTROL
            SMART_HOME -> AUTOMATION
            COMMUNICATION, LOCATION, NETWORK -> PHONE_SAFETY
            CALENDAR, REMINDERS, PRODUCTIVITY -> TASK_AUTOMATION
            WEB, SEARCH -> INTERNET
            MEDIA, ENTERTAINMENT -> MEDIA_CREATIVE
            NOTIFICATIONS, FINANCE, DEVELOPER, SECURITY -> SYSTEM_SECURITY
            else -> this
        }
}

data class ToolResult(
    val success: Boolean,
    val message: String,
    val requiresPermission: Boolean = false,
    val requiresUserAction: Boolean = false,
    val data: Map<String, Any?>? = null
)

interface AssistantTool {
    val id: String
    val name: String
    val description: String
    val category: ToolCategory
    val keywords: List<String>
    val requiredPermissions: Set<String>
    
    // Schema definition for Gemini (JSON schema properties)
    val parametersSchema: Map<String, Any>

    val isSafeToTest: Boolean get() = true
    val dependencies: List<String> get() = emptyList()

    suspend fun checkRealAvailability(context: Context): ToolStatus {
        return ToolStatus.AVAILABLE
    }

    suspend fun execute(params: Map<String, Any?>): ToolResult

    suspend fun executeWithVerification(context: Context, params: Map<String, Any?>): ToolResult {
        return execute(params)
    }

    fun openSettingsOrFix(context: Context) {}
}
