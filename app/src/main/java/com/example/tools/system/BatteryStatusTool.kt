package com.example.tools.system

import android.content.Context
import android.os.BatteryManager
import com.example.tools.AssistantTool
import com.example.tools.ToolCategory
import com.example.tools.ToolResult

class BatteryStatusTool(private val context: Context) : AssistantTool {
    override val id = "sys_battery_check"
    override val name = "Battery Monitor"
    override val category = ToolCategory.SYSTEM_CONTROL
    override val keywords = listOf("ব্যাটারি কত", "চার্জ কত", "battery level", "battery status")
    
    override val description = "Checks the current battery level of the device."
    override val properties = emptyMap<String, String>()
    override val requiredParams = emptyList<String>()

    override suspend fun execute(params: Map<String, String>): ToolResult {
        return try {
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val level = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

            ToolResult(
                isSuccess = true,
                responseMessage = "সুজিত, বর্তমানে তোমার ফোনে $level% চার্জ আছে।",
                data = level
            )
        } catch (e: Exception) {
            ToolResult(
                isSuccess = false,
                responseMessage = "ব্যাটারি স্ট্যাটাস চেক করতে সমস্যা হচ্ছে।"
            )
        }
    }
}
