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
    companion object {
        const val QUICK_OPEN_PIN = "9242"
        const val MASTER_SECRET_KEY = "sujit@123"
        const val HERO_SUPER_CODE = "sujit@hero" // 🦸‍♂️ নতুন হিরো স্পেশাল কোড
        const val MASTER_EMERGENCY_PIN = "924208"
    }

    /**
     * পিন ও হিরো স্পেশাল কোড ভ্যালিডেশন
     */
    suspend fun verifyAndExecutePin(input: String): PinAuthResult = withContext(Dispatchers.IO) {
        val cleanInput = input.trim()

        when {
            // ১. নতুন HERO স্পেশাল কোড (sujit@hero / SUJIT HERO)
            cleanInput.equals(HERO_SUPER_CODE, ignoreCase = true) || 
            cleanInput.equals("sujit hero", ignoreCase = true) ||
            cleanInput.equals("SUJIT HERO STOP", ignoreCase = true) -> {
                lostPhoneEngine.stopSiren(HERO_SUPER_CODE)
                PinAuthResult(
                    isSuccess = true,
                    authType = PinAuthType.MASTER_KEY,
                    message = "🦸‍♂️ Welcome Hero! ফুল অ্যাডমিন এক্সেস গ্রান্টেড ও সাইরেন নিষ্ক্রিয়!",
                    details = "Hero Master Override Executed"
                )
            }

            // ২. রেগুলার মাস্টার সিক্রেট কি (sujit@123)
            cleanInput.equals(MASTER_SECRET_KEY, ignoreCase = true) || cleanInput.equals("sujit@123 STOP", ignoreCase = true) -> {
                lostPhoneEngine.stopSiren(MASTER_SECRET_KEY)
                PinAuthResult(
                    isSuccess = true,
                    authType = PinAuthType.MASTER_KEY,
                    message = "✅ মাস্টার কোড ভেরিফায়েড!",
                    details = "Master Authorization Approved"
                )
            }

            // ৩. কুইক ৪-ডিজিট পিন (9242)
            cleanInput == QUICK_OPEN_PIN -> {
                PinAuthResult(
                    isSuccess = true,
                    authType = PinAuthType.QUICK_OPEN,
                    message = "🔓 কুইক পিন 9242 ভেরিফায়েড!",
                    details = "Quick Access Mode"
                )
            }

            // ৪. ৬-ডিজিট মাস্টার ওভাররাইড (924208)
            cleanInput == MASTER_EMERGENCY_PIN -> {
                pcEngine.lockPc()
                val locStatus = locationEngine.locateFamilyMembers()
                PinAuthResult(
                    isSuccess = true,
                    authType = PinAuthType.MASTER_OVERRIDE,
                    message = "🚨 মাস্টার পিন 924208 এক্সিকিউটেড! পিসি লকড!",
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
