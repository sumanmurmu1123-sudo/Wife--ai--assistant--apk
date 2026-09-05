package com.example.domain.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs

class ClapDetectorEngine {

    private var isListening = false
    private var audioRecord: AudioRecord? = null
    
    // তালি শনাক্তকরণের জন্য সাউন্ড থ্রেশহোল্ড
    private val CLAP_THRESHOLD = 18000 
    private val SAMPLE_RATE = 44100
    private val BUFFER_SIZE = AudioRecord.getMinBufferSize(
        SAMPLE_RATE,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )

    @SuppressLint("MissingPermission")
    suspend fun startClapListening(onClapDetected: () -> Unit) = withContext(Dispatchers.IO) {
        if (BUFFER_SIZE <= 0) return@withContext

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            BUFFER_SIZE
        )

        audioRecord?.startRecording()
        isListening = true
        val buffer = ShortArray(BUFFER_SIZE)

        while (isListening) {
            val readCount = audioRecord?.read(buffer, 0, BUFFER_SIZE) ?: 0
            if (readCount > 0) {
                var maxAmplitude = 0
                for (i in 0 until readCount) {
                    val amp = abs(buffer[i].toInt())
                    if (amp > maxAmplitude) {
                        maxAmplitude = amp
                    }
                }

                // হঠাৎ তীব্র শব্দ (হাততালি) হলে ট্রিগার হবে
                if (maxAmplitude > CLAP_THRESHOLD) {
                    withContext(Dispatchers.Main) {
                        onClapDetected()
                    }
                    // পরপর অতিরিক্ত ট্রিগার আটকাতে ছোট বিরতি
                    kotlinx.coroutines.delay(600)
                }
            }
        }
    }

    fun stopClapListening() {
        isListening = false
        try {
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
