package com.example.domain.security

import android.content.Context
import android.util.Log

class IntruderCaptureService(
    private val context: Context,
    private val lostPhoneDefenseEngine: LostPhoneDefenseEngine
) {

    fun triggerIntruderLockdown(onAlertComplete: () -> Unit) {
        Log.w("VoiceGuardian", "ALERT: Unauthorized voice detected! Activating lock.")

        // 1. Instantly Lock Screen via integration
        lostPhoneDefenseEngine.triggerDeviceLock("IntruderCaptureService (Voice Guardian)")

        // 2. Play warning or go completely silent based on configuration
        onAlertComplete()
    }
}
