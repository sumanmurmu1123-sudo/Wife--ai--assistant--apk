package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.example.MainActivity
import com.example.R
import com.example.BuildConfig
import com.example.data.AssistantState
import com.example.data.GeminiLiveSessionManager
import com.example.domain.ToolExecutionEngine
import com.example.domain.WakeWordDetector
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class WifeForegroundService : LifecycleService() {

    private lateinit var toolEngine: ToolExecutionEngine
    private lateinit var sessionManager: GeminiLiveSessionManager
    private lateinit var wakeWordDetector: WakeWordDetector

    companion object {
        const val CHANNEL_ID = "wife_live_channel"
        const val NOTIFICATION_ID = 1337
        const val ACTION_START_SESSION = "ACTION_START_SESSION"
        const val ACTION_STOP_SESSION = "ACTION_STOP_SESSION"
        
        @JvmStatic
        var currentSessionManager: GeminiLiveSessionManager? = null
            private set
    }

    override fun onCreate() {
        super.onCreate()
        val userPrefs = com.example.data.UserPreferences(applicationContext)
        toolEngine = ToolExecutionEngine(applicationContext)
        val gamingModeEngine = com.example.domain.GamingModeEngine(applicationContext, toolEngine)
        val drivingModeEngine = com.example.domain.DrivingModeEngine(applicationContext)
        val geminiKey = userPrefs.geminiApiKey.ifEmpty { BuildConfig.GEMINI_API_KEY }
        sessionManager = GeminiLiveSessionManager(geminiKey, toolEngine, userPrefs, gamingModeEngine, drivingModeEngine)
        currentSessionManager = sessionManager
        
        wakeWordDetector = WakeWordDetector {
            lifecycleScope.launch {
                sessionManager.startSession(lifecycleScope)
            }
        }

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildForegroundNotification("Idle - Say 'wife'"))

        lifecycleScope.launch {
            sessionManager.assistantState.collectLatest { state ->
                val text = when (state) {
                    AssistantState.IDLE -> {
                        wakeWordDetector.startListening(lifecycleScope)
                        "Listening for 'wife'..."
                    }
                    AssistantState.LISTENING -> "wife is listening..."
                    AssistantState.THINKING -> "wife is thinking..."
                    AssistantState.SPEAKING -> "wife is speaking..."
                }
                updateNotification(text)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_START_SESSION -> {
                wakeWordDetector.stopListening()
                lifecycleScope.launch {
                    sessionManager.startSession(lifecycleScope)
                }
            }
            ACTION_STOP_SESSION -> {
                sessionManager.terminateSession()
                wakeWordDetector.startListening(lifecycleScope)
            }
            "ACTION_UPDATE_SOCIAL_MODE" -> {
                val enable = intent.getBooleanExtra("enable", false)
                sessionManager.setSocialMode(enable)
            }
            "ACTION_UPDATE_PERSONA" -> {
                val personaName = intent.getStringExtra("persona")
                if (personaName != null) {
                    try {
                        val persona = com.example.data.CompanionPersona.valueOf(personaName)
                        sessionManager.setPersonaMode(persona)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            "ACTION_TRIGGER_LAUGH" -> {
                lifecycleScope.launch {
                    val joke = toolEngine.laughterEngine.tellJoke()
                    sessionManager.speakProactiveText(joke)
                }
            }
            "ACTION_TRIGGER_GREETING" -> {
                lifecycleScope.launch {
                    sessionManager.onSessionReady()
                }
            }
            "ACTION_SEND_TEXT" -> {
                val text = intent.getStringExtra("text")
                if (!text.isNullOrBlank()) {
                    lifecycleScope.launch {
                        sessionManager.sendTextMessage(text)
                    }
                }
            }
            "ACTION_SEND_FRAME" -> {
                val frameBytes = intent.getByteArrayExtra("frame")
                if (frameBytes != null) {
                    lifecycleScope.launch {
                        sessionManager.sendCameraFrame(frameBytes)
                    }
                }
            }
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "wife Assistant Live Link",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps wife real-time voice intelligence alive."
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(status: String): Notification {
        val launchIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("wife Intelligence Active")
            .setContentText(status)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(status: String) {
        val notification = buildForegroundNotification(status)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        currentSessionManager = null
        sessionManager.terminateSession()
        wakeWordDetector.stopListening()
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }
}
