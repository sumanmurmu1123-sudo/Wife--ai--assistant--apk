package com.example.service

import android.app.Notification
import android.app.RemoteInput
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SocialNotification(
    val packageName: String,
    val sender: String,
    val message: String,
    val timestamp: Long,
    val action: Notification.Action,
    val remoteInput: RemoteInput
)

object SocialReplyManager {
    private const val TAG = "SocialReplyManager"
    
    private val _lastNotification = MutableStateFlow<SocialNotification?>(null)
    val lastNotification = _lastNotification.asStateFlow()

    fun updateLastNotification(
        packageName: String,
        sender: String,
        message: String,
        action: Notification.Action,
        remoteInput: RemoteInput
    ) {
        Log.d(TAG, "Updating last notification from $packageName ($sender)")
        _lastNotification.value = SocialNotification(
            packageName, sender, message, System.currentTimeMillis(), action, remoteInput
        )
    }

    fun reply(context: Context, text: String): Boolean {
        val notification = _lastNotification.value ?: run {
            Log.e(TAG, "No notification to reply to.")
            return false
        }

        Log.i(TAG, "Attempting to reply to ${notification.packageName} (${notification.sender}) with: $text")
        
        val intent = Intent()
        val bundle = Bundle().apply {
            putCharSequence(notification.remoteInput.resultKey, text)
        }
        RemoteInput.addResultsToIntent(arrayOf(notification.remoteInput), intent, bundle)

        return try {
            notification.action.actionIntent.send(context, 0, intent)
            Log.i(TAG, "Reply sent successfully via Direct Reply.")
            // Clear it after reply to avoid double replies to same notification if logic isn't careful
            _lastNotification.value = null 
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send reply: ${e.message}")
            false
        }
    }
}
