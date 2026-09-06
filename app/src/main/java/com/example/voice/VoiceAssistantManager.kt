package com.example.voice

import android.content.Context
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Locale
import com.example.BuildConfig

class VoiceAssistantManager(private val context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = TextToSpeech(context, this)
    private var isInitialized = false
    private val client = OkHttpClient()
    private var mediaPlayer: MediaPlayer? = null

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            // ভাষা সেট করা (বাংলা ভাষার জন্য 'bn', 'IN')
            val result = tts?.setLanguage(Locale("bn", "IN"))

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "কাঙ্ক্ষিত ভাষাটি এই ডিভাইসে সমর্থিত নয় বা ডেটা অনুপস্থিত।")
            }

            // ফিমেল ভয়েস খুঁজে বের করে সেট করা
            val voices = tts?.voices
            val femaleVoice = voices?.firstOrNull { voice ->
                voice.locale.language == "bn" && 
                 (voice.name.contains("female", ignoreCase = true) || 
                  voice.features.contains("gender=female") ||
                 voice.name.contains("bn-in-x-ban-network"))
            }
            femaleVoice?.let {
                tts?.voice = it
            }
        } else {
            Log.e("TTS", "TextToSpeech initialization failed.")
        }
    }

    /**
     * যেকোনো নির্দিষ্ট ভাষায় কথা বলানোর ফাংশন
     */
    fun speak(text: String, languageCode: String = "bn") {
        val elevenLabsKey = BuildConfig.ELEVENLABS_API_KEY
        if (elevenLabsKey.isNotBlank() && elevenLabsKey != "MY_ELEVENLABS_API_KEY") {
            playAnjaliVoice(text, languageCode)
        } else {
            speakWithAndroidTTS(text, languageCode)
        }
    }

    private fun playAnjaliVoice(textToSpeak: String, languageCode: String) {
        val voiceId = "gHu9GtaHOXcSqFTK06ux" // Anjali Voice ID
        val apiKey = BuildConfig.ELEVENLABS_API_KEY
        val url = "https://api.elevenlabs.io/v1/text-to-speech/$voiceId"

        val json = """
        {
            "text": "$textToSpeak",
            "model_id": "eleven_multilingual_v2",
            "voice_settings": {
                "stability": 0.45,
                "similarity_boost": 0.85,
                "style": 0.20,
                "use_speaker_boost": true
            }
        }
        """.trimIndent()

        val request = Request.Builder()
            .url(url)
            .addHeader("xi-api-key", apiKey)
            .addHeader("Content-Type", "application/json")
            .post(json.toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("VoiceAssistant", "ElevenLabs API failed", e)
                speakWithAndroidTTS(textToSpeak, languageCode)
            }

            override fun onResponse(call: Call, response: Response) {
                val audioBytes = response.body?.bytes()
                if (response.isSuccessful && audioBytes != null) {
                    playAudioBytes(audioBytes)
                } else {
                    Log.e("VoiceAssistant", "ElevenLabs error: ${response.code} ${response.body?.string()}")
                    speakWithAndroidTTS(textToSpeak, languageCode)
                }
            }
        })
    }

    private fun playAudioBytes(audioBytes: ByteArray) {
        try {
            val tempAudioFile = File.createTempFile("elevenlabs_audio", ".mp3", context.cacheDir)
            FileOutputStream(tempAudioFile).use { fos ->
                fos.write(audioBytes)
            }

            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(tempAudioFile.absolutePath)
                prepare()
                start()
                setOnCompletionListener {
                    tempAudioFile.delete()
                    it.release()
                }
            }
        } catch (e: Exception) {
            Log.e("VoiceAssistant", "Error playing audio bytes", e)
        }
    }

    private fun speakWithAndroidTTS(text: String, languageCode: String) {
        if (!isInitialized || tts == null) return

        val locale = when (languageCode.lowercase()) {
            "bn", "bengali" -> Locale("bn", "IN")
            "en", "english" -> Locale.US
            "hi", "hindi" -> Locale("hi", "IN")
            "es", "spanish" -> Locale("es", "ES")
            "ar", "arabic" -> Locale("ar", "SA")
            else -> Locale.getDefault()
        }

        // ভাষা পরিবর্তন ও স্পিচ রেন্ডার
        val result = tts?.setLanguage(locale)
        if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
            tts?.setPitch(1.15f) // একটু সফট ও মিষ্টি ভয়েসের জন্য পিচ অ্যাডজাস্ট
            tts?.setSpeechRate(0.95f) // স্বাভাবিক কথা বলার গতি
            
            // টেক্সট স্পিক করার জন্য QUEUE_FLUSH ব্যবহার করা হয় যাতে আগের কথা কেটে নতুন কথা শুরু হয়
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "TTS_ID")
        } else {
            Log.e("TTS", "Speak: কাঙ্ক্ষিত ভাষাটি এই ডিভাইসে সমর্থিত নয় বা ডেটা অনুপস্থিত।")
        }
    }

    fun stop() {
        tts?.stop()
        mediaPlayer?.stop()
    }

    fun shutdown() {
        // অ্যাপ বন্ধ হওয়ার সময় রিসোর্স রিলিজ করা আবশ্যক
        tts?.stop()
        tts?.shutdown()
        tts = null
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
