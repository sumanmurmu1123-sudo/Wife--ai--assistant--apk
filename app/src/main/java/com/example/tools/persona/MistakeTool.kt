package com.example.tools.persona

import com.example.persona.WifeMistakeEngine
import com.example.tools.AssistantTool
import com.example.tools.ToolCategory
import com.example.tools.ToolResult

class MistakeTool(private val engine: WifeMistakeEngine) : AssistantTool {
    override val id = "persona_mistake_handler"
    override val name = "Mistake Handler"
    override val category = ToolCategory.AUTOMATION_LIFESTYLE
    override val keywords = listOf("ভুল করে ফেলেছি", "ভুল হয়ে গেছে", "ভুল করলাম", "গণ্ডগোল হয়ে গেছে", "মিস্টেক", "mistake", "সরি", "sorry", "মাফ করে দাও", "আর হবে না", "ক্ষমা করো")
    
    override val description = "Handles Sujit's mistakes with sweet scolding, support, or forgiveness."
    override val properties = mapOf("userInput" to "STRING")
    override val requiredParams = listOf("userInput")

    override suspend fun execute(params: Map<String, String>): ToolResult {
        val input = params["userInput"] ?: ""
        val reply = engine.handleMistake(input)
        return ToolResult(isSuccess = true, responseMessage = reply)
    }
}
