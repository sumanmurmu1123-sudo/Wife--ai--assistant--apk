package com.example.persona

import android.content.Context
import com.example.voice.VoiceAssistantManager

class WifeMistakeEngine(private val context: Context) {

    private val voiceManager = VoiceAssistantManager(context)
    private var mistakeCounter = 0

    // ভুল সংক্রান্ত কি-ওয়ার্ড
    private val mistakeTriggers = listOf(
        "ভুল করে ফেলেছি", "ভুল হয়ে গেছে", "ভুল করলাম", "গণ্ডগোল হয়ে গেছে", "মিস্টেক", "mistake"
    )

    // ক্ষমা চাওয়ার কি-ওয়ার্ড
    private val apologyTriggers = listOf(
        "সরি", "sorry", "মাফ করে দাও", "আর হবে না", "ক্ষমা করো"
    )

    /**
     * সুজিতের ভুলের স্বীকারোক্তি প্রসেস করা
     */
    fun handleMistake(userInput: String): String {
        val input = userInput.lowercase().trim()

        // ১. সরি বললে রাগ ভাঙা ও আদর
        if (apologyTriggers.any { input.contains(it) }) {
            mistakeCounter = 0
            val reply = "আচ্ছা ঠিক আছে, এবার ক্ষমা করলাম। সরি বলতে হবে না, একটা মিষ্টি করে হাসো তো! আমি তো সবসময় তোমার পাশেই আছি।"
            voiceManager.speak(reply, "bn")
            return reply
        }

        // ২. ভুল স্বীকার করলে
        if (mistakeTriggers.any { input.contains(it) }) {
            mistakeCounter++

            val reply = if (mistakeCounter > 1) {
                // একই ভুল বারবার করলে মিষ্টি শাসন
                "উফফ সুজিত! এই একই ভুল তুমি বারবার করো কেন বলো তো? আমার কথা একটু শুনলে কি হতো? কান ধরো আগে, নয়তো কিন্তু আর কথা বলব না!"
            } else {
                // প্রথমবার হলে ভরসা ও সান্ত্বনা
                "ভুল হয়ে গেছে তো কী হয়েছে সুজিত? মানুষ মাত্রই তো ভুল হয়। মন খারাপ করো না একদম! চলো, দুজন মিলে ঠান্ডা মাথায় এটা ঠিক করে ফেলি।"
            }

            voiceManager.speak(reply, "bn")
            return reply
        }

        return "কী ভুল করেছ আমাকে স্পষ্ট করে বলো সুজিত।"
    }
}
