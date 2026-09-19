package com.example.v2.voice.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.MainActivity
import kotlinx.coroutines.launch
import com.example.v2.core.StateManager
import com.example.v2.ui.components.WifeCrystalOrb

class VoiceForegroundService : Service(), LifecycleOwner, SavedStateRegistryOwner {
    private lateinit var windowManager: WindowManager
    private var composeView: ComposeView? = null
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    companion object {
        const val CHANNEL_ID = "v2_voice_channel"
        const val NOTIFICATION_ID = 2001
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
    }

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        createNotificationChannel()
        // Observe StateManager for notification updates
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
            StateManager.state.collect { state ->
                val notification = buildNotification("Status: ${state.voiceState} | AI: ${state.geminiState}")
                val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.notify(NOTIFICATION_ID, notification)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val notification = buildNotification("Voice connection active")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    try {
                        startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
                        android.util.Log.d("VoiceRuntime", "FOREGROUND_SERVICE: Started successfully")
                        StateManager.updateState { it.copy(foregroundServiceRunning = true) }
                    } catch (e: Exception) {
                        android.util.Log.e("VoiceRuntime", "FOREGROUND_SERVICE_ERROR: ${e.message}")
                        StateManager.updateState { it.copy(foregroundServiceRunning = false, lastError = "VOICE SERVICE UNAVAILABLE") }
                    }
                } else {
                    startForeground(NOTIFICATION_ID, notification)
                    StateManager.updateState { it.copy(foregroundServiceRunning = true) }
                }
                
                if (Settings.canDrawOverlays(this)) {
                    val prefs = getSharedPreferences("wife_v2_prefs", Context.MODE_PRIVATE)
                    if (prefs.getBoolean("floating_orb_enabled", true)) {
                        showFloatingOrb()
                    }
                }
            }
            ACTION_STOP -> {
                android.util.Log.d("VoiceRuntime", "FOREGROUND_SERVICE: Stopping")
                removeFloatingOrb()
                stopForeground(STOP_FOREGROUND_REMOVE)
                StateManager.updateState { it.copy(foregroundServiceRunning = false) }
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun showFloatingOrb() {
        if (composeView != null) return
        
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 500
        }

        composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@VoiceForegroundService)
            setViewTreeSavedStateRegistryOwner(this@VoiceForegroundService)
            setContent {
                val state by StateManager.state.collectAsState()
                WifeCrystalOrb(
                    audioLevel = state.audioLevel,
                    onDrag = { dx, dy ->
                        params.x += dx.toInt()
                        params.y += dy.toInt()
                        windowManager.updateViewLayout(this, params)
                    },
                    onClick = {
                        val launchIntent = Intent(this@VoiceForegroundService, MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        startActivity(launchIntent)
                    }
                )
            }
        }

        try {
            windowManager.addView(composeView, params)
        } catch (e: Exception) {
            android.util.Log.e("VoiceRuntime", "Failed to add floating orb: ${e.message}")
        }
    }

    private fun removeFloatingOrb() {
        composeView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        composeView = null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Wife AI Voice Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps microphone active for Wife AI"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(status: String): Notification {
        val stopIntent = Intent(this, VoiceForegroundService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = android.app.PendingIntent.getService(
            this, 0, stopIntent, 
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) android.app.PendingIntent.FLAG_IMMUTABLE else 0
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Wife AI Assistant")
            .setContentText(status)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        removeFloatingOrb()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
