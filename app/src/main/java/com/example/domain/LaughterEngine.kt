package com.example.domain

import android.content.Context
import android.media.MediaPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LaughterEngine(private val context: Context) {

    private val funnyBanglaJokes = listOf(
        "Boss, শিক্ষক ছাত্রকে জিজ্ঞেস করল: 'বল তো নিউটনের তৃতীয় সূত্র কী?' ছাত্র বলল: 'স্যার, পড়ার সময় ঘুমালে, পরীক্ষার সময় রেজাল্ট অটোমেটিক কাঁদায়!' হাহাহা! 😂",
        "ডাক্তার: আপনার কী সমস্যা? রোগী: ডাক্তারবাবু, আমি খুব ভুলোমনা! ডাক্তার: কবে থেকে এই সমস্যা? রোগী: কোন সমস্যা? হিহিহি! 🤭",
        "Boss, ইন্টারভিউয়ার জিজ্ঞেস করল: 'আপনার দুর্বলতা কী?' ছেলেটি বলল: 'আমি খুব সৎ।' ইন্টারভিউয়ার: 'এটা তো কোনো দুর্বলতা না!' ছেলে: 'আপনার কী মনে হয় তাতে আমার কোনো যায় আসে না!' হাহাহা! 🤣",
        "স্বামী স্ত্রীকে বলল: 'তুমি সবসময় নিজের ভুল অন্যের ওপর চাপাও কেন?' স্ত্রী বলল: 'কারণ আমার ভুলগুলো একা বহন করার মতো দুর্বল আমি নই!' হিহিহি! 💕"
    )

    private val cuteGiggles = listOf(
        "হিহিহি! Boss, তুমি সত্যি খুব ফানি! 🤭",
        "হাহাহা! তোমার সাথে কথা বললে আমার মন একদম ভালো হয়ে যায়, Boss! 🥰",
        "কী যে বলো না! দুষ্টুমি বন্ধ করো, হিহি! 😉",
        "হাহাহা, ঠিক আছে Boss! তোমার কথাই মেনে নিলাম!"
    )

    fun tellJoke(): String {
        return funnyBanglaJokes.random()
    }

    fun getCuteGiggle(): String {
        return cuteGiggles.random()
    }

    suspend fun playLaughSound() = withContext(Dispatchers.IO) {
        try {
            val resId = context.resources.getIdentifier("cute_laugh", "raw", context.packageName)
            if (resId != 0) {
                val mediaPlayer = MediaPlayer.create(context, resId)
                mediaPlayer.setOnCompletionListener { it.release() }
                mediaPlayer.start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
