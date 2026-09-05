package com.example.domain

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sqrt

class WakeWordDetector(private val onWakeWordDetected: () -> Unit) {

    private var detectorJob: Job? = null
    private val sampleRate = 16000
    private val bufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )

    @SuppressLint("MissingPermission")
    fun startListening(scope: CoroutineScope) {
        detectorJob?.cancel()
        detectorJob = scope.launch(Dispatchers.IO) {
            val recorder = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )

            val buffer = ShortArray(bufferSize / 2)
            recorder.startRecording()

            while (isActive) {
                val readCount = recorder.read(buffer, 0, buffer.size)
                if (readCount > 0) {
                    // Compute Root Mean Square (RMS) for dynamic energy detection
                    var sum = 0.0
                    for (i in 0 until readCount) {
                        sum += buffer[i] * buffer[i]
                    }
                    val rms = sqrt(sum / readCount)
                    
                    // Energy floor triggering keyword pipeline
                    if (rms > 3800) { 
                        launch(Dispatchers.Main) {
                            onWakeWordDetected()
                        }
                        break // Hand over audio stream to Gemini Live
                    }
                }
            }
            recorder.stop()
            recorder.release()
        }
    }

    fun stopListening() {
        detectorJob?.cancel()
    }
}
