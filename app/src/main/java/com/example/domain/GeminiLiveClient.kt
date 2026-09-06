package com.example.domain

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.media.AudioManager
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.coroutines.CoroutineContext

class GeminiLiveClient(private val apiKey: String) : CoroutineScope {
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext = Dispatchers.IO + job

    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null

    // Audio settings as per user script
    private val MIC_RATE = 16000
    private val SPK_RATE = 24000
    private val CHUNK_SIZE = 320 * 2 // roughly 320 frames, *2 for 16-bit

    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null
    private var isRecording = false

    val isConnected = MutableStateFlow(false)

    fun startSession(instruction: String) {
        val request = Request.Builder()
            .url("wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.v1alpha.GenerativeService.BidiGenerateContent?key=\$apiKey")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("GeminiLive", "WebSocket Opened")
                isConnected.value = true
                sendSetupMessage(instruction)
                startAudioIO()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                // Log.d("GeminiLive", "Message received: \$text")
                handleServerMessage(text)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("GeminiLive", "WebSocket Closed: \$reason")
                isConnected.value = false
                stopAudioIO()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("GeminiLive", "WebSocket Failure", t)
                isConnected.value = false
                stopAudioIO()
            }
        })
    }

    private fun sendSetupMessage(instruction: String) {
        try {
            val setupJson = JSONObject().apply {
                put("setup", JSONObject().apply {
                    put("model", "models/gemini-2.0-flash-exp") // Fallback to 2.0 flash exp for stability or use the one requested
                    put("generationConfig", JSONObject().apply {
                        put("responseModalities", JSONArray().put("AUDIO"))
                        put("speechConfig", JSONObject().apply {
                            put("voiceConfig", JSONObject().apply {
                                put("prebuiltVoiceConfig", JSONObject().apply {
                                    put("voiceName", "Aoede") // Female voice similar to Laomedeia
                                })
                            })
                        })
                    })
                    put("systemInstruction", JSONObject().apply {
                        put("parts", JSONArray().put(JSONObject().apply {
                            put("text", instruction)
                        }))
                    })
                })
            }
            webSocket?.send(setupJson.toString())
        } catch (e: Exception) {
            Log.e("GeminiLive", "Failed to send setup message", e)
        }
    }

    private fun handleServerMessage(text: String) {
        try {
            val json = JSONObject(text)
            if (json.has("serverContent")) {
                val serverContent = json.getJSONObject("serverContent")
                if (serverContent.has("modelTurn")) {
                    val modelTurn = serverContent.getJSONObject("modelTurn")
                    val parts = modelTurn.getJSONArray("parts")
                    for (i in 0 until parts.length()) {
                        val part = parts.getJSONObject(i)
                        if (part.has("inlineData")) {
                            val inlineData = part.getJSONObject("inlineData")
                            val base64Data = inlineData.getString("data")
                            val audioBytes = Base64.decode(base64Data, Base64.DEFAULT)
                            playAudio(audioBytes)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("GeminiLive", "Error parsing server message", e)
        }
    }

    @SuppressLint("MissingPermission")
    private fun startAudioIO() {
        if (isRecording) return
        isRecording = true

        // Setup AudioTrack for playback
        val minBufferSizeSpk = AudioTrack.getMinBufferSize(
            SPK_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        audioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            SPK_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            minBufferSizeSpk,
            AudioTrack.MODE_STREAM
        )
        audioTrack?.play()

        // Setup AudioRecord for mic
        val minBufferSizeMic = AudioRecord.getMinBufferSize(
            MIC_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            MIC_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            maxOf(minBufferSizeMic, CHUNK_SIZE * 2)
        )

        launch {
            audioRecord?.startRecording()
            val buffer = ByteArray(CHUNK_SIZE)
            while (isRecording) {
                val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                if (read > 0) {
                    // Send to websocket
                    sendAudio(buffer.copyOfRange(0, read))
                }
            }
        }
    }

    private fun sendAudio(audioBytes: ByteArray) {
        try {
            val base64Audio = Base64.encodeToString(audioBytes, Base64.NO_WRAP)
            val json = JSONObject().apply {
                put("realtimeInput", JSONObject().apply {
                    put("mediaChunks", JSONArray().put(JSONObject().apply {
                        put("mimeType", "audio/pcm;rate=16000")
                        put("data", base64Audio)
                    }))
                })
            }
            webSocket?.send(json.toString())
        } catch (e: Exception) {
            Log.e("GeminiLive", "Failed to send audio", e)
        }
    }

    private fun playAudio(audioBytes: ByteArray) {
        audioTrack?.write(audioBytes, 0, audioBytes.size)
    }

    fun stopSession() {
        isRecording = false
        webSocket?.close(1000, "Normal closure")
        webSocket = null
        stopAudioIO()
        isConnected.value = false
        job.cancelChildren()
    }

    private fun stopAudioIO() {
        try {
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
        } catch (e: Exception) { e.printStackTrace() }
        
        try {
            audioTrack?.stop()
            audioTrack?.flush()
            audioTrack?.release()
            audioTrack = null
        } catch (e: Exception) { e.printStackTrace() }
    }
}
