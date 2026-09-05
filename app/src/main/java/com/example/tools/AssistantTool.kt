package com.example.tools

enum class ToolCategory {
    SYSTEM_CONTROL,      // Wi-Fi, Flashlight, Volume, Battery
    SENSORS_MAGIC,       // Shake, Radar, Hand-wave, Face Lock
    DEVICE_UTILITY,      // Calculator, Alarm, Stopwatch, Timer
    TELEPHONY_COMMS,     // Call, SMS, WhatsApp, Email
    VISION_SECURITY,     // Camera OCR, Intruder Selfie, QR Scanner
    AUTOMATION_LIFESTYLE // Water tracker, Step counter, Sleep log
}

data class ToolResult(
    val isSuccess: Boolean,
    val responseMessage: String,
    val data: Any? = null
)

interface AssistantTool {
    val id: String              // Function call name e.g., "flashlight_on"
    val name: String            // Tool name
    val category: ToolCategory  // Category
    val keywords: List<String>  // Trigger keywords

    // Added for Gemini integration
    val description: String
    val properties: Map<String, String>
    val requiredParams: List<String>

    suspend fun execute(params: Map<String, String>): ToolResult
}
