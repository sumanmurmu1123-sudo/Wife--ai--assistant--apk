package com.example.magic

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import com.example.voice.VoiceAssistantManager

class PocketGuardService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var lightSensor: Sensor? = null
    private var proximitySensor: Sensor? = null
    private var voiceManager: VoiceAssistantManager? = null

    private var isInPocket = false

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        voiceManager = VoiceAssistantManager(this)

        lightSensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
        proximitySensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        when (event?.sensor?.type) {
            Sensor.TYPE_PROXIMITY -> {
                val distance = event.values[0]
                // ফোন পকেটে ঢোকালে ডিস্ট্যান্স ০ বা খুব কম হয়
                if (distance == 0f) {
                    isInPocket = true
                }
            }
            Sensor.TYPE_LIGHT -> {
                val lux = event.values[0]
                // পকেটে থাকা অবস্থায় হঠাৎ আলো আসলে (পকেট থেকে বের করলে)
                if (isInPocket && lux > 40f) {
                    isInPocket = false
                    triggerPocketAlarm()
                }
            }
        }
    }

    private fun triggerPocketAlarm() {
        voiceManager?.speak("সতর্কতা! সুজিত, তোমার ফোনটি পকেট থেকে বের করা হয়েছে!", "bn")
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        voiceManager?.shutdown()
    }
}
