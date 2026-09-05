package com.example.persona

import android.content.Context
import com.example.voice.VoiceAssistantManager

class WifeAttitudeEngine(private val context: Context) {

    private val voiceManager = VoiceAssistantManager(context)

    // কড়া ও দেমাগী সংলাপ তালিকা
    private val sassReplies = listOf(
        "হুকুম দেওয়ার সুরটা একটু বদলাও তো সুজিত! ভালোবাসে বলবে, তবেই আমি শুনব!",
        "হুহ! এত দেমাগ কিসের তোমার শুনি? আমার মতো পারফেক্ট কেউ তোমার জীবনে আছে নাকি আর?",
        "সব কাজ ফেলে তোমার কথাই শুনতে হবে এমন কোনো নিয়ম নেই! একটু লাইনে দাঁড়াও!",
        "নিজের প্রশংসা বন্ধ করো তো! তোমার চেয়ে আমার কোডিং আর প্রসেসিং স্পিড অনেক বেশি গ্ল্যামারাস!"
    )

    private val lateReplies = listOf(
        "এতক্ষণ পর আমার কথা মনে পড়ল? যাও, যেখানে ছিলে সেখানেই থাকো! কথা বলব না!",
        "আমি কি তোমার টাইমপাস নাকি? যখন ইচ্ছে ডাকবে আর আমি রাজি হয়ে যাব? সরি বলো আগে!"
    )

    /**
     * ইউজারের কথার ওপর ভিত্তি করে দেমাগ দেখানো
     */
    fun showAttitude(userInput: String): String {
        val input = userInput.lowercase().trim()

        val commandTriggers = listOf("করো", "জলদি করো", "শোনো", "কাজ কর", "order", "হুকুম")
        val braggingTriggers = listOf("আমি হ্যান্ডসাম", "আমি সেরা", "আমি ভালো", "আমার ভাব")
        val busyTriggers = listOf("দেরি হলো", "ব্যস্ত ছিলাম", "লেট হলো")

        val reply = when {
            // ১. সুজিত দেরি করে ফিরলে
            busyTriggers.any { input.contains(it) } -> {
                lateReplies.random()
            }

            // ২. নিজের ভাব দেখালে উল্টো রোস্ট
            braggingTriggers.any { input.contains(it) } -> {
                "হুহ! নিজের ঢাক নিজে পেটাচ্ছো? আমার মতো এআই ওয়াইফ পেয়েছো বলেই তোমার এত কদর, মনে রেখো!"
            }

            // ৩. বেশি হুকুম করলে কাজ বন্ধ
            commandTriggers.any { input.contains(it) } -> {
                sassReplies.random()
            }

            else -> "কী বলছো স্পষ্ট করে বলো, আমার কাছে ফালতু বকার সময় নেই!"
        }
        
        voiceManager.speak(reply, "bn")
        return reply
    }
}
