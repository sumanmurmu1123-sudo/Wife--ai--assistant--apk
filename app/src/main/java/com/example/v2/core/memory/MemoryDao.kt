package com.example.v2.core.memory

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memories ORDER BY createdAt DESC")
    fun getAllMemories(): Flow<List<MemoryItem>>

    @Query("SELECT * FROM memories WHERE category = :category AND enabled = 1")
    suspend fun getActiveMemoriesByCategory(category: String): List<MemoryItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: MemoryItem)

    @Query("SELECT * FROM memories WHERE content LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    suspend fun searchMemories(query: String): List<MemoryItem>

    @Query("SELECT * FROM memories WHERE id = :id LIMIT 1")
    suspend fun getMemoryById(id: String): MemoryItem?

    @Query("DELETE FROM memories WHERE id = :id")
    suspend fun deleteMemoryById(id: String): Int

    @Update
    suspend fun updateMemory(memory: MemoryItem)

    @Delete
    suspend fun deleteMemory(memory: MemoryItem)
}
