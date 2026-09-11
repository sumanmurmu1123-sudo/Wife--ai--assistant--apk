package com.example.v2.core.tools

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ToolRegistry {
    private val _tools = mutableMapOf<String, AssistantTool>()
    
    private val _registryState = MutableStateFlow<List<AssistantTool>>(emptyList())
    val registryState: StateFlow<List<AssistantTool>> = _registryState.asStateFlow()

    fun register(tool: AssistantTool) {
        _tools[tool.id] = tool
        _registryState.value = _tools.values.toList()
    }

    fun unregister(toolId: String) {
        _tools.remove(toolId)
        _registryState.value = _tools.values.toList()
    }

    fun getTool(toolId: String): AssistantTool? {
        return _tools[toolId]
    }

    fun getAllTools(): List<AssistantTool> {
        return _tools.values.toList()
    }

    fun searchTools(query: String): List<AssistantTool> {
        val lowercaseQuery = query.lowercase()
        return _tools.values.filter { tool ->
            tool.name.lowercase().contains(lowercaseQuery) ||
            tool.description.lowercase().contains(lowercaseQuery) ||
            tool.keywords.any { it.lowercase().contains(lowercaseQuery) }
        }
    }
}
