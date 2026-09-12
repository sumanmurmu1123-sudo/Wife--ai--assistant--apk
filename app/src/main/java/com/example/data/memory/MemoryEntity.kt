package com.example.data.memory

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val icon: String,
    val text: String,
    val time: String,
    val timestamp: Long = System.currentTimeMillis()
)
