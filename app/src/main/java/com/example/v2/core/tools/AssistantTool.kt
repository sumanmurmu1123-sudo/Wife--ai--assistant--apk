package com.example.v2.core.tools

enum class ToolCategory {
    SYSTEM_CONTROL,
    DEVICE_CONTROL,
    VOICE,
    MEDIA,
    SMART_HOME,
    COMMUNICATION,
    PRODUCTIVITY,
    FILES,
    WEB,
    SEARCH,
    CALENDAR,
    REMINDERS,
    NOTIFICATIONS,
    LOCATION,
    FINANCE,
    ENTERTAINMENT,
    AI,
    DEVELOPER,
    PC_CONTROL,
    NETWORK,
    AUTOMATION,
    SECURITY
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

    suspend fun execute(params: Map<String, Any?>): ToolResult
}
