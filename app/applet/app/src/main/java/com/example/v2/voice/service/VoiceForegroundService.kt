package com.example.v2.voice.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.R

class VoiceForegroundService : Service() {
    companion object {
        const val CHANNEL_ID = "v2_voice_channel"
        const val NOTIFICATION_ID = 2001
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val notification = buildNotification("Voice connection active")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
                        } else {
                            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
                        }
                        android.util.Log.d("VoiceRuntime", "FOREGROUND_SERVICE: Started successfully")
                    } catch (e: Exception) {
                        // ForegroundServiceStartNotAllowedException or SecurityException
                        android.util.Log.e("VoiceRuntime", "FOREGROUND_SERVICE_ERROR: ${e.message}")
                    }
                } else {
                    startForeground(NOTIFICATION_ID, notification)
                    android.util.Log.d("VoiceRuntime", "FOREGROUND_SERVICE: Started successfully (Legacy)")
                }
            }
            ACTION_STOP -> {
                android.util.Log.d("VoiceRuntime", "FOREGROUND_SERVICE: Stopping")
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Maya V2 Voice Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps microphone active for Maya V2"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(status: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Maya V2")
            .setContentText(status)
            // Use a fallback icon if launcher icon isn't available
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
