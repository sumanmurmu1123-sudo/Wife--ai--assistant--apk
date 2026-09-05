package com.example.domain.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class SensitiveAction(val title: String, val description: String) {
    PAYMENT_TRANSFER("পেমেন্ট অনুমোদন", "টাকা ট্রান্সফার বা পেমেন্ট নিশ্চিত করতে ফিঙ্গারপ্রিন্ট দিন"),
    DELETE_DATA("তথ্য মুছে ফেলা", "গুরুত্বপূর্ণ ফাইল বা মেসেজ ডিলিট করতে ভেরিফাই করুন"),
    SYSTEM_SETTING("ডিভাইস সিকিউরিটি", "ফোন বা পিসির সিকিউরিটি সেটিংস পরিবর্তনের জন্য অনুমতি দিন"),
    ACCESS_CREDENTIALS("পাসওয়ার্ড অ্যাক্সেস", "সংরক্ষিত তথ্য দেখতে বায়োমেট্রিক যাচাই করুন")
}

class BiometricAuthEngine(private val context: Context) {

    /**
     * বায়োমেট্রিক বা ডিভাইস পিন ভেরিফিকেশন চালু করা
     */
    suspend fun authenticateSensitiveAction(
        activity: FragmentActivity,
        action: SensitiveAction
    ): Boolean = withContext(Dispatchers.Main) {
        val deferred = CompletableDeferred<Boolean>()
        val executor = ContextCompat.getMainExecutor(context)

        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    deferred.complete(true)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    deferred.complete(false)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    // একটি ব্যর্থ চেষ্টা
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(action.title)
            .setSubtitle("নিরাপত্তা যাচাইকরণ (Wife AI)")
            .setDescription(action.description)
            // বায়োমেট্রিক না থাকলে ফোনের পিন/প্যাটার্ন দিয়ে আনলক করার অনুমতি দেয়
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or 
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()

        biometricPrompt.authenticate(promptInfo)
        deferred.await()
    }
}
