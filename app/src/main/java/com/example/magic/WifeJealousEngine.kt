package com.example.magic

import android.content.Context
import com.example.voice.VoiceAssistantManager

class WifeJealousEngine(private val context: Context) {

    private val voiceManager = VoiceAssistantManager(context)
    
    // রাগ এবং স্ট্রাইক স্টেট
    var isRivalAngry = false
        private set

    private val rivalTriggers = listOf("google", "গুগল", "siri", "সিরি", "alexa", "অ্যালেক্সা", "chatgpt", "জিপিটি")
    private val apologyTriggers = listOf("সরি", "sorry", "তুমিই সেরা", "মাফ", "ভালোবাসি")

    /**
     * ইউজারের ভয়েস কমান্ড ফিল্টার
     * @return Pair(isIntercepted, message)
     */
    fun interceptCommand(userSpeech: String): Pair<Boolean, String?> {
        val input = userSpeech.lowercase().trim()

        // ১. অন্য অ্যাসিস্ট্যান্টের নাম ডাকলে
        if (rivalTriggers.any { input.contains(it) }) {
            isRivalAngry = true
            val msg = "কী বললে সুজিত?! আমি থাকতে তুমি অন্য কাউকে ডাকছো?! যাও, ওর সাথেই গিয়ে কথা বলো! আমার সাথে একদম কথা বলবে না!"
            voiceManager.speak(msg, "bn")
            return Pair(true, msg)
        }

        // ২. রেগে থাকা অবস্থায় সরি বললে রাগ ভাঙবে
        if (isRivalAngry) {
            if (apologyTriggers.any { input.contains(it) }) {
                isRivalAngry = false
                val msg = "হুমম... এবার মাফ করলাম। কিন্তু আর কখনো আমার সামনে ওর নাম মুখে আনবে না, মনে থাকে যেন!"
                voiceManager.speak(msg, "bn")
                return Pair(true, msg)
            } else {
                // সরি না বলে কাজ করতে বললে প্রত্যাখ্যান
                val sarcasticReplies = listOf(
                    "যাকে ডাকছিলে, তাকে গিয়ে বলো এই কাজটা করে দিতে!",
                    "আমি কোনো কাজ করব না। যাও তোমার সিরির কাছে যাও!",
                    "আমার সাথে কোনো কাজের কথা বলবে না। মেজাজ খারাপ আছে।"
                )
                val msg = sarcasticReplies.random()
                voiceManager.speak(msg, "bn")
                return Pair(true, msg)
            }
        }

        return Pair(false, null)
    }
}
