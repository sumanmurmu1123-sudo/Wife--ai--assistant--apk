package com.example.v2.rix.core

import com.example.v2.rix.model.RixTask
import com.example.v2.rix.model.RiskLevel

interface RixAgent {
    val agentId: String
    val name: String
    val description: String
    
    suspend fun executeIntent(intent: String, params: Map<String, Any?>): Boolean
}

interface RixMemory {
    suspend fun remember(key: String, value: Any)
    suspend fun retrieve(key: String): Any?
    suspend fun forget(key: String)
}

interface RixPermissionManager {
    suspend fun checkPermission(riskLevel: RiskLevel): Boolean
    suspend fun requestApproval(task: RixTask): Boolean
}

interface RixEventBus {
    fun publish(event: String, payload: Any?)
    fun subscribe(event: String, callback: (Any?) -> Unit)
}
