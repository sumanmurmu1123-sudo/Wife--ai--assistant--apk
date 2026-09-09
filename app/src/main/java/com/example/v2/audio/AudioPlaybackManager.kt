package com.example.v2.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AudioPlaybackManager {

    private var audioTrack: AudioTrack? = null
    private val sampleRate = 24000 // Gemini often returns 24kHz. Let's use 24kHz as default output.
    private val channelConfig = AudioFormat.CHANNEL_OUT_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    init {
        val minBufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(audioFormat)
                    .setSampleRate(sampleRate)
                    .setChannelMask(channelConfig)
                    .build()
            )
            .setBufferSizeInBytes(minBufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()
    }

    suspend fun playChunk(pcmData: ByteArray) = withContext(Dispatchers.IO) {
        if (audioTrack?.playState != AudioTrack.PLAYSTATE_PLAYING) {
            audioTrack?.play()
        }
        audioTrack?.write(pcmData, 0, pcmData.size)
    }

    fun stopPlayback() {
        if (audioTrack?.playState == AudioTrack.PLAYSTATE_PLAYING) {
            audioTrack?.pause()
            audioTrack?.flush()
        }
    }

    fun release() {
        audioTrack?.release()
        audioTrack = null
    }
}
