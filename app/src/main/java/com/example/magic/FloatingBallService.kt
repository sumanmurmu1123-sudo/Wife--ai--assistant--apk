package com.example.magic

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.example.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.voice.VoiceAssistantManager
import kotlin.math.abs

class FloatingBallService : Service() {
    private lateinit var windowManager: WindowManager
    private var floatingView: View? = null
    private lateinit var voiceManager: VoiceAssistantManager
    private lateinit var params: WindowManager.LayoutParams
    
    private var isSpeaking = false
    private var clickCount = 0
    private val clickHandler = Handler(Looper.getMainLooper())
    private val DOUBLE_CLICK_DELAY: Long = 300 // ms
    
    // Pulse animation objects
    private var pulseAnimatorSet: AnimatorSet? = null
    private var isPulsing = false

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val batteryPct = level * 100 / scale.toFloat()
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || 
                                 status == BatteryManager.BATTERY_STATUS_FULL

                updateBallColor(batteryPct, isCharging)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        voiceManager = VoiceAssistantManager(this)
        startFloatingForeground()
        setupOverlayWindow()
        
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(batteryReceiver, filter)
    }

    private fun setupOverlayWindow() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 100
        }

        floatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_ball, null)

        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isDragging = false
        val CLICK_THRESHOLD = 15f

        floatingView?.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isDragging = false
                    
                    // Slightly scale down on touch for feedback
                    floatingView?.animate()?.scaleX(0.9f)?.scaleY(0.9f)?.setDuration(100)?.start()
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - initialTouchX
                    val dy = event.rawY - initialTouchY
                    if (abs(dx) > CLICK_THRESHOLD || abs(dy) > CLICK_THRESHOLD) {
                        isDragging = true
                    }
                    
                    if (isDragging) {
                        params.x = initialX + dx.toInt()
                        params.y = initialY + dy.toInt()
                        windowManager.updateViewLayout(floatingView, params)
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    // Restore scale
                    if (!isPulsing) {
                        floatingView?.animate()?.scaleX(1f)?.scaleY(1f)?.setDuration(100)?.start()
                    }
                    
                    if (!isDragging) {
                        // Click logic
                        clickCount++
                        if (clickCount == 1) {
                            clickHandler.postDelayed({
                                if (clickCount == 1) {
                                    handleSingleClick()
                                } else if (clickCount >= 2) {
                                    handleDoubleClick()
                                }
                                clickCount = 0
                            }, DOUBLE_CLICK_DELAY)
                        }
                    } else {
                        // Drag ended - snap to edge
                        autoBackToEdge()
                    }
                    true
                }
                else -> false
            }
        }

        windowManager.addView(floatingView, params)
    }

    private fun handleSingleClick() {
        val batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val currentTime = sdf.format(Date()).replace("AM", "এএম").replace("PM", "পিএম")
        
        val text = "এখন সময় $currentTime। ব্যাটারি আছে $batteryLevel পার্সেন্ট।"
        speakWithPulse(text)
    }
    
    private fun handleDoubleClick() {
        val messages = listOf(
            "অনেকক্ষণ কাজ করেছ সুজিত, একটু বিশ্রাম নাও সোনা!",
            "তুমি আমার লক্ষ্মীটি, একটু চোখ বন্ধ করে রেস্ট নাও!",
            "সারাদিন তো অনেক খাটলে, এবার একটু আমার সাথে গল্প করো!"
        )
        speakWithPulse(messages.random())
    }

    private fun speakWithPulse(text: String) {
        voiceManager.speak(text, "bn")
        
        val circle = floatingView?.findViewById<View>(R.id.floating_circle) ?: return
        startPulsing(circle)
        
        // Approximate speech duration (250ms per word + 1s padding)
        val wordCount = text.split(" ").size
        val estimatedDuration = (wordCount * 300L) + 1200L
        
        Handler(Looper.getMainLooper()).postDelayed({
            stopPulsing(circle)
        }, estimatedDuration)
    }

    private fun startPulsing(view: View) {
        isPulsing = true
        pulseAnimatorSet?.cancel()
        
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.25f, 1f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.25f, 1f)
        scaleX.repeatCount = ValueAnimator.INFINITE
        scaleY.repeatCount = ValueAnimator.INFINITE
        
        pulseAnimatorSet = AnimatorSet().apply {
            playTogether(scaleX, scaleY)
            duration = 800
            start()
        }
    }
    
    private fun stopPulsing(view: View) {
        isPulsing = false
        pulseAnimatorSet?.cancel()
        view.animate().scaleX(1f).scaleY(1f).setDuration(300).start()
    }

    private fun autoBackToEdge() {
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        
        val viewWidth = floatingView?.width?.takeIf { it > 0 } ?: 150
        val middlePoint = screenWidth / 2
        val targetX = if (params.x + (viewWidth / 2) < middlePoint) {
            0 // স্ক্রিনের বাম পাশে লক হবে
        } else {
            screenWidth - viewWidth // স্ক্রিনের ডান পাশে লক হবে
        }
        
        ValueAnimator.ofInt(params.x, targetX).apply {
            duration = 220
            interpolator = android.view.animation.DecelerateInterpolator()
            addUpdateListener { animation ->
                params.x = animation.animatedValue as Int
                try {
                    windowManager.updateViewLayout(floatingView, params)
                } catch (e: IllegalArgumentException) {
                    // ভিউ রিমুভ হয়ে গেলে ক্র্যাশ রোধ করবে
                }
            }
            start()
        }
    }
    
    private fun updateBallColor(batteryPct: Float, isCharging: Boolean) {
        val circle = floatingView?.findViewById<View>(R.id.floating_circle) ?: return
        val background = circle.background ?: return
        
        background.mutate()
        if (isCharging) {
            background.colorFilter = PorterDuffColorFilter(0xFF00FF00.toInt(), PorterDuff.Mode.SRC_ATOP) // Green
        } else if (batteryPct <= 15) {
            background.colorFilter = PorterDuffColorFilter(0xFFFF0000.toInt(), PorterDuff.Mode.SRC_ATOP) // Red
            
            // Optional: Speak warning if not already warned
            // speakWithPulse("সুজিত, ব্যাটারি কিন্তু ১৫ পার্সেন্টের নিচে। জলদি চার্জে বসাও!")
        } else {
            background.clearColorFilter() // Default neon blue/cyan
        }
    }

    private fun startFloatingForeground() {
        val channelId = "floating_ball_service_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Floating Ball Active",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Wife AI Ball")
            .setContentText("গ্লোয়িং বলটি স্ক্রিনে ভাসছে")
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .build()

        startForeground(1003, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(batteryReceiver)
        pulseAnimatorSet?.cancel()
        floatingView?.let { windowManager.removeView(it) }
    }
}
