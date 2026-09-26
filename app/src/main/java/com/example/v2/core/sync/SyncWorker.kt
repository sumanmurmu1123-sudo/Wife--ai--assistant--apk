package com.example.v2.core.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.v2.core.MayaAssistantCore

class SyncWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val core = MayaAssistantCore.getInstance(applicationContext)
        val cloudSyncManager = core.cloudSyncManager
        
        return try {
            // Check if user is logged in before starting background sync
            if (core.cloudAuthRepository.isUserLoggedIn()) {
                cloudSyncManager.syncNow()
                Result.success()
            } else {
                Result.failure()
            }
        } catch (e: Exception) {
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}
