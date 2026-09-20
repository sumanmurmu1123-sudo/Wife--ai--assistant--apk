package com.example.v2.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.audiofx.AcousticEchoCanceler
import android.media.audiofx.NoiseSuppressor
import android.media.MediaRecorder
import android.util.Log
import com.example.v2.core.StateManager
import com.example.v2.core.MicrophoneState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlin.coroutines.coroutineContext

class AudioCaptureManager {

    private var audioRecord: AudioRecord? = null
    private var aec: AcousticEchoCanceler? = null
    private var ns: NoiseSuppressor? = null
    private var currentSampleRate = 16000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    @SuppressLint("MissingPermission")
    fun startCapture(): Flow<ByteArray> = flow {
        // Probe for sample rate - Gemini expects 16000 for input
        val sampleRates = listOf(16000, 48000, 44100, 24000, 8000)
        var initialized = false
        var activeBufferSize = 4096
        
        for (rate in sampleRates) {
            val minBufferSize = AudioRecord.getMinBufferSize(rate, channelConfig, audioFormat)
            if (minBufferSize <= 0) continue
            
            activeBufferSize = minBufferSize * 2
            val sources = listOf(
                MediaRecorder.AudioSource.VOICE_RECOGNITION,
                MediaRecorder.AudioSource.MIC,
                MediaRecorder.AudioSource.DEFAULT
            )
            
            for (source in sources) {
                try {
                    audioRecord = AudioRecord(source, rate, channelConfig, audioFormat, activeBufferSize)
                    if (audioRecord?.state == AudioRecord.STATE_INITIALIZED) {
                        currentSampleRate = rate
                        initialized = true
                        Log.d("VoiceDiag", "MIC_INIT: Success rate=$rate, source=$source, buffer=$activeBufferSize")
                        break
                    } else {
                        audioRecord?.release()
                        audioRecord = null
                    }
                } catch (e: Exception) {
                    Log.e("VoiceDiag", "MIC_INIT: Failed $rate/$source: ${e.message}")
                }
            }
            if (initialized) break
        }

        if (!initialized || audioRecord == null) {
            throw IllegalStateException("AudioRecord failed to initialize on any supported configuration")
        }

        StateManager.updateState { it.copy(micState = MicrophoneState.RECORDING, micAvailable = true) }
        android.util.Log.i("VoicePipeline", "STAGE 2: AudioRecord INITIALIZED. Rate: $currentSampleRate, Source: ${audioRecord?.audioSource}")

        try {
            val audioSessionId = audioRecord?.audioSessionId ?: -1
            if (audioSessionId != -1) {
                if (AcousticEchoCanceler.isAvailable()) {
                    aec = AcousticEchoCanceler.create(audioSessionId)
                    aec?.enabled = true
                    Log.d("VoiceDiag", "ECHO_CONTROL: AcousticEchoCanceler enabled")
                }
                if (NoiseSuppressor.isAvailable()) {
                    ns = NoiseSuppressor.create(audioSessionId)
                    ns?.enabled = true
                    Log.d("VoiceDiag", "NOISE_SUPPRESSION: NoiseSuppressor enabled")
                }
            }
        } catch (e: Exception) {
            Log.e("VoiceDiag", "AUDIO_FX: Failed to initialize audio effects: ${e.message}")
        }

        android.util.Log.i("VoicePipeline", "STAGE 3: AudioRecord RECORDING. Status: ${audioRecord?.recordingState}")
        audioRecord?.startRecording()
        
        try {
            val buffer = ByteArray(activeBufferSize)
            var totalCaptured = 0L
            while (coroutineContext.isActive) {
                val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                if (read > 0) {
                    if (totalCaptured == 0L) {
                        android.util.Log.i("VoicePipeline", "STAGE 4: PCM chunk captured. Size: $read")
                    }
                    totalCaptured += read
                    emit(buffer.copyOf(read))
                }
            }
        } finally {
            android.util.Log.d("VoiceDiag", "MIC_STOPPED: Stopping recording loop")
            stopCapture()
        }
    }.flowOn(Dispatchers.IO)

    fun stopCapture() {
        android.util.Log.d("VoiceDiag", "AUDIO_CAPTURE: Releasing AudioRecord")
        audioRecord?.stop()
        audioRecord?.release()
        aec?.release()
        aec = null
        ns?.release()
        ns = null
        audioRecord = null
    }
}
