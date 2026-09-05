package com.example.tools.system

import android.content.Context
import android.hardware.camera2.CameraManager
import com.example.tools.AssistantTool
import com.example.tools.ToolCategory
import com.example.tools.ToolResult

class FlashlightTool(private val context: Context) : AssistantTool {
    override val id = "sys_torch_toggle"
    override val name = "Torch Controller"
    override val category = ToolCategory.SYSTEM_CONTROL
    override val keywords = listOf("লাইট জ্বালাও", "ফ্ল্যাশ অন করো", "আলো দাও", "torch on", "flashlight")
    
    override val description = "Turns the device flashlight on or off."
    override val properties = mapOf("turnOn" to "BOOLEAN")
    override val requiredParams = listOf("turnOn")

    override suspend fun execute(params: Map<String, String>): ToolResult {
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList[0]
            val turnOnStr = params["turnOn"]
            val turnOn = turnOnStr?.toBooleanStrictOrNull() ?: true
            
            cameraManager.setTorchMode(cameraId, turnOn)
            
            ToolResult(
                isSuccess = true,
                responseMessage = if (turnOn) "সুজিত, ফ্ল্যাশলাইট জ্বালিয়ে দেওয়া হয়েছে।" else "সুজিত, ফ্ল্যাশলাইট অফ করা হয়েছে।"
            )
        } catch (e: Exception) {
            ToolResult(
                isSuccess = false,
                responseMessage = "ফ্ল্যাশলাইট কন্ট্রোল করতে সমস্যা হচ্ছে।"
            )
        }
    }
}
