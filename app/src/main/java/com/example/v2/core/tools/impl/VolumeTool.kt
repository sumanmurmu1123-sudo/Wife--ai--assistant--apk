package com.example.v2.core.tools.impl

import android.content.Context
import android.media.AudioManager
import com.example.v2.core.tools.AssistantTool
import com.example.v2.core.tools.ToolCategory
import com.example.v2.core.tools.ToolResult

class VolumeTool(private val context: Context) : AssistantTool {
    override val id = "device.volume"
    override val name = "Volume Control"
    override val description = "Set the device media volume level."
    override val category = ToolCategory.DEVICE_CONTROL
    override val keywords = listOf("volume", "sound", "loudness", "mute")
    override val requiredPermissions = emptySet<String>()

    override val parametersSchema: Map<String, Any> = mapOf(
        "type" to "OBJECT",
        "properties" to mapOf(
            "level" to mapOf(
                "type" to "NUMBER",
                "description" to "The volume level percentage from 0 to 100"
            )
        ),
        "required" to listOf("level")
    )

    override suspend fun execute(params: Map<String, Any?>): ToolResult {
        val levelDouble = params["level"] as? Double
            ?: return ToolResult(false, "Missing or invalid required parameter 'level'")
            
        val levelPercentage = levelDouble.toInt().coerceIn(0, 100)
        
        return try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            if (audioManager == null) {
                return ToolResult(false, "Audio manager not available")
            }
            
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val targetVolume = ((levelPercentage / 100.0) * maxVolume).toInt()
            
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVolume, AudioManager.FLAG_SHOW_UI)
            
            ToolResult(true, "Volume set to \$levelPercentage%")
        } catch (e: Exception) {
            ToolResult(false, "Failed to control volume: \${e.message}")
        }
    }
}
