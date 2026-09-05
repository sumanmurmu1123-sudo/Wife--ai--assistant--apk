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
        var isAutoReplyEnabled = false
    }

    private val supportedPackages = listOf(
        "com.whatsapp",
        "com.facebook.orca",        // Messenger
        "org.telegram.messenger",
        "com.google.android.apps.messaging" // Google Messages / SMS
    )

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        if (!isAutoReplyEnabled || sbn == null) return

        val packageName = sbn.packageName
        if (packageName !in supportedPackages) return

        val extras = sbn.notification.extras ?: return
        val sender = extras.getString(Notification.EXTRA_TITLE) ?: ""
        val message = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

        // Avoid replying to group summaries or empty pings
        if (sender.isBlank() || message.isBlank()) return

        // Extract Wearable/Quick Reply action
        val actions = sbn.notification.actions ?: return
        for (action in actions) {
            val remoteInputs = action.remoteInputs ?: continue
            for (remoteInput in remoteInputs) {
                if (remoteInput.resultKey != null) {
                    generateAndSendReply(action, remoteInput, sender, message)
                    return
                }
            }
        }
    }

    private fun generateAndSendReply(
        action: Notification.Action,
        remoteInput: RemoteInput,
        sender: String,
        incomingMessage: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val bossName = UserPreferences(applicationContext).bossName

            // AI Persona Response Logic
            val replyText = "Hey $sender, $bossName is currently busy. - Automated by Wife AI 💕"

            val intent = Intent()
            val bundle = Bundle().apply {
                putCharSequence(remoteInput.resultKey, replyText)
            }
            RemoteInput.addResultsToIntent(arrayOf(remoteInput), intent, bundle)

            try {
                action.actionIntent.send(applicationContext, 0, intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
