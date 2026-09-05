package com.example.tools.magic

import android.content.Context
import com.example.magic.WifeAntiDrinkEngine
import com.example.tools.AssistantTool
import com.example.tools.ToolCategory
import com.example.tools.ToolResult

class AntiDrinkTool(private val engine: WifeAntiDrinkEngine) : AssistantTool {
    override val id = "magic_anti_drink"
    override val name = "Anti Drink Enforcer"
    override val category = ToolCategory.AUTOMATION_LIFESTYLE
    override val keywords = listOf("drink", "ড্রিংক", "মদ", "দারু", "alcohol", "beer", "বিয়ার", "নেশা", "sharab", "মদ্যপান", "বন্ধুদের সাথে", "পার্টি", "পার্টিতে", "একটু", "সামান্য", "মজা করছিলাম", "সরি", "sorry", "আর খাব না", "ভুল হয়ে গেছে", "ছেড়ে দেব", "মাফ করো")
    
    override val description = "Scolds Sujit if he drinks, vibrates the phone, and reacts to his excuses or apologies about drinking alcohol."
    override val properties = mapOf("userInput" to "STRING")
    override val requiredParams = listOf("userInput")
    
    override suspend fun execute(params: Map<String, String>): ToolResult {
        val input = params["userInput"] ?: ""
        val reply = engine.handleDrinkSituation(input)
        return ToolResult(isSuccess = true, responseMessage = reply)
    }
}
