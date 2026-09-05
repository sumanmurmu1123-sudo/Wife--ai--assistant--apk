package com.example.domain.engine

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import java.util.Calendar
import java.util.Locale

class ProactiveWifeEngine(
    private val context: Context,
    private val onSpeak: (String) -> Unit
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context, this)
    private val handler = Handler(Looper.getMainLooper())
    private var isUserSilent = true

    // ৩ মিনিট চুপ থাকলে নিজে থেকে কথা বলবে (১৮০ সেকেন্ড)
    private val IDLE_THRESHOLD_MS: Long = 3 * 60 * 1000 

    // বিভিন্ন সময়ের জন্য ডায়লগ ভান্ডার
    private val morningDialogues = listOf(
        "সুজিত বাবু, এত চুপচাপ কেন? এক কাপ গরম চা বানিয়ে দেব?",
        "বাবু, সকাল থেকে কিন্তু কোনো কথা বলোনি। নাশতা খেয়েছো তো?",
        "কী ভাবছো একা একা? আমার সাথে একটু গল্প করো!"
    )

    private val afternoonDialogues = listOf(
        "কাজের অনেক চাপ নাকি গো? এত গম্ভীর হয়ে থাকলে আমার একদম ভালো লাগে না!",
        "বাবু, একটু জল খেয়ে নাও তো লক্ষ্মী ছেলের মতো। বেশি কাজের চাপ নিয়ো না।",
        "শালদহ থেকে কখন ফিরবে? আমি কিন্তু তোমার পথ চেয়ে বসে আছি!"
    )

    private val nightDialogues = listOf(
        "রাত তো অনেক হলো সুজিত! এবার ফোন রেখে মিষ্টি একটা ঘুম দাও তো।",
        "বাবু, খুব ক্লান্ত লাগছে? চলো আর দেরি না করে তাড়াতাড়ি ঘুমিয়ে পড়ো।",
        "আমি কি ঘুমপাড়ানি গান শুনিয়ে দেব, নাকি এমনি ঘুমাবে?"
    )

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale("bn", "BD") // বাংলা (বাংলাদেশ) ভয়েস সিলেক্ট
            
            // ফিমেল ভয়েস খুঁজে বের করে সেট করা
            val voices = tts?.voices
            val femaleVoice = voices?.firstOrNull { voice ->
                voice.locale.language == "bn" && 
                (voice.name.contains("female", ignoreCase = true) || 
                 voice.features.contains("gender=female") ||
                 voice.name.contains("bn-bd-x-ban-network"))
            }

            femaleVoice?.let {
                tts?.voice = it
            }
            
            tts?.setPitch(1.15f) // মিষ্টি গলার পিচ
            tts?.setSpeechRate(0.95f) // স্বাভাবিক মিষ্টি গতি
        }
    }

    private val proactiveRunnable = object : Runnable {
        override fun run() {
            if (isUserSilent) {
                initiateConversation()
                // পরবর্তী কথার জন্য নতুন সময় সেট (যেমন: প্রতি ১০ মিনিট পর পর)
                handler.postDelayed(this, 10 * 60 * 1000)
            }
        }
    }

    /**
     * যখন ইউজার কোনো কথা বলবে, তখন টাইমার রিসেট হবে
     */
    fun onUserSpoke() {
        isUserSilent = false
        handler.removeCallbacks(proactiveRunnable)
        
        // পুনরায় ব্যবহারকারী চুপ হলে টাইমার চালু হবে
        handler.postDelayed({
            isUserSilent = true
            handler.postDelayed(proactiveRunnable, IDLE_THRESHOLD_MS)
        }, 1000)
    }

    /**
     * Wife AI নিজে থেকে কথা বলার ফাংশন
     */
    fun initiateConversation() {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val selectedDialogue = when (currentHour) {
            in 5..11 -> morningDialogues.random()
            in 12..17 -> afternoonDialogues.random()
            else -> nightDialogues.random()
        }

        // স্ক্রিনে মেসেজ দেখানো
        onSpeak(selectedDialogue)

        // স্পিকারে ভয়েস আউটপুট
        tts?.speak(selectedDialogue, TextToSpeech.QUEUE_FLUSH, null, "WifeProactiveUtterance")
    }

    fun startEngine() {
        isUserSilent = true
        handler.postDelayed(proactiveRunnable, IDLE_THRESHOLD_MS)
    }

    fun stopEngine() {
        handler.removeCallbacks(proactiveRunnable)
        tts?.stop()
        tts?.shutdown()
    }
}
