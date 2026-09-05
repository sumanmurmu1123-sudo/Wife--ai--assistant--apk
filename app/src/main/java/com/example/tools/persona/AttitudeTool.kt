package com.example.tools.persona

import com.example.persona.WifeAttitudeEngine
import com.example.tools.AssistantTool
import com.example.tools.ToolCategory
import com.example.tools.ToolResult

class AttitudeTool(private val engine: WifeAttitudeEngine) : AssistantTool {
    override val id = "persona_attitude"
    override val name = "Attitude Engine"
    override val category = ToolCategory.AUTOMATION_LIFESTYLE
    override val keywords = listOf("করো", "জলদি করো", "শোনো", "কাজ কর", "order", "হুকুম", "আমি হ্যান্ডসাম", "আমি সেরা", "আমি ভালো", "আমার ভাব", "দেরি হলো", "ব্যস্ত ছিলাম", "লেট হলো")
    
    override val description = "Shows sassy attitude if Sujit acts bossy, brags about himself, or replies late."
    override val properties = mapOf("userInput" to "STRING")
    override val requiredParams = listOf("userInput")

    override suspend fun execute(params: Map<String, String>): ToolResult {
        val input = params["userInput"] ?: ""
        val reply = engine.showAttitude(input)
        return ToolResult(isSuccess = true, responseMessage = reply)
    }
}
