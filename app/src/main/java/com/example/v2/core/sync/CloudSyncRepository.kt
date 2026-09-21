package com.example.v2.core.sync

import com.example.data.memory.MemoryEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

interface CloudSyncRepository {
    suspend fun uploadMemories(userId: String, memories: List<MemoryEntity>): Result<Unit>
    suspend fun downloadMemories(userId: String, lastSyncedAt: Long): Result<List<MemoryEntity>>
}

class FirestoreSyncRepository(private val dbOverride: FirebaseFirestore? = null) : CloudSyncRepository {
    private val db: FirebaseFirestore? by lazy {
        dbOverride ?: try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            if (e.message?.contains("FirebaseApp is not initialized") != true) {
                android.util.Log.e("SyncRepo", "Failed to get Firestore: ${e.message}")
            }
            null
        }
    }

    override suspend fun uploadMemories(userId: String, memories: List<MemoryEntity>): Result<Unit> {
        val dbInstance = db ?: return Result.failure(Exception("Firestore not initialized"))
        return try {
            val batch = dbInstance.batch()
            val userMemoriesRef = dbInstance.collection("users").document(userId).collection("memories")
            
            memories.forEach { memory ->
                val docRef = userMemoriesRef.document(memory.syncId)
                val data = mapOf(
                    "icon" to memory.icon,
                    "text" to memory.text,
                    "time" to memory.time,
                    "timestamp" to memory.timestamp,
                    "updatedAt" to memory.updatedAt,
                    "isDeleted" to memory.isDeleted
                )
                batch.set(docRef, data, SetOptions.merge())
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun downloadMemories(userId: String, lastSyncedAt: Long): Result<List<MemoryEntity>> {
        val dbInstance = db ?: return Result.failure(Exception("Firestore not initialized"))
        return try {
            val snapshot = dbInstance.collection("users").document(userId).collection("memories")
                .whereGreaterThan("updatedAt", lastSyncedAt)
                .get()
                .await()
            
            val memories = snapshot.documents.map { doc ->
                MemoryEntity(
                    icon = doc.getString("icon") ?: "",
                    text = doc.getString("text") ?: "",
                    time = doc.getString("time") ?: "",
                    timestamp = doc.getLong("timestamp") ?: 0L,
                    syncId = doc.id,
                    updatedAt = doc.getLong("updatedAt") ?: 0L,
                    isDeleted = doc.getBoolean("isDeleted") ?: false
                )
            }
            Result.success(memories)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
