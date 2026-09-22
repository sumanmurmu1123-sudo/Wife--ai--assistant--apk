package com.example.v2.audio

import kotlin.math.sqrt

/**
 * A simple energy-based Voice Activity Detector (VAD).
 * Calculates the Root Mean Square (RMS) of the PCM buffer and compares it against a threshold.
 */
class VoiceActivityDetector {

    private var threshold = 1000f // Default threshold for RMS
    private var silenceCounter = 0
    private val silenceThreshold = 10 // Number of silent chunks before considering it "Silence"

    fun setThreshold(newThreshold: Float) {
        this.threshold = newThreshold
    }

    /**
     * Analyzes a PCM ByteArray and returns true if speech is detected.
     */
    fun isSpeechDetected(pcmData: ByteArray): Boolean {
        if (pcmData.isEmpty()) return false

        // Convert byte array to shorts for analysis (PCM 16-bit)
        val shortArray = ShortArray(pcmData.size / 2)
        for (i in shortArray.indices) {
            val low = pcmData[i * 2].toInt() and 0xFF
            val high = pcmData[i * 2 + 1].toInt() shl 8
            shortArray[i] = (low or high).toShort()
        }

        val rms = calculateRms(shortArray)
        
        return if (rms > threshold) {
            silenceCounter = 0
            true
        } else {
            silenceCounter++
            silenceCounter < silenceThreshold // Stay "active" for a small tail to avoid abrupt cuts
        }
    }

    private fun calculateRms(shorts: ShortArray): Float {
        var sum = 0.0
        for (s in shorts) {
            sum += s.toDouble() * s.toDouble()
        }
        val avg = sum / shorts.size
        return sqrt(avg).toFloat()
    }
}
