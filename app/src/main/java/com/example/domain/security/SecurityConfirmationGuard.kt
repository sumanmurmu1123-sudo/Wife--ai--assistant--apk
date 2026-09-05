package com.example.domain.security

import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class PendingAction(
    val actionName: String,
    val description: String,
    val requiresBiometric: Boolean,
    val onConfirmed: suspend () -> String
)

class SecurityConfirmationGuard(
    private val biometricEngine: BiometricAuthEngine
) {
    private val _pendingAction = MutableStateFlow<PendingAction?>(null)
    val pendingAction: StateFlow<PendingAction?> = _pendingAction

    // সেনসিটিভ টুল লিস্ট
    private val criticalTools = setOf(
        "initiatePhonePePayment",
        "deleteFileOrFolder",
        "lockOrResetDevice",
        "sendEmergencySos"
    )

    fun isCriticalAction(toolName: String): Boolean = criticalTools.contains(toolName)

    /**
     * সেনসিটিভ টাস্ক আটকে অনুমতি চাওয়া
     */
    fun holdForConfirmation(
        toolName: String,
        description: String,
        requiresBiometrics: Boolean = true,
        action: suspend () -> String
    ): String {
        _pendingAction.value = PendingAction(
            actionName = toolName,
            description = description,
            requiresBiometric = requiresBiometrics,
            onConfirmed = action
        )

        return if (requiresBiometrics) {
            "Boss, এটি একটি গুরুত্বপূর্ণ কাজ ($description)। এগিয়ে যেতে স্ক্রিনে আপনার ফিঙ্গারপ্রিন্ট বা পিন দিন।"
        } else {
            "Boss, আপনি কি নিশ্চিত যে আপনি '$description' সম্পন্ন করতে চান? 'হ্যাঁ' অথবা 'না' বলুন।"
        }
    }

    /**
     * ফিঙ্গারপ্রিন্ট অথবা মুখে "হ্যাঁ" বলার পর অ্যাকশন রান করা
     */
    suspend fun executeConfirmedAction(activity: FragmentActivity?): String {
        val pending = _pendingAction.value ?: return "কোনো অপেক্ষমাণ কাজ নেই, Boss।"

        if (pending.requiresBiometric) {
            if (activity == null) return "স্ক্রিন একটিভ নেই, বায়োমেট্রিক দেওয়া সম্ভব হয়নি।"
            val isVerified = biometricEngine.authenticateSensitiveAction(
                activity,
                SensitiveAction.PAYMENT_TRANSFER
            )
            if (!isVerified) {
                _pendingAction.value = null
                return "নিরাপত্তা যাচাই ব্যর্থ হয়েছে! কাজটি বাতিল করা হলো, Boss। 🔒"
            }
        }

        // অনুমোদন সফল হলে টাস্ক এক্সিকিউট
        val result = pending.onConfirmed()
        _pendingAction.value = null
        return result
    }

    fun cancelPendingAction(): String {
        _pendingAction.value = null
        return "কাজটি বাতিল করা হয়েছে, Boss।"
    }
}
