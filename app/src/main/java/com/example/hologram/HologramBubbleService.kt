package com.example.hologram

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
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
import android.content.pm.ServiceInfo
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect

class HologramBubbleService : Service(), LifecycleOwner, SavedStateRegistryOwner {

    private lateinit var windowManager: WindowManager
    private var composeView: ComposeView? = null
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        WifeServiceManager.updateState(WifeServiceState.SERVICE_STARTING)
        
        // Observe StateManager for overlay suspension
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
            com.example.v2.core.StateManager.state.collect { state ->
                when (state.overlayState) {
                    com.example.v2.core.OverlayState.VISIBLE -> {
                        if (Settings.canDrawOverlays(this@HologramBubbleService)) {
                            if (composeView == null) {
                                setupOverlayWindow()
                            }
                        }
                    }
                    com.example.v2.core.OverlayState.SUSPENDED_FOR_PERMISSION, com.example.v2.core.OverlayState.HIDDEN -> {
                        composeView?.let {
                            try {
                                windowManager.removeView(it)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        composeView = null
                    }
                    else -> {}
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!Settings.canDrawOverlays(this)) {
            WifeServiceManager.updateState(WifeServiceState.OVERLAY_PERMISSION_REQUIRED)
            stopSelf()
            return START_NOT_STICKY
        }

        try {
            startHologramForeground()
            if (composeView == null && Settings.canDrawOverlays(this)) {
                com.example.v2.core.StateManager.updateState { it.copy(overlayState = com.example.v2.core.OverlayState.VISIBLE) }
            }
            WifeServiceManager.updateState(WifeServiceState.SERVICE_RUNNING)
        } catch (e: Exception) {
            e.printStackTrace()
            WifeServiceManager.updateState(WifeServiceState.SERVICE_ERROR)
            stopSelf()
        }
        return START_STICKY
    }

    private fun setupOverlayWindow() {
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
            y = 300
        }

        composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@HologramBubbleService)
            setViewTreeSavedStateRegistryOwner(this@HologramBubbleService)
            setContent {
                GlowingHologramBubble(
                    onDrag = { dx, dy ->
                        params.x += dx.toInt()
                        params.y += dy.toInt()
                        windowManager.updateViewLayout(this, params)
                    },
                    onClick = {
                        com.example.v2.core.StateManager.triggerVoiceToggle()
                    }
                )
            }
        }

        windowManager.addView(composeView, params)
    }

    private fun startHologramForeground() {
        val channelId = "hologram_service_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Wife AI Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        // Create initial notification
        val notification = createStatusNotification("Initializing...")
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1002, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(1002, notification)
        }

        // Observe state for updates
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
            com.example.v2.core.StateManager.state.collect { state ->
                val geminiStatus = when(state.geminiState) {
                    com.example.v2.core.GeminiConnectionState.CONNECTED -> "CONNECTED"
                    com.example.v2.core.GeminiConnectionState.CONNECTING -> "CONNECTING..."
                    com.example.v2.core.GeminiConnectionState.RECONNECTING -> "RECONNECTING"
                    com.example.v2.core.GeminiConnectionState.FAILED -> "FAILED"
                    else -> "DISCONNECTED"
                }
                val voiceStatus = when(state.voiceSessionState) {
                    com.example.v2.core.VoiceSessionState.CONNECTED -> "READY"
                    com.example.v2.core.VoiceSessionState.LISTENING -> "LISTENING"
                    com.example.v2.core.VoiceSessionState.THINKING -> "THINKING"
                    com.example.v2.core.VoiceSessionState.SPEAKING -> "SPEAKING"
                    else -> "DISCONNECTED"
                }
                val manager = getSystemService(NotificationManager::class.java)
                manager.notify(1002, createStatusNotification("Status: $voiceStatus | AI: $geminiStatus"))
            }
        }
    }

    private fun createStatusNotification(contentText: String): Notification {
        return NotificationCompat.Builder(this, "hologram_service_channel")
            .setContentTitle("Wife AI Assistant")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        composeView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        composeView = null
        if (WifeServiceManager.serviceState.value == WifeServiceState.SERVICE_RUNNING || WifeServiceManager.serviceState.value == WifeServiceState.SERVICE_STARTING) {
            WifeServiceManager.updateState(WifeServiceState.SERVICE_STOPPED)
        }
    }
}
