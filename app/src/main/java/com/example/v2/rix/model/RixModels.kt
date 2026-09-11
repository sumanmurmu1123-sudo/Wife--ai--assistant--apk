package com.example.v2.rix.model

enum class RiskLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

enum class TaskState {
    CREATED,
    PLANNED,
    WAITING_APPROVAL,
    RUNNING,
    VERIFYING,
    COMPLETED,
    FAILED,
    CANCELLED
}

data class BusinessOpportunity(
    val id: String,
    val title: String,
    val matchScore: Int,
    val budgetEstimate: String,
    val effort: String,
    val risk: RiskLevel,
    val recommendedAction: String,
    val deadline: String,
    val isHighPriority: Boolean = false
)

data class RixTask(
    val taskId: String,
    val agentId: String,
    val title: String,
    val status: TaskState,
    val riskLevel: RiskLevel,
    val requiresApproval: Boolean
)
