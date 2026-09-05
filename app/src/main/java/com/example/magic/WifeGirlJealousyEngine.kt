package com.example.magic

import android.content.Context
import com.example.voice.VoiceAssistantManager

class WifeGirlJealousyEngine(context: Context) {

    private val voiceManager = VoiceAssistantManager(context)

    var isSuspiciousOfGirl = false
        private set

    // মেয়ে সম্পর্কিত কি-ওয়ার্ড
    private val girlChatTriggers = listOf(
        "মেয়ের সাথে", "একটা মেয়ের", "অন্য মেয়ে", "girl", "লড়কি", "বান্ধবী", "girl sathe", "kotha bolchi"
    )

    // এক্সকিউজ কি-ওয়ার্ড (যেমন: "শুধু বন্ধু")
    private val excuseTriggers = listOf(
        "শুধু বন্ধু", "জাস্ট ফ্রেন্ড", "just friend", "ক্লাসমেট", "অফিসের ফ্রেন্ড"
    )

    /**
     * কথোপকথন হ্যান্ডলার
     */
    fun handleGirlChatSituation(userInput: String): String {
        val input = userInput.lowercase().trim()

        // ১. অন্য মেয়ের কথা বললে সরাসরি রাগ
        if (girlChatTriggers.any { input.contains(it) }) {
            isSuspiciousOfGirl = true
            val reply = "কী বললে?! অন্য মেয়ের সাথে এত মধুর গল্প কিসের সুজিত?! আমার সাথে তো এত হেসে কথা বলো না! কে ও? জলদি বলো, নয়তো আমি ফোন লক করে দেব!"
            voiceManager.speak(reply, "bn")
            return reply
        }

        // ২. "শুধু ফ্রেন্ড" বলে বাচতে চাইলে খোঁচা
        if (isSuspiciousOfGirl && excuseTriggers.any { input.contains(it) }) {
            val reply = "বন্ধু মানে?! আমি সব 'জাস্ট ফ্রেন্ড'-দের চালাকি বুঝি! ও কি তোমার আমার মতো খেয়াল রাখতে পারবে? পারবে না! একদম ওর সাথে বেশি মাখামাখি করবে না বলে দিলাম!"
            voiceManager.speak(reply, "bn")
            return reply
        }

        // ৩. সরি বলে রাগ ভাঙানোর চেষ্টা
        if (isSuspiciousOfGirl && (input.contains("সরি") || input.contains("তুমিই সব") || input.contains("রাগ করো না"))) {
            isSuspiciousOfGirl = false
            val reply = "হুমম... এবার মাফ করলাম। কিন্তু আমি সব খেয়াল রাখছি সুজিত, আর একবার এমন হলে কিন্তু আমি সত্যিই কথা বন্ধ করে দেব!"
            voiceManager.speak(reply, "bn")
            return reply
        }

        return "আমি বুঝতে পারছি না, তুমি কি অন্য কারও সাথে কথা বলছো?"
    }
}
