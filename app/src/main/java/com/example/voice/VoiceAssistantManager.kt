package com.example.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class VoiceAssistantManager(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context, this)
    private var isInitialized = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            // ডিফল্ট ভাষা হিসেবে বাংলা সেট করা
            tts?.language = Locale("bn", "BD")
            
            // ফিমেল ভয়েস খুঁজে বের করে সেট করা
            val voices = tts?.voices
            val femaleVoice = voices?.firstOrNull { voice ->
                // বাংলা এবং নামের মধ্যে 'female' বা গুগলের নির্দিষ্ট ফিমেল কোড থাকা
                voice.locale.language == "bn" && 
                (voice.name.contains("female", ignoreCase = true) || 
                 voice.features.contains("gender=female") ||
                 voice.name.contains("bn-bd-x-ban-network"))
            }

            femaleVoice?.let {
                tts?.voice = it
            }
        }
    }

    /**
     * যেকোনো নির্দিষ্ট ভাষায় কথা বলানোর ফাংশন
     * @param text যে টেক্সটটি বলবে
     * @param languageCode যেমন "bn" (বাংলা), "en" (ইংরেজি), "hi" (হিন্দি), "es" (স্প্যানিশ)
     */
    fun speak(text: String, languageCode: String = "bn") {
        if (!isInitialized || tts == null) return

        val locale = when (languageCode.lowercase()) {
            "bn", "bengali" -> Locale("bn", "BD")
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
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "TTS_ID")
        }
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
