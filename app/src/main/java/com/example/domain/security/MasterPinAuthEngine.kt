package com.example.domain.security

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class PinAuthType {
    MASTER_KEY,
    QUICK_OPEN,
    MASTER_OVERRIDE
}

data class PinAuthResult(
    val isSuccess: Boolean,
    val authType: PinAuthType?,
    val message: String,
    val details: String?
)

class MasterPinAuthEngine(
    private val context: Context,
    private val pcEngine: PcRemoteEngine,
    private val locationEngine: FamilyLocationEngine,
    private val lostPhoneEngine: LostPhoneDefenseEngine
) {

    /**
     * পিন ও হিরো স্পেশাল কোড ভ্যালিডেশন
     */
    suspend fun verifyAndExecutePin(input: String): PinAuthResult = withContext(Dispatchers.IO) {
        val cleanInput = input.trim()
        val prefs = context.getSharedPreferences("maya_v2_prefs", Context.MODE_PRIVATE)
        val masterSecret = prefs.getString("master_secret", "sujit@123") ?: "sujit@123"
        val heroCode = prefs.getString("hero_code", "sujit@hero") ?: "sujit@hero"
        val quickPin = prefs.getString("quick_pin", "9242") ?: "9242"
        val emergencyPin = prefs.getString("emergency_pin", "924208") ?: "924208"

        when {
            // ১. নতুন HERO স্পেশাল কোড (sujit@hero / SUJIT HERO)
            cleanInput.equals(heroCode, ignoreCase = true) || 
            cleanInput.equals("sujit hero", ignoreCase = true) ||
            cleanInput.equals("SUJIT HERO STOP", ignoreCase = true) -> {
                lostPhoneEngine.stopSiren(heroCode)
                PinAuthResult(
                    isSuccess = true,
                    authType = PinAuthType.MASTER_KEY,
                    message = "🦸‍♂️ Welcome Hero! ফুল অ্যাডমিন এক্সেস গ্রান্টেড ও সাইরেন নিষ্ক্রিয়!",
                    details = "Hero Master Override Executed"
                )
            }

            // ২. রেগুলার মাস্টার সিক্রেট কি (sujit@123)
            cleanInput.equals(masterSecret, ignoreCase = true) || cleanInput.equals("$masterSecret STOP", ignoreCase = true) -> {
                lostPhoneEngine.stopSiren(masterSecret)
                PinAuthResult(
                    isSuccess = true,
                    authType = PinAuthType.MASTER_KEY,
                    message = "✅ মাস্টার কোড ভেরিফায়েড!",
                    details = "Master Authorization Approved"
                )
            }

            // ৩. কুইক ৪-ডিজিট পিন (9242)
            cleanInput == quickPin -> {
                PinAuthResult(
                    isSuccess = true,
                    authType = PinAuthType.QUICK_OPEN,
                    message = "🔓 কুইক পিন ভেরিফায়েড!",
                    details = "Quick Access Mode"
                )
            }

            // ৪. ৬-ডিজিট মাস্টার ওভাররাইড (924208)
            cleanInput == emergencyPin -> {
                pcEngine.lockPc()
                val locStatus = locationEngine.locateFamilyMembers()
                PinAuthResult(
                    isSuccess = true,
                    authType = PinAuthType.MASTER_OVERRIDE,
                    message = "🚨 মাস্টার পিন এক্সিকিউটেড! পিসি লকড!",
                    details = locStatus
                )
            }

            else -> {
                PinAuthResult(
                    isSuccess = false,
                    authType = null,
                    message = "❌ ভুল কোড! এক্সেস ডিনাইড!",
                    details = null
                )
            }
        }
    }
}
