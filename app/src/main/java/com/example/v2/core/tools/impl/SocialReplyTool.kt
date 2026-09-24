package com.example.v2.core.tools.impl

import android.content.Context
import com.example.service.SocialReplyManager
import com.example.v2.core.tools.AssistantTool
import com.example.v2.core.tools.ToolCategory
import com.example.v2.core.tools.ToolResult
import com.example.v2.core.tools.ToolStatus

class SocialReplyTool(private val context: Context) : AssistantTool {
    override val id: String = "reply_to_social_message"
    override val name: String = "Reply to Social Message"
    override val description: String = "Reply to the most recent message received on WhatsApp, Instagram, or Facebook Messenger. Use this when the user asks to reply to a person or message."
    override val category: ToolCategory = ToolCategory.COMMUNICATION
    override val keywords: List<String> = listOf("reply", "whatsapp", "messenger", "instagram", "message", "social")
    override val requiredPermissions: Set<String> = emptySet()

    override val parametersSchema: Map<String, Any> = mapOf(
        "type" to "OBJECT",
        "properties" to mapOf(
            "reply_text" to mapOf(
                "type" to "STRING",
                "description" to "The text of the reply to send."
            ),
            "app_name" to mapOf(
                "type" to "STRING",
                "description" to "Optional app name (whatsapp, instagram, messenger) to confirm which message to reply to."
            )
        ),
        "required" to listOf("reply_text")
    )

    override suspend fun checkRealAvailability(context: Context): ToolStatus {
        return if (SocialReplyManager.lastNotification.value != null) {
            ToolStatus.AVAILABLE
        } else {
            ToolStatus.UNAVAILABLE
        }
    }

    override suspend fun execute(params: Map<String, Any?>): ToolResult {
        val replyText = params["reply_text"] as? String ?: return ToolResult(false, "Reply text is missing.")
        val appName = params["app_name"] as? String
        
        val lastNotif = SocialReplyManager.lastNotification.value
        if (lastNotif == null) {
            return ToolResult(false, "No recent message found to reply to. I can only reply if a notification was received recently.")
        }

        // Optional filtering by app name if Gemini provides it
        if (appName != null) {
            val pkg = lastNotif.packageName.lowercase()
            val matches = when (appName.lowercase()) {
                "whatsapp" -> pkg.contains("whatsapp")
                "instagram" -> pkg.contains("instagram")
                "messenger" -> pkg.contains("orca") || pkg.contains("messenger")
                else -> true
            }
            if (!matches) {
                return ToolResult(false, "The last message was from ${lastNotif.packageName}, not $appName.")
            }
        }

        val success = SocialReplyManager.reply(context, replyText)
        return if (success) {
            ToolResult(true, "Successfully sent reply to ${lastNotif.sender} via ${lastNotif.packageName}.")
        } else {
            ToolResult(false, "Failed to send the reply. The notification might have expired.")
        }
    }
}
