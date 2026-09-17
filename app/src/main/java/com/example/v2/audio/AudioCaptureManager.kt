package com.example.v2.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.audiofx.AcousticEchoCanceler
import android.media.audiofx.NoiseSuppressor
import android.media.MediaRecorder
import android.util.Log
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
    private val sampleRate = 16000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    @SuppressLint("MissingPermission")
    fun startCapture(): Flow<ByteArray> = flow {
        var minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        if (minBufferSize == AudioRecord.ERROR_BAD_VALUE || minBufferSize == AudioRecord.ERROR) {
            minBufferSize = 4096 // Fallback
        }
        val bufferSize = minBufferSize * 2
        
        Log.d("VoiceDiag", "MIC_INIT: Attempting to initialize AudioRecord with bufferSize: $bufferSize")
        
        // Try different audio sources
        val sources = listOf(
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
            MediaRecorder.AudioSource.MIC,
            MediaRecorder.AudioSource.DEFAULT
        )
        
        for (source in sources) {
            try {
                audioRecord = AudioRecord(source, sampleRate, channelConfig, audioFormat, bufferSize)
                if (audioRecord?.state == AudioRecord.STATE_INITIALIZED) {
                    Log.d("VoiceDiag", "MIC_INIT: AudioRecord initialized successfully with source: $source")
                    break
                } else {
                    audioRecord?.release()
        aec?.release()
        aec = null
        ns?.release()
        ns = null
                    audioRecord = null
                }
            } catch (e: Exception) {
                Log.e("VoiceDiag", "MIC_INIT: Failed with source $source: ${e.message}")
            }
        }

        if (audioRecord == null || audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
            android.util.Log.e("VoiceDiag", "VOICE_ERROR: AudioRecord initialization failed")
            throw IllegalStateException("AudioRecord initialization failed. No available audio sources.")
        }

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

        android.util.Log.d("VoiceDiag", "MIC_STARTED: Starting recording")
        audioRecord?.startRecording()
        
        try {
            val buffer = ByteArray(bufferSize)
            while (coroutineContext.isActive) {
                val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                if (read > 0) {
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
