package com.example.v2.core.memory

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memories")
data class MemoryItem(
    @PrimaryKey val id: String,
    val content: String,
    val category: String, // PREFERENCE, SAVED_TASK, FACT, CONFIGURATION
    val createdAt: Long,
    val updatedAt: Long,
    val enabled: Boolean
)
