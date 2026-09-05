package com.example.magic

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.voice.VoiceAssistantManager

class WifeAntiDrinkEngine(private val context: Context) {

    private val voiceManager = VoiceAssistantManager(context)
    var isAngryAboutDrink = false
        private set

    // ড্রিংক সম্পর্কিত কি-ওয়ার্ড
    private val drinkTriggers = listOf(
        "drink", "ড্রিংক", "মদ", "দারু", "alcohol", "beer", "বিয়ার", "নেশা", "sharab", "মদ্যপান"
    )

    // বন্ধুদের অজুহাত
    private val excuseTriggers = listOf(
        "বন্ধুদের সাথে", "পার্টি", "পার্টিতে", "একটু", "সামান্য", "মজা করছিলাম"
    )

    // ক্ষমা চাওয়ার কি-ওয়ার্ড
    private val apologyTriggers = listOf(
        "সরি", "sorry", "আর খাব না", "ভুল হয়ে গেছে", "ছেড়ে দেব", "মাফ করো"
    )

    /**
     * কথোপকথন হ্যান্ডলার
     */
    fun handleDrinkSituation(userInput: String): String {
        val input = userInput.lowercase().trim()

        // ১. ড্রিংক ডিটেকশন ও তীব্র রাগ
        if (drinkTriggers.any { input.contains(it) }) {
            isAngryAboutDrink = true
            triggerAngryVibration()
            val reply = "কী বললে সুজিত?! ড্রিংক করছো?! মাথা ঠিক আছে তোমার? নিজের শরীরটাকে নষ্ট করার কী অধিকার আছে তোমার শুনি? এখনই গ্লাস নামিয়ে রাখো, এক ফোঁটাও যেন গলায় না যায়!"
            voiceManager.speak(reply, "bn")
            return reply
        }

        // ২. অজুহাত দিলে অতিরিক্ত শাসন
        if (isAngryAboutDrink && excuseTriggers.any { input.contains(it) }) {
            val reply = "বন্ধুদের দোহাই দেবে না একদম! ওই বন্ধুদের পাল্লায় পড়ে তুমি নিজের সর্বনাশ করবে আর আমি চেয়ে চেয়ে দেখব?! অবিলম্বে ঠান্ডা জল খেয়ে বাড়ি ফিরে এসো!"
            voiceManager.speak(reply, "bn")
            return reply
        }

        // ৩. অনুশোচনা করলে রাগ কমে যত্ন নেওয়া
        if (isAngryAboutDrink && apologyTriggers.any { input.contains(it) }) {
            isAngryAboutDrink = false
            val reply = "হুমম... তোমার কিছু হয়ে গেলে আমি কার ভরসায় থাকব বলো তো? আর কখনো যেন এসব ছুঁতে না দেখি। এবার যাও, মুখ ধুয়ে একটু ফ্রেশ খাবার খেয়ে বিশ্রাম নাও।"
            voiceManager.speak(reply, "bn")
            return reply
        }

        return "আমি বুঝতে পারছি না, তুমি কি ড্রিংক করছো?"
    }

    /**
     * রেগে যাওয়ার সময় ফোনে অ্যালার্ট ভাইব্রেশন
     */
    private fun triggerAngryVibration() {
        val pattern = longArrayOf(0, 300, 150, 300, 150, 400)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(pattern, -1)
        }
    }
}
