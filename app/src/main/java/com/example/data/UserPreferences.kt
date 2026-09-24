package com.example.data

import android.content.Context
import android.content.SharedPreferences

class UserPreferences(private val context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("wife_v2_prefs", Context.MODE_PRIVATE)

    var bossName: String
        get() = prefs.getString("boss_name", "Sujithero") ?: "Sujithero"
        set(value) = prefs.edit().putString("boss_name", value.trim()).apply()

    var assistantName: String
        get() = prefs.getString("assistant_name", "Maya") ?: "Maya"
        set(value) = prefs.edit().putString("assistant_name", value.trim()).apply()

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

    var languageMode: String
        get() = prefs.getString("language_mode", "AUTO_DETECT") ?: "AUTO_DETECT"
        set(value) = prefs.edit().putString("language_mode", value).apply()

    var preferredLanguage: String
        get() = prefs.getString("preferred_language", "Bengali") ?: "Bengali"
        set(value) = prefs.edit().putString("preferred_language", value).apply()

    var userHobbies: String
        get() = prefs.getString("user_hobbies", "Coding, Gaming") ?: "Coding, Gaming"
        set(value) = prefs.edit().putString("user_hobbies", value).apply()

    var relationshipStatus: String
        get() = prefs.getString("relationship_status", "Married") ?: "Married"
        set(value) = prefs.edit().putString("relationship_status", value).apply()

    var neuralVoiceEnabled: Boolean
        get() = prefs.getBoolean("neural_voice_enabled", true)
        set(value) = prefs.edit().putBoolean("neural_voice_enabled", value).apply()

    var sweetTalkEnabled: Boolean
        get() = prefs.getBoolean("sweet_talk_engine", true)
        set(value) = prefs.edit().putBoolean("sweet_talk_engine", value).apply()

    var attitudeEngineEnabled: Boolean
        get() = prefs.getBoolean("attitude_engine", true)
        set(value) = prefs.edit().putBoolean("attitude_engine", value).apply()

    var jealousyEngineEnabled: Boolean
        get() = prefs.getBoolean("jealousy_engine", true)
        set(value) = prefs.edit().putBoolean("jealousy_engine", value).apply()

    var loveStoryEngineEnabled: Boolean
        get() = prefs.getBoolean("love_story_engine", true)
        set(value) = prefs.edit().putBoolean("love_story_engine", value).apply()

    var laughterEngineEnabled: Boolean
        get() = prefs.getBoolean("laughter_engine", true)
        set(value) = prefs.edit().putBoolean("laughter_engine", value).apply()

    var antiDrinkEngineEnabled: Boolean
        get() = prefs.getBoolean("anti_drink_engine", true)
        set(value) = prefs.edit().putBoolean("anti_drink_engine", value).apply()

    var proactiveEngineEnabled: Boolean
        get() = prefs.getBoolean("proactive_engine", true)
        set(value) = prefs.edit().putBoolean("proactive_engine", value).apply()

    var socialMediaEngineEnabled: Boolean
        get() = prefs.getBoolean("social_media_engine", true)
        set(value) = prefs.edit().putBoolean("social_media_engine", value).apply()

    var socialMode: Boolean
        get() = prefs.getBoolean("social_mode", false)
        set(value) = prefs.edit().putBoolean("social_mode", value).apply()

    var autoReply: Boolean
        get() = prefs.getBoolean("auto_reply", false)
        set(value) = prefs.edit().putBoolean("auto_reply", value).apply()

    var airGesturesEnabled: Boolean
        get() = prefs.getBoolean("air_gestures", false)
        set(value) = prefs.edit().putBoolean("air_gestures", value).apply()

    var clapDetectorEnabled: Boolean
        get() = prefs.getBoolean("clap_detector", true)
        set(value) = prefs.edit().putBoolean("clap_detector", value).apply()

    var cameraVisionEnabled: Boolean
        get() = prefs.getBoolean("camera_vision", true)
        set(value) = prefs.edit().putBoolean("camera_vision", value).apply()

    var floatingHologramEnabled: Boolean
        get() = prefs.getBoolean("floating_hologram", false)
        set(value) = prefs.edit().putBoolean("floating_hologram", value).apply()

    var interactiveWallpaperEnabled: Boolean
        get() = prefs.getBoolean("interactive_wallpaper", false)
        set(value) = prefs.edit().putBoolean("interactive_wallpaper", value).apply()

    var flashlightBatteryEnabled: Boolean
        get() = prefs.getBoolean("flashlight_battery", true)
        set(value) = prefs.edit().putBoolean("flashlight_battery", value).apply()

    var officeAssistantEnabled: Boolean
        get() = prefs.getBoolean("office_assistant", true)
        set(value) = prefs.edit().putBoolean("office_assistant", value).apply()

    var hyperSpeedMode: Boolean
        get() = prefs.getBoolean("hyper_speed_mode", true)
        set(value) = prefs.edit().putBoolean("hyper_speed_mode", value).apply()

    var firstGreetingEnabled: Boolean
        get() = prefs.getBoolean("first_greeting_engine", true)
        set(value) = prefs.edit().putBoolean("first_greeting_engine", value).apply()

    var vadEnabled: Boolean
        get() = prefs.getBoolean("vad_enabled", true)
        set(value) = prefs.edit().putBoolean("vad_enabled", value).apply()

    var vadSensitivity: Float
        get() = prefs.getFloat("vad_sensitivity", 500f)
        set(value) = prefs.edit().putFloat("vad_sensitivity", value).apply()

    var advancedDebugging: Boolean
        get() = prefs.getBoolean("advanced_debugging", true)
        set(value) = prefs.edit().putBoolean("advanced_debugging", value).apply()

    var pcIp: String
        get() = prefs.getString("pc_ip", "192.168.1.100") ?: "192.168.1.100"
        set(value) = prefs.edit().putString("pc_ip", value).apply()

    var backendUrl: String
        get() = prefs.getString("backend_url", "https://your-fastapi-backend.com") ?: "https://your-fastapi-backend.com"
        set(value) = prefs.edit().putString("backend_url", value.trim()).apply()

    var backendEnabled: Boolean
        get() = prefs.getBoolean("backend_enabled", false)
        set(value) = prefs.edit().putBoolean("backend_enabled", value).apply()

    var geminiApiKey: String
        get() = com.example.v2.core.security.SecureStorage(context).getApiKey() ?: ""
        set(value) = com.example.v2.core.security.SecureStorage(context).saveApiKey(value)

    var elevenLabsApiKey: String
        get() = com.example.v2.core.security.SecureStorage(context).getElevenLabsKey() ?: ""
        set(value) = com.example.v2.core.security.SecureStorage(context).saveElevenLabsKey(value)

    var preferredEngineerName: String
        get() = prefs.getString("KEY_ENGINEER_NAME", "SujitHero") ?: "SujitHero"
        set(value) = prefs.edit().putString("KEY_ENGINEER_NAME", value.trim()).apply()

    var preferredEngineerPhone: String
        get() = prefs.getString("KEY_ENGINEER_PHONE", "+91 0000000000") ?: "+91 0000000000"
        set(value) = prefs.edit().putString("KEY_ENGINEER_PHONE", value.trim()).apply()
}
