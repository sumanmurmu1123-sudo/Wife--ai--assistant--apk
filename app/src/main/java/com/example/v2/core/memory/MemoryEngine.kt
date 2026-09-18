package com.example.v2.core.memory

import android.content.Context
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class MemoryEngine(context: Context) {
    private val db = MemoryDatabase.getDatabase(context)
    private val dao = db.memoryDao()

    fun getAllMemories(): Flow<List<MemoryItem>> = dao.getAllMemories()

    suspend fun getActiveMemoriesContext(): String {
        val facts = dao.getActiveMemoriesByCategory("FACT")
        val prefs = dao.getActiveMemoriesByCategory("PREFERENCE")
        if (facts.isEmpty() && prefs.isEmpty()) return ""
        
        val sb = StringBuilder("Memory Context:\n")
        facts.forEach { sb.append("- ${it.content}\n") }
        prefs.forEach { sb.append("- Preference: ${it.content}\n") }
        return sb.toString()
    }

    suspend fun saveMemory(content: String, category: String) {
        val now = System.currentTimeMillis()
        val item = MemoryItem(
            id = UUID.randomUUID().toString(),
            content = content,
            category = category,
            createdAt = now,
            updatedAt = now,
            enabled = true
        )
        dao.insertMemory(item)
    }

    suspend fun searchMemories(query: String): List<MemoryItem> {
        return dao.searchMemories(query)
    }

    suspend fun updateMemoryContent(id: String, newContent: String): Boolean {
        val item = dao.getMemoryById(id) ?: return false
        dao.updateMemory(item.copy(content = newContent, updatedAt = System.currentTimeMillis()))
        return true
    }

    suspend fun deleteMemoryById(id: String): Boolean {
        return dao.deleteMemoryById(id) > 0
    }

    suspend fun updateMemoryStatus(item: MemoryItem, enabled: Boolean) {
        dao.updateMemory(item.copy(enabled = enabled, updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteMemory(item: MemoryItem) {
        dao.deleteMemory(item)
    }
}
