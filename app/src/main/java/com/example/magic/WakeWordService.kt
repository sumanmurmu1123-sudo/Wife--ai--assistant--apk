package com.example.magic

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log

class WakeWordService : Service() {

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()
        setupSpeechRecognizer()
        startListening()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun setupSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            
            override fun onEndOfSpeech() {
                isListening = false
                restartListening()
            }
            
            override fun onError(error: Int) {
                isListening = false
                // Ignore errors and restart to keep listening continuously
                restartListening()
            }
            
            override fun onResults(results: Bundle?) {
                isListening = false
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull()?.lowercase() ?: ""
                if (text.isNotEmpty()) {
                    val intent = Intent(this@WakeWordService, FloatingBallService::class.java).apply {
                        action = "ACTION_SET_THINKING"
                    }
                    startService(intent)
                }

                if (text.contains("wife") || text.contains("ওয়াইফ")) {
                    triggerAssistant()
                } else {
                    restartListening()
                }
            }
            
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private fun startListening() {
        if (!isListening && speechRecognizer != null) {
            val speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD")
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }
            try {
                speechRecognizer?.startListening(speechIntent)
                isListening = true
            } catch (e: Exception) {
                Log.e("WakeWord", "Failed to start listening: ${e.message}")
                restartListening()
            }
        }
    }

    private fun restartListening() {
        // Add a small delay before restarting to prevent rapid loops on error
        handler.postDelayed({
            startListening()
        }, 500)
    }

    private fun triggerAssistant() {
        // Notify the floating ball or main assistant to activate
        val intent = Intent(this, FloatingBallService::class.java).apply {
            action = "ACTION_WAKE_WORD_DETECTED"
        }
        startService(intent)
        
        // Temporarily pause listening while assistant handles the command
        handler.postDelayed({
            startListening()
        }, 5000)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer?.destroy()
        speechRecognizer = null
        handler.removeCallbacksAndMessages(null)
    }
}
