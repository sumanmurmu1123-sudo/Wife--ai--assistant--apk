package com.example.v2.core.diagnostics

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Build
import android.os.Vibrator
import android.os.VibratorManager
import androidx.biometric.BiometricManager
import com.example.v2.core.StateManager

class HardwareCapabilityManager(private val context: Context) {

    fun updateCapabilities() {
        val pm = context.packageManager
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        
        val hasMic = pm.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)
        val hasSpeaker = true // Basic assumption, but could check for audio output devices
        val hasBT = pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)
        val hasCamera = pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
        val hasGps = pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)
        val hasTelephony = pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)
        
        val hasVibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator.hasVibrator()
        } else {
            val v = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            v.hasVibrator()
        }

        val biometricManager = BiometricManager.from(context)
        val hasBiometrics = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) != BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE

        val isLowRam = am.isLowRamDevice
        
        val hasOverlay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.provider.Settings.canDrawOverlays(context)
        } else {
            true
        }

        StateManager.updateState { it.copy(
            hasMicrophone = hasMic,
            hasSpeaker = hasSpeaker,
            hasBluetooth = hasBT,
            hasCamera = hasCamera,
            hasGps = hasGps,
            hasVibrator = hasVibrator,
            hasBiometrics = hasBiometrics,
            hasTelephony = hasTelephony,
            hasOverlaySupport = true, // We assume support if permission can be requested
            isLowRamDevice = isLowRam,
            overlayPermissionGranted = hasOverlay
        ) }
    }
}
