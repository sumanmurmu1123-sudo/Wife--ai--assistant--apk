package com.example.tools.magic

import com.example.magic.WifeGirlJealousyEngine
import com.example.tools.AssistantTool
import com.example.tools.ToolCategory
import com.example.tools.ToolResult

class GirlJealousyTool(private val engine: WifeGirlJealousyEngine) : AssistantTool {
    override val id = "magic_girl_jealousy"
    override val name = "Girl Jealousy Engine"
    override val category = ToolCategory.AUTOMATION_LIFESTYLE
    override val keywords = listOf(
        "মেয়ের সাথে", "একটা মেয়ের", "অন্য মেয়ে", "girl", "লড়কি", "বান্ধবী", "girl sathe", "kotha bolchi",
        "শুধু বন্ধু", "জাস্ট ফ্রেন্ড", "just friend", "ক্লাসমেট", "অফিসের ফ্রেন্ড",
        "সরি", "তুমিই সব", "রাগ করো না"
    )
    
    override val description = "Reacts with jealousy and drama if Sujit talks to or mentions another girl, handles excuses and apologies."
    override val properties = mapOf("userInput" to "STRING")
    override val requiredParams = listOf("userInput")

    override suspend fun execute(params: Map<String, String>): ToolResult {
        val input = params["userInput"] ?: ""
        val reply = engine.handleGirlChatSituation(input)
        return ToolResult(isSuccess = true, responseMessage = reply)
    }
}
