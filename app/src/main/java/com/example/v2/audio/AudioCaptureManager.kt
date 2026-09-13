package com.example.v2.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
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
        
        Log.d("AudioCaptureManager", "Attempting to initialize AudioRecord with bufferSize: $bufferSize")
        
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
                    Log.d("AudioCaptureManager", "AudioRecord initialized successfully with source: $source")
                    break
                } else {
                    audioRecord?.release()
                    audioRecord = null
                }
            } catch (e: Exception) {
                Log.e("AudioCaptureManager", "Failed with source $source: ${e.message}")
            }
        }

        if (audioRecord == null || audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
            throw IllegalStateException("AudioRecord initialization failed. No available audio sources.")
        }

        audioRecord?.startRecording()
        
        val buffer = ByteArray(bufferSize)
        while (coroutineContext.isActive) {
            val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
            if (read > 0) {
                emit(buffer.copyOf(read))
            }
        }
    }.flowOn(Dispatchers.IO)

    fun stopCapture() {
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
    }
}
