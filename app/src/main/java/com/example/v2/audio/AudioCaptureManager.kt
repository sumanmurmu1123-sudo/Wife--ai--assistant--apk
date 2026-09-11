package com.example.v2.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
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
    private val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
    private val bufferSize = if (minBufferSize > 0) minBufferSize * 2 else 4096

    @SuppressLint("MissingPermission")
    fun startCapture(): Flow<ByteArray> = flow {
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize
        )

        if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
            throw IllegalStateException("AudioRecord initialization failed")
        }

        audioRecord?.startRecording()
        
        val buffer = ByteArray(bufferSize)
        while (coroutineContext.isActive) {
            val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
            if (read > 0) {
                // Return a copy of the read data
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
