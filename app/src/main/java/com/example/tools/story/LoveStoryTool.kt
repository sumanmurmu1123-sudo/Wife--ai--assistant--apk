package com.example.tools.story

import android.content.Context
import com.example.story.WifeLoveStoryEngine
import com.example.tools.AssistantTool
import com.example.tools.ToolCategory
import com.example.tools.ToolResult

class LoveStoryTool(context: Context) : AssistantTool {
    override val id = "story_love_story"
    override val name = "Love Story Teller"
    override val category = ToolCategory.AUTOMATION_LIFESTYLE
    override val keywords = listOf("ভালোবাসার গল্প", "love story", "প্রেমের গল্প")
    
    override val description = "Narrates a romantic love story between Sujit and the Wife AI."
    override val properties = emptyMap<String, String>()
    override val requiredParams = emptyList<String>()

    private val storyEngine = WifeLoveStoryEngine(context)

    override suspend fun execute(params: Map<String, String>): ToolResult {
        storyEngine.narrateStory()
        return ToolResult(
            isSuccess = true,
            responseMessage = "ভালোবাসার গল্প বলছি..."
        )
    }
}
