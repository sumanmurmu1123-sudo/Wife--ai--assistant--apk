package com.example.v2.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AudioPlaybackManager(private val context: Context) {
    private var audioTrack: AudioTrack? = null
    private val sampleRate = 24000 // Gemini often returns 24kHz. Let's use 24kHz as default output.
    private val channelConfig = AudioFormat.CHANNEL_OUT_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null

    init {
        val minBufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()
            
        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(audioAttributes)
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
            
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                .setAudioAttributes(audioAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener { focusChange ->
                    if (focusChange == AudioManager.AUDIOFOCUS_LOSS || focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                        stopPlayback()
                    }
                }
                .build()
        }
    }

    suspend fun playChunk(pcmData: ByteArray) = withContext(Dispatchers.IO) {
        if (audioTrack?.playState != AudioTrack.PLAYSTATE_PLAYING) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && audioFocusRequest != null) {
                val res = audioManager.requestAudioFocus(audioFocusRequest!!)
                if (res != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    return@withContext // Audio focus failed
                }
            }
            audioTrack?.play()
        }
        audioTrack?.write(pcmData, 0, pcmData.size)
    }

    fun stopPlayback() {
        if (audioTrack?.playState == AudioTrack.PLAYSTATE_PLAYING) {
            audioTrack?.pause()
            audioTrack?.flush()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && audioFocusRequest != null) {
            audioManager.abandonAudioFocusRequest(audioFocusRequest!!)
        }
    }

    fun release() {
        stopPlayback()
        audioTrack?.release()
        audioTrack = null
    }
}
