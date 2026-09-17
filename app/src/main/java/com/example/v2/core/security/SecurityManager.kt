package com.example.v2.core.security

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first

data class SecurityRequest(
    val actionId: String,
    val description: String,
    val severity: SecuritySeverity
)

enum class SecuritySeverity {
    LOW, MEDIUM, HIGH, CRITICAL
}

class SecurityManager {
    private val _confirmationRequests = MutableSharedFlow<SecurityRequest>(extraBufferCapacity = 10)
    val confirmationRequests = _confirmationRequests
    
    private val _confirmationResponses = MutableSharedFlow<Pair<String, Boolean>>(extraBufferCapacity = 10)

    suspend fun requestActionApproval(actionId: String, description: String, severity: SecuritySeverity): Boolean {
        if (severity == SecuritySeverity.LOW) return true // Auto-approve low risk
        
        _confirmationRequests.emit(SecurityRequest(actionId, description, severity))
        
        // Wait for response matching actionId
        while (true) {
            val response = _confirmationResponses.first()
            if (response.first == actionId) {
                return response.second
            }
        }
    }

    fun submitApproval(actionId: String, approved: Boolean) {
        _confirmationResponses.tryEmit(Pair(actionId, approved))
    }
}
