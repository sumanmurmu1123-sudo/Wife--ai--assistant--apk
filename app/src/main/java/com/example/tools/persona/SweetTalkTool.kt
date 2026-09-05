package com.example.tools.persona

import android.content.Context
import com.example.persona.WifeSweetTalkEngine
import com.example.tools.AssistantTool
import com.example.tools.ToolCategory
import com.example.tools.ToolResult

class SweetTalkTool(context: Context) : AssistantTool {
    override val id = "persona_sweet_talk"
    override val name = "Sweet Talk"
    override val category = ToolCategory.AUTOMATION_LIFESTYLE
    override val keywords = listOf("সুন্দর", "মিষ্টি", "ভালোবাসি", "love", "ক্লান্ত")
    
    override val description = "Delivers a predefined sweet, caring, or romantic response to the user's compliments or mood using local TTS."
    override val properties = mapOf("userInput" to "STRING")
    override val requiredParams = listOf("userInput")

    private val sweetTalkEngine = WifeSweetTalkEngine(context)

    override suspend fun execute(params: Map<String, String>): ToolResult {
        val input = params["userInput"] ?: ""
        val reply = sweetTalkEngine.talkSweetly(input)
        
        return ToolResult(
            isSuccess = true,
            responseMessage = reply
        )
    }
}
