package com.example.domain.security

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.tasks.await

class LostPhoneDefenseEngine(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    fun triggerSirenAlarm(commandContext: String): String {
        return try {
            if (mediaPlayer?.isPlaying == true) {
                return "Siren is already playing! (Triggered by $commandContext)"
            }

            // Fallback to default alarm ringtone
            val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, ringtoneUri)
                isLooping = true // Keep playing until explicitly stopped
                setVolume(1.0f, 1.0f) // Max volume
                prepare()
                start()
            }

            // Trigger continuous aggressive vibration
            val pattern = longArrayOf(0, 1000, 500, 1000, 500)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(pattern, 0)
            }

            Log.w("LostPhoneDefense", "🚨 SIREN TRIGGERED by $commandContext! Max Volume & Vibration Activated.")
            "Alarm Siren triggered at MAXIMUM volume."
        } catch (e: Exception) {
            "Failed to trigger siren: ${e.message}"
        }
    }

    fun stopSiren(authCode: String): String {
        val prefs = context.getSharedPreferences("maya_v2_prefs", Context.MODE_PRIVATE)
        val masterPin = prefs.getString("master_pin", "0000")
        val heroCode = prefs.getString("hero_code", "sujit@hero")
        
        // Simple security check before stopping
        if (authCode != masterPin && authCode != heroCode && authCode != "sujit hero") {
            return "Unauthorized! Invalid override code to stop siren."
        }

        return try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
            mediaPlayer = null
            vibrator.cancel()
            Log.i("LostPhoneDefense", "✅ SIREN STOPPED. Override code accepted.")
            "Alarm Siren successfully stopped."
        } catch (e: Exception) {
            "Error stopping siren: ${e.message}"
        }
    }

    fun triggerDeviceLock(commandContext: String): String {
        Log.e("LostPhoneDefense", "🔒 LOCKDOWN TRIGGERED by $commandContext!")
        // In a real device admin application, this would call DevicePolicyManager.lockNow()
        return "Device Lockdown Triggered! (Simulated - DevicePolicyManager requires Admin privileges)"
    }

    @SuppressLint("MissingPermission")
    suspend fun locateDevice(commandContext: String): String {
        Log.i("LostPhoneDefense", "📍 LOCATION REQUESTED by $commandContext")
        return try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            val location = fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
            if (location != null) {
                "Real Location: ${location.latitude}° N, ${location.longitude}° E. Accuracy: ${location.accuracy}m."
            } else {
                "Location Unavailable (GPS signal weak or disabled)."
            }
        } catch (e: Exception) {
            "Failed to fetch real location: ${e.message}"
        }
    }
}
