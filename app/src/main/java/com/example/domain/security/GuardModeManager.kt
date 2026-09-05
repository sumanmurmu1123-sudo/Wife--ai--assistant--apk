package com.example.domain.security

import android.content.Context
import android.content.SharedPreferences

class GuardModeManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("voice_guardian_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_GUARD_MODE = "is_guard_mode_active"
        private const val KEY_MASTER_PIN = "master_voice_pin"
        private const val KEY_SILENT_MODE = "silent_reject"
    }

    var isGuardModeActive: Boolean
        get() = prefs.getBoolean(KEY_GUARD_MODE, false)
        set(value) = prefs.edit().putBoolean(KEY_GUARD_MODE, value).apply()

    fun setMasterPin(pin: String) {
        prefs.edit().putString(KEY_MASTER_PIN, pin).apply()
    }

    fun verifyPasscode(input: String): Boolean {
        val masterPin = prefs.getString(KEY_MASTER_PIN, "sujit@hero")
        return input.trim().equals(masterPin, ignoreCase = true)
    }
}
