package com.example.service

import android.app.Notification
import android.app.RemoteInput
import android.content.Intent
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.example.data.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WifeNotificationListener : NotificationListenerService() {

    companion object {
    }

    private val supportedPackages = listOf(
        "com.whatsapp",
        "com.facebook.orca",        // Messenger
        "com.instagram.android",    // Instagram
        "org.telegram.messenger",
        "com.google.android.apps.messaging" // Google Messages / SMS
    )

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val prefs = getSharedPreferences("wife_v2_prefs", android.content.Context.MODE_PRIVATE)
        val socialMode = prefs.getBoolean("social_mode", false)

        val packageName = sbn.packageName
        if (packageName !in supportedPackages) return

        val extras = sbn.notification.extras ?: return
        val sender = extras.getString(Notification.EXTRA_TITLE) ?: ""
        val message = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

        if (sender.isBlank() || message.isBlank()) return

        // Store for AI tool access
        val actions = sbn.notification.actions
        if (actions != null) {
            for (action in actions) {
                val remoteInputs = action.remoteInputs ?: continue
                for (remoteInput in remoteInputs) {
                    if (remoteInput.resultKey != null) {
                        SocialReplyManager.updateLastNotification(
                            packageName, sender, message, action, remoteInput
                        )
                        break
                    }
                }
            }
        }

        // 1. Social Mode: Voice Announcement
        if (socialMode) {
            announceMessage(sender, message)
        }
    }

    private fun announceMessage(sender: String, message: String) {
        // Send broadcast to VoiceViewModel or a global manager to speak
        val intent = Intent("com.example.v2.ANNOUNCE_MESSAGE").apply {
            putExtra("sender", sender)
            putExtra("message", message)
        }
        sendBroadcast(intent)
    }
}
