package com.example.gesture

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.widget.Toast
import com.example.service.WifeForegroundService

class AirGestureService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var proximitySensor: Sensor? = null

    // হাত নাড়ার সময় গণনার জন্য
    private var lastWaveTime: Long = 0

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

        proximitySensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_PROXIMITY) {
            val distance = event.values[0]
            val maxRange = proximitySensor?.maximumRange ?: 5f

            // যখন ফোনের সেন্সরের সামনে হাত আসবে
            if (distance < maxRange) {
                val currentTime = System.currentTimeMillis()
                
                // দ্রুত হাত নাড়ালে (Wave Gesture)
                if (currentTime - lastWaveTime > 1500) { 
                    lastWaveTime = currentTime
                    onHandWaveDetected()
                }
            }
        }
    }

    private fun onHandWaveDetected() {
        Toast.makeText(this, "👋 Magic Wave Detected by Sujit's Assistant!", Toast.LENGTH_SHORT).show()
        
        // দূর থেকে হাত নাড়লে অ্যাসিস্ট্যান্ট রেসপন্স করবে
        val intent = Intent(this, WifeForegroundService::class.java).apply {
            action = WifeForegroundService.ACTION_START_SESSION
        }
        startService(intent)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }
}
