package com.example.audio

import android.media.AudioManager
import android.media.ToneGenerator

object SciFiSoundSynthesizer {
    fun playFuturisticChirp() {
        try {
            val toneGen = ToneGenerator(AudioManager.STREAM_ALARM, 100)
            toneGen.startTone(ToneGenerator.TONE_PROP_BEEP2, 150)
            // In a real app, load a sci-fi sound pool here.
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
