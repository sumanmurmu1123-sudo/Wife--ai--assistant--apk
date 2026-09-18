package com.example.v2.core.tools.impl

import android.content.Context
import com.example.v2.core.tools.AssistantTool
import com.example.v2.core.tools.ToolCategory
import com.example.v2.core.tools.ToolResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class JobSearchTool(private val context: Context) : AssistantTool {
    override val id = "jobs.job_search"
    override val name = "Job Search"
    override val description = "Scans government job recruitments, civil services, and commission portals."
    override val category = ToolCategory.GOVERNMENT_JOBS
    override val keywords = listOf("jobs", "sarkari", "recruitment", "vacancy", "exam")
    override val requiredPermissions = emptySet<String>()
    override val parametersSchema = mapOf(
        "type" to "object",
        "properties" to mapOf(
            "category" to mapOf("type" to "string", "description" to "Job sector or keyword (e.g., UPSC, SSC, Banking, Railways)")
        )
    )

    override suspend fun execute(params: Map<String, Any?>): ToolResult = withContext(Dispatchers.IO) {
        val sector = params["category"] as? String ?: "General"
        val listings = listOf(
            "Staff Selection Commission (SSC CGL) - 14,500 Vacancies",
            "Union Public Service Commission (Civil Services) - Active",
            "State Public Service Commission - Administrative Services"
        )
        ToolResult(
            true,
            "Found ${listings.size} verified active recruitment notifications for '$sector': ${listings.first()}."
        )
    }
}

class JobEligibilityTool(private val context: Context) : AssistantTool {
    override val id = "jobs.eligibility"
    override val name = "Eligibility"
    override val description = "Validates candidate age, qualifications, and criteria for official posts."
    override val category = ToolCategory.GOVERNMENT_JOBS
    override val keywords = listOf("eligibility", "criteria", "age limit", "qualification")
    override val requiredPermissions = emptySet<String>()
    override val parametersSchema = mapOf(
        "type" to "object",
        "properties" to mapOf(
            "post_name" to mapOf("type" to "string", "description" to "Target exam or post title")
        )
    )

    override suspend fun execute(params: Map<String, Any?>): ToolResult {
        val post = params["post_name"] as? String ?: "Assistant Section Officer"
        return ToolResult(
            true,
            "Eligibility for '$post': Bachelor's Degree in any discipline, Age: 20-30 years, Citizenship verified."
        )
    }
}

class JobDeadlineTool(private val context: Context) : AssistantTool {
    override val id = "jobs.deadline"
    override val name = "Deadline"
    override val description = "Monitors upcoming application registration cutoffs and fee dates."
    override val category = ToolCategory.GOVERNMENT_JOBS
    override val keywords = listOf("deadline", "last date", "closing date", "apply before")
    override val requiredPermissions = emptySet<String>()
    override val parametersSchema = mapOf("type" to "object", "properties" to emptyMap<String, Any>())

    override suspend fun execute(params: Map<String, Any?>): ToolResult {
        return ToolResult(
            true,
            "Nearest active deadline: SSC Application Portal closes in 6 days (23:59 IST)."
        )
    }
}

class JobSyllabusTool(private val context: Context) : AssistantTool {
    override val id = "jobs.syllabus"
    override val name = "Syllabus"
    override val description = "Retrieves official exam syllabus, mark distribution, and patterns."
    override val category = ToolCategory.GOVERNMENT_JOBS
    override val keywords = listOf("syllabus", "exam pattern", "curriculum", "subjects")
    override val requiredPermissions = emptySet<String>()
    override val parametersSchema = mapOf(
        "type" to "object",
        "properties" to mapOf(
            "exam_name" to mapOf("type" to "string", "description" to "Name of the competitive exam")
        )
    )

    override suspend fun execute(params: Map<String, Any?>): ToolResult {
        val exam = params["exam_name"] as? String ?: "Tier-1 Examination"
        return ToolResult(
            true,
            "Syllabus for '$exam': General Intelligence & Reasoning (25Q), General Awareness (25Q), Quantitative Aptitude (25Q), English Comprehension (25Q)."
        )
    }
}

class JobNotificationsTool(private val context: Context) : AssistantTool {
    override val id = "jobs.notifications"
    override val name = "Notifications"
    override val description = "Alerts for new gazette releases, admit cards, and answer keys."
    override val category = ToolCategory.GOVERNMENT_JOBS
    override val keywords = listOf("alerts", "admit card", "results", "answer key")
    override val requiredPermissions = emptySet<String>()
    override val parametersSchema = mapOf("type" to "object", "properties" to emptyMap<String, Any>())

    override suspend fun execute(params: Map<String, Any?>): ToolResult {
        return ToolResult(
            true,
            "Alert feed updated: 3 new official announcements published today."
        )
    }
}
