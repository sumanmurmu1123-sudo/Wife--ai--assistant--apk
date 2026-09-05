package com.example.slx

import android.content.Context
import android.os.PowerManager
import com.example.audio.SciFiSoundSynthesizer
import com.example.voice.VoiceAssistantManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object SlxModManager {

    private val _isSlxActive = MutableStateFlow(false)
    val isSlxActive: StateFlow<Boolean> = _isSlxActive

    private var wakeLock: PowerManager.WakeLock? = null

    /**
     * SLX MOD টগল লজিক
     */
    fun toggleSlxMod(context: Context, voiceManager: VoiceAssistantManager) {
        val nextState = !_isSlxActive.value
        _isSlxActive.value = nextState

        SciFiSoundSynthesizer.playFuturisticChirp()

        if (nextState) {
            activateExtremePerformance(context)
            voiceManager.speak(
                "এসএলএক্স মোড সক্রিয়! টার্বো প্রসেসিং এবং স্টিলথ শিল্ড অনলাইন।",
                "bn"
            )
        } else {
            deactivateExtremePerformance()
            voiceManager.speak(
                "এসএলএক্স মোড নিষ্ক্রিয়। সিস্টেম সাধারণ ক্ষমতায় ফিরে এসেছে।",
                "bn"
            )
        }
    }

    private fun activateExtremePerformance(context: Context) {
        // ১. অপ্রয়োজনীয় র্যাম ক্লিয়ার করা
        System.gc()

        // ২. সিপিইউ পাওয়ার ড্রপ প্রতিরোধে আংশিক ওয়েক-লক
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "Assistant:SLX_TurboPerformanceLock"
        ).apply {
            acquire(10 * 60 * 1000L /* ১০ মিনিটের জন্য সেফ লিমিট */)
        }
    }

    private fun deactivateExtremePerformance() {
        wakeLock?.let {
            if (it.isHeld) it.release()
        }
        wakeLock = null
    }
}
