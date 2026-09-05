package com.example.domain

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.view.WindowManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DrivingModeEngine(private val context: Context) {

    private val _isDrivingModeActive = MutableStateFlow(false)
    val isDrivingModeActive: StateFlow<Boolean> = _isDrivingModeActive

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun toggleDrivingMode(enable: Boolean, activity: Activity? = null): String {
        _isDrivingModeActive.value = enable

        activity?.runOnUiThread {
            if (enable) {
                // Keep screen awake while driving
                activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                audioManager.isSpeakerphoneOn = true
            } else {
                activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                audioManager.isSpeakerphoneOn = false
            }
        }
        
        // If activity is null, we can still set the speakerphone
        if (activity == null) {
            audioManager.isSpeakerphoneOn = enable
        }

        return if (enable) {
            "Driving mode engaged, Boss. Keeping the screen on and speaker active. Drive safely! 🚗💨"
        } else {
            "Exited Driving mode. Welcome back, Boss!"
        }
    }

    fun startNavigation(destination: String): String {
        val encodedDest = Uri.encode(destination)
        val gmmIntentUri = Uri.parse("google.navigation:q=$encodedDest&mode=d")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
            `package` = "com.google.android.apps.maps"
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return try {
            context.startActivity(mapIntent)
            "Starting GPS navigation to $destination. Eyes on the road, Boss!"
        } catch (e: Exception) {
            "Google Maps is not available on this device."
        }
    }
}
