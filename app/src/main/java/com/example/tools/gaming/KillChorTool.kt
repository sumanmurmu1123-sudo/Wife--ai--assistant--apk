package com.example.tools.gaming

import android.content.Context
import com.example.gaming.WifeKillChorCaller
import com.example.tools.AssistantTool
import com.example.tools.ToolCategory
import com.example.tools.ToolResult

class KillChorTool(context: Context) : AssistantTool {
    override val id = "game_call_kill_chor"
    override val name = "Kill Chor Caller"
    override val category = ToolCategory.TELEPHONY_COMMS
    override val keywords = listOf("কিল চোর", "কিল চুরি", "kill chor", "ওকে কল করো")
    
    override val description = "Calls a friend who stole a kill in Free Fire or any other game to scold them."
    override val properties = mapOf("friendPhoneNumber" to "STRING")
    override val requiredParams = listOf("friendPhoneNumber")

    private val caller = WifeKillChorCaller(context)

    override suspend fun execute(params: Map<String, String>): ToolResult {
        return try {
            val friendPhoneNumber = params["friendPhoneNumber"] ?: "01700000000" // Default dummy number if not provided by Gemini
            
            caller.callKillChorFriend(friendPhoneNumber)
            
            ToolResult(
                isSuccess = true,
                responseMessage = "Calling the kill chor friend..."
            )
        } catch (e: Exception) {
            ToolResult(
                isSuccess = false,
                responseMessage = "Failed to initiate call to the kill chor."
            )
        }
    }
}
