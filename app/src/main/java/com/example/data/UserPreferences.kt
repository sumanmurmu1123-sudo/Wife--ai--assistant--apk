package com.example.data

import android.content.Context
import android.content.SharedPreferences

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("wife_user_prefs", Context.MODE_PRIVATE)

    var bossName: String
        get() = prefs.getString("KEY_BOSS_NAME", "Boss") ?: "Boss"
        set(value) = prefs.edit().putString("KEY_BOSS_NAME", value.trim()).apply()

    var assistantName: String
        get() = prefs.getString("KEY_ASSISTANT_NAME", "Wife") ?: "Wife"
        set(value) = prefs.edit().putString("KEY_ASSISTANT_NAME", value.trim()).apply()

    var selectedVoiceSlate: VoiceSlate
        get() {
            val savedName = prefs.getString("KEY_VOICE_SLATE", VoiceSlate.AOEDE.name)
            return try {
                VoiceSlate.valueOf(savedName ?: VoiceSlate.AOEDE.name)
            } catch (e: Exception) {
                VoiceSlate.AOEDE
            }
        }
        set(value) = prefs.edit().putString("KEY_VOICE_SLATE", value.name).apply()

    var geminiApiKey: String
        get() = prefs.getString("KEY_GEMINI_API_KEY", "") ?: ""
        set(value) = prefs.edit().putString("KEY_GEMINI_API_KEY", value.trim()).apply()
        
    var elevenLabsApiKey: String
        get() = prefs.getString("KEY_ELEVENLABS_API_KEY", "") ?: ""
        set(value) = prefs.edit().putString("KEY_ELEVENLABS_API_KEY", value.trim()).apply()
}
