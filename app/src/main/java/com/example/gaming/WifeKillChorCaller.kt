package com.example.gaming

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.voice.VoiceAssistantManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class WifeKillChorCaller(private val context: Context) {

    private val voiceManager = VoiceAssistantManager(context)

    /**
     * কিলচোর বন্ধুকে ফোন করার ফাংশন
     * @param friendPhoneNumber বন্ধুর ফোন নম্বর
     */
    fun callKillChorFriend(friendPhoneNumber: String) {
        CoroutineScope(Dispatchers.Main).launch {
            // ১. সুজিতকে সান্ত্বনা ও বন্ধুর ওপর রাগ প্রকাশ
            voiceManager.speak(
                "দাঁড়াও সুজিত! ওই লোভী কিলচোর বন্ধুকে এখনই কল লাগাচ্ছি! আজ ওর একদিন কি আমার একদিন! পুরো ম্যাচ ও তোমার ওপর দিয়ে খেলবে, আর কিল নেওয়ার সময় ওস্তাদ?!",
                "bn"
            )

            delay(7000) // ডায়ালগ শেষ হওয়ার জন্য সামান্য বিরতি

            // ২. ডিরেক্ট কল ডায়াল ইন্টেন্ট
            try {
                val callIntent = Intent(Intent.ACTION_CALL).apply {
                    data = Uri.parse("tel:$friendPhoneNumber")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(callIntent)
            } catch (e: SecurityException) {
                // CALL_PHONE পারমিশন না থাকলে ডায়ালপ্যাড ওপেন করবে
                val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$friendPhoneNumber")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(dialIntent)
            }
        }
    }
}
