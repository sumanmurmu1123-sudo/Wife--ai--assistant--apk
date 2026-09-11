package com.example.v2.core.tools.impl

import android.content.Context
import android.hardware.camera2.CameraManager
import android.os.Build
import com.example.v2.core.tools.AssistantTool
import com.example.v2.core.tools.ToolCategory
import com.example.v2.core.tools.ToolResult

class FlashlightTool(private val context: Context) : AssistantTool {
    override val id = "device.flashlight"
    override val name = "Flashlight Control"
    override val description = "Turn the device flashlight on or off."
    override val category = ToolCategory.DEVICE_CONTROL
    override val keywords = listOf("flashlight", "torch", "light")
    override val requiredPermissions = setOf("android.permission.CAMERA")

    override val parametersSchema: Map<String, Any> = mapOf(
        "type" to "OBJECT",
        "properties" to mapOf(
            "action" to mapOf(
                "type" to "STRING",
                "description" to "The action to perform: 'ON' or 'OFF'"
            )
        ),
        "required" to listOf("action")
    )

    override suspend fun execute(params: Map<String, Any?>): ToolResult {
        val action = params["action"]?.toString()?.uppercase()
            ?: return ToolResult(false, "Missing required parameter 'action'")

        val turnOn = action == "ON"
        
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            if (cameraManager == null) {
                return ToolResult(false, "Camera manager not available")
            }
            
            val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                cameraManager.getCameraCharacteristics(id)
                    .get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }

            if (cameraId == null) {
                return ToolResult(false, "No flashlight available on this device")
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cameraManager.setTorchMode(cameraId, turnOn)
                ToolResult(true, "Flashlight turned \$action")
            } else {
                ToolResult(false, "Flashlight control not supported on this Android version")
            }
        } catch (e: Exception) {
            ToolResult(false, "Failed to control flashlight: \${e.message}")
        }
    }
}
