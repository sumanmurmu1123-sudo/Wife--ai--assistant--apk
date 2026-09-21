package com.example.v2.core.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.data.memory.MemoryDao
import com.example.v2.core.CloudSyncState
import com.example.v2.core.StateManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CloudSyncManager(
    private val context: Context,
    private val authRepository: CloudAuthRepository,
    private val syncRepository: CloudSyncRepository,
    private val memoryDao: MemoryDao
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val prefs = context.getSharedPreferences("cloud_sync_prefs", Context.MODE_PRIVATE)

    init {
        // Initialize state on startup
        updateSyncStateFromAuth()
    }

    fun updateSyncStateFromAuth() {
        if (authRepository.isUserLoggedIn()) {
            val lastSyncedAt = prefs.getLong("last_synced_at", 0L)
            StateManager.updateState { 
                it.copy(
                    cloudSyncState = if (lastSyncedAt > 0) CloudSyncState.Synced(lastSyncedAt) else CloudSyncState.Connected,
                    cloudUserEmail = authRepository.currentUserEmail
                )
            }
        } else {
            StateManager.updateState { 
                it.copy(
                    cloudSyncState = CloudSyncState.Disconnected,
                    cloudUserEmail = null
                )
            }
        }
    }

    fun syncNow() {
        if (!isNetworkAvailable()) {
            StateManager.updateState { it.copy(cloudSyncState = CloudSyncState.Offline) }
            return
        }

        if (!authRepository.isUserLoggedIn()) {
            StateManager.updateState { it.copy(cloudSyncState = CloudSyncState.AuthRequired) }
            return
        }

        val userId = authRepository.currentUserEmail ?: "anonymous" // Fallback but authRepository should ensure it's logged in

        scope.launch {
            try {
                StateManager.updateState { it.copy(cloudSyncState = CloudSyncState.Syncing(0f)) }
                
                val lastSyncedAt = prefs.getLong("last_synced_at", 0L)
                
                // 1. Upload local changes
                val localMemories = memoryDao.getMemoriesToSync(lastSyncedAt)
                if (localMemories.isNotEmpty()) {
                    syncRepository.uploadMemories(userId, localMemories).getOrThrow()
                }
                
                StateManager.updateState { it.copy(cloudSyncState = CloudSyncState.Syncing(0.5f)) }

                // 2. Download remote changes
                val remoteMemoriesResult = syncRepository.downloadMemories(userId, lastSyncedAt)
                val remoteMemories = remoteMemoriesResult.getOrThrow()
                
                if (remoteMemories.isNotEmpty()) {
                    memoryDao.insertMemories(remoteMemories)
                }

                // 3. Mark success
                val now = System.currentTimeMillis()
                prefs.edit().putLong("last_synced_at", now).apply()
                StateManager.updateState { it.copy(cloudSyncState = CloudSyncState.Synced(now)) }

            } catch (e: Exception) {
                StateManager.updateState { it.copy(cloudSyncState = CloudSyncState.Error(e.message ?: "Sync failed")) }
            }
        }
    }

    fun connect(onResult: (Result<String>) -> Unit) {
        if (!isNetworkAvailable()) {
            StateManager.updateState { it.copy(cloudSyncState = CloudSyncState.Offline) }
            return
        }

        StateManager.updateState { it.copy(cloudSyncState = CloudSyncState.Connecting) }
        
        scope.launch {
            val result = authRepository.signInWithGoogle(context)
            result.onSuccess { email ->
                StateManager.updateState { it.copy(cloudSyncState = CloudSyncState.Connected, cloudUserEmail = email) }
                onResult(Result.success(email))
            }.onFailure { error ->
                StateManager.updateState { it.copy(cloudSyncState = CloudSyncState.Error(error.message ?: "Auth failed")) }
                onResult(Result.failure(error))
            }
        }
    }

    fun disconnect() {
        scope.launch {
            authRepository.signOut()
            prefs.edit().remove("last_synced_at").apply()
            StateManager.updateState { 
                it.copy(
                    cloudSyncState = CloudSyncState.Disconnected,
                    cloudUserEmail = null
                )
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
