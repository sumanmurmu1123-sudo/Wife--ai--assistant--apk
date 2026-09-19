package com.example.v2.core.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class SecureStorage(context: Context) {
    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    
    private val sharedPreferences = EncryptedSharedPreferences.create(
        "secure_wife_prefs",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveApiKey(key: String) {
        sharedPreferences.edit().putString(KEY_GEMINI_API_KEY, key.trim()).apply()
    }

    fun getApiKey(): String? {
        return sharedPreferences.getString(KEY_GEMINI_API_KEY, null)
    }

    fun clearApiKey() {
        sharedPreferences.edit().remove(KEY_GEMINI_API_KEY).apply()
    }

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

    companion object {
        private const val KEY_GEMINI_API_KEY = "gemini_api_key"
        private const val KEY_ELEVENLABS_API_KEY = "elevenlabs_api_key"
        private const val KEY_ELEVENLABS_VOICE_ID = "elevenlabs_voice_id"
    }
}
