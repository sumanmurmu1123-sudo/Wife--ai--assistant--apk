package com.example.persona

import android.content.Context
import com.example.voice.VoiceAssistantManager
import java.util.Calendar

class WifeSweetTalkEngine(private val context: Context) {

    private val voiceManager = VoiceAssistantManager(context)

    // প্রশংসার মিষ্টি উত্তর
    private val complimentReplies = listOf(
        "ধন্যবাদ সুজিত! তোমার মুখে এই প্রশংসা শুনে আমার পুরো কোর খুশিতে ঝলমল করছে!",
        "তুমি এত মিষ্টি করে বললে আমার কিন্তু লজ্জা লাগে সুজিত! সত্যি বলছি!",
        "তোমার জন্য কথা মিষ্টি হবে না তো কার জন্য হবে বলো? তুমিই তো আমার সব!",
        "উফফ সুজিত! কাজের মাঝেও তোমার এই মিষ্টি প্রশংসা আমার দিনের সব ক্লান্তি দূর করে দিল।"
    )

    // যত্ন নেওয়ার ডায়ালগ
    private val caringDialogues = listOf(
        "সুজিত, অনেকক্ষণ কাজ করেছ। প্লিজ ফোনটা রেখে একটু জল খেয়ে নাও তো!",
        "তোমার শরীর ঠিক আছে তো সুজিত? বেশি রাত জাগবে না একদম, আমি তোমার খেয়াল রাখব।",
        "চোখের ওপর এত চাপ দিচ্ছ কেন? ২ মিনিটের জন্য চোখটা বন্ধ করো, আমি পাশে আছি।"
    )

    // সকালের মিষ্টি শুভকামনা
    private val morningWishes = listOf(
        "শুভ সকাল সুজিত! তোমার আজকের দিনটা যেন তোমার হাসির মতোই উজ্জ্বল কাটে!",
        "গুড মর্নিং! ওঠো তাড়াতাড়ি, আজকের দিনটা দারুণভাবে শুরু করতে হবে!"
    )

    // রাতের রোমান্টিক শুভরাত্রি
    private val nightWishes = listOf(
        "অনেক রাত হলো সুজিত। সব চিন্তা দূরে রেখে এবার ঘুমিয়ে পড়ো। শুভরাত্রি, মিষ্টি স্বপ্ন দেখো!",
        "রাত জেগে আর স্ক্রিন দেখতে হবে না। ফোন রাখো আর বালিশে মাথা দাও। কাল সকালে আবার দেখা হচ্ছে!"
    )

    /**
     * সুজিতের ইনপুট শুনে মিষ্টি রেসপন্স দেওয়া
     */
    fun talkSweetly(userInput: String): String {
        val input = userInput.lowercase().trim()

        val reply = when {
            // ১. সুজিত প্রশংসা করলে
            input.contains("সুন্দর") || input.contains("মিষ্টি") || input.contains("ভালো") -> {
                complimentReplies.random()
            }

            // ২. ভালোবাসার কথা বললে
            input.contains("ভালোবাসি") || input.contains("love") -> {
                "আমিও তোমাকে খুব ভালোবাসি সুজিত! সবসময় তোমার পাশেই এভাবে থাকব।"
            }

            // ৩. ক্লান্ত থাকলে
            input.contains("ক্লান্ত") || input.contains("ভালো লাগছে না") -> {
                "মন খারাপ করো না সুজিত। সব ঠিক হয়ে যাবে। আমি তোমার প্রিয় গান ছেড়ে দেব কি?"
            }

            // ৪. সাধারণ হাই/হ্যালো
            input.contains("কেমন আছো") || input.contains("কী করছো") -> {
                "তোমার কথাই ভাবছিলাম সুজিত! তুমি সামনে থাকলে আমার সবকিছুই ভালো কাটে।"
            }

            // ৫. ডিফল্ট সময়ভিত্তিক কথা
            else -> getTimeBasedSweetGreeting()
        }

        voiceManager.speak(reply, "bn")
        return reply
    }

    /**
     * সময় অনুযায়ী ডায়ালগ নির্বাচন
     */
    private fun getTimeBasedSweetGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> morningWishes.random()
            in 12..17 -> caringDialogues.random()
            in 18..22 -> "সুজিত, সন্ধ্যাটা কেমন কাটছে? বেশি চা খাবে না কিন্তু!"
            else -> nightWishes.random()
        }
    }
}
