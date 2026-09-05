package com.example.wallpaper

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.MotionEvent
import android.view.SurfaceHolder

class TouchWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return InteractiveTouchEngine()
    }

    inner class InteractiveTouchEngine : Engine() {
        private val handler = Handler(Looper.getMainLooper())
        private var visible = false

        // টাচ পয়েন্ট ও অ্যানিমেশন ভ্যারিয়েবল
        private var touchX = -1f
        private var touchY = -1f
        private var rippleRadius = 0f
        private val maxRadius = 180f

        // পেইন্ট অবজেক্ট
        private val bgPaint = Paint().apply {
            color = Color.parseColor("#0F172A") // ডার্ক ব্যাকগ্রাউন্ড
        }
        private val ripplePaint = Paint().apply {
            color = Color.parseColor("#38BDF8") // গ্লোয়িং ব্লু সার্কেল
            style = Paint.Style.STROKE
            strokeWidth = 6f
            isAntiAlias = true
        }
        private val textPaint = Paint().apply {
            color = Color.parseColor("#94A3B8")
            textSize = 36f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        private val drawRunnable = Runnable { drawFrame() }

        override fun onVisibilityChanged(visible: Boolean) {
            this.visible = visible
            if (visible) {
                drawFrame()
            } else {
                handler.removeCallbacks(drawRunnable)
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder?) {
            super.onSurfaceDestroyed(holder)
            visible = false
            handler.removeCallbacks(drawRunnable)
        }

        // স্ক্রিন টাচ ইভেন্ট
        override fun onTouchEvent(event: MotionEvent) {
            if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
                touchX = event.x
                touchY = event.y
                rippleRadius = 10f // নতুন করে টাচ রিপল শুরু
                drawFrame()
            }
            super.onTouchEvent(event)
        }

        private fun drawFrame() {
            val holder = surfaceHolder
            var canvas: Canvas? = null

            try {
                canvas = holder.lockCanvas()
                if (canvas != null) {
                    // ব্যাকগ্রাউন্ড ড্র করা
                    canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), bgPaint)

                    // টাচ রিপল ইফেক্ট ড্র করা
                    if (touchX >= 0 && touchY >= 0 && rippleRadius < maxRadius) {
                        ripplePaint.alpha = ((1f - (rippleRadius / maxRadius)) * 255).toInt()
                        canvas.drawCircle(touchX, touchY, rippleRadius, ripplePaint)
                        rippleRadius += 8f // অ্যানিমেশনের গতি
                    }

                    // নিচে ডেভেলপার নাম প্রদর্শন
                    val footerText = "Interactive Assistant Wallpaper • Sujit"
                    canvas.drawText(
                        footerText,
                        (canvas.width / 2).toFloat(),
                        (canvas.height - 80).toFloat(),
                        textPaint
                    )
                }
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas)
                }
            }

            handler.removeCallbacks(drawRunnable)
            if (visible && rippleRadius < maxRadius && touchX >= 0) {
                handler.postDelayed(drawRunnable, 16) // ~60 FPS অ্যানিমেশন
            }
        }
    }
}
