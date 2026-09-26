package com.example.v2.core.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class SecureStorage(context: Context) {
    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    
    private val sharedPreferences = EncryptedSharedPreferences.create(
        "secure_maya_prefs",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveSessionToken(token: String) {
        sharedPreferences.edit().putString(KEY_SESSION_TOKEN, token.trim()).apply()
    }

    fun getSessionToken(): String? {
        return sharedPreferences.getString(KEY_SESSION_TOKEN, null)
    }

    fun clearSessionToken() {
        sharedPreferences.edit().remove(KEY_SESSION_TOKEN).apply()
    }

    // Legacy: Gemini API Key is now handled server-side in Maya V2
    fun saveApiKey(key: String) { /* No-op in V2 */ }
    fun getApiKey(): String? = null
    fun clearApiKey() { /* No-op in V2 */ }

    fun saveElevenLabsKey(key: String) {
        sharedPreferences.edit().putString(KEY_ELEVENLABS_API_KEY, key.trim()).apply()
    }

    fun getElevenLabsKey(): String? {
        return sharedPreferences.getString(KEY_ELEVENLABS_API_KEY, null)
    }

    fun clearElevenLabsKey() {
        sharedPreferences.edit().remove(KEY_ELEVENLABS_API_KEY).apply()
    }

    fun saveElevenLabsVoiceId(voiceId: String) {
        sharedPreferences.edit().putString(KEY_ELEVENLABS_VOICE_ID, voiceId.trim()).apply()
    }

    fun getElevenLabsVoiceId(): String {
        return sharedPreferences.getString(KEY_ELEVENLABS_VOICE_ID, "21m00Tcm4TlvDq8ikWAM") ?: "21m00Tcm4TlvDq8ikWAM"
    }

    fun saveWeatherApiKey(key: String) {
        sharedPreferences.edit().putString(KEY_WEATHER_API_KEY, key.trim()).apply()
    }

    fun getWeatherApiKey(): String? {
        return sharedPreferences.getString(KEY_WEATHER_API_KEY, null)
    }

    fun clearWeatherApiKey() {
        sharedPreferences.edit().remove(KEY_WEATHER_API_KEY).apply()
    }

    companion object {
        private const val KEY_SESSION_TOKEN = "maya_session_token"
        private const val KEY_ELEVENLABS_API_KEY = "elevenlabs_api_key"
        private const val KEY_ELEVENLABS_VOICE_ID = "elevenlabs_voice_id"
        private const val KEY_WEATHER_API_KEY = "weather_api_key"
    }
}
