package com.example.domain.security

class VoiceGuardianFilter(
    private val guardManager: GuardModeManager,
    private val intruderService: IntruderCaptureService
) {

    /**
     * Intercepts and parses commands when Guard Mode is active.
     * @param voiceInput Raw transcript from voice recognition
     * @param isVoiceMatched Result of local biometric speaker verification (if enabled)
     */
    fun processVoiceCommand(voiceInput: String, isVoiceMatched: Boolean): String {
        val cleanInput = voiceInput.lowercase().trim()

        // Mode Activation Trigger
        if (cleanInput.contains("voice guardian on") || cleanInput.contains("guard mode on")) {
            guardManager.isGuardModeActive = true
            return "🔒 Voice Guardian ON. Away/Guard mode activated. Anjaan babu/strangers will be blocked."
        }

        // Mode Deactivation Trigger
        if (cleanInput.contains("voice guardian off") || cleanInput.contains("guard mode off")) {
            if (isVoiceMatched || cleanInput.contains("unlock") || guardManager.verifyPasscode(cleanInput)) {
                guardManager.isGuardModeActive = false
                return "🔓 Voice Guardian deactivated. Welcome back, Sujit Babu!"
            } else {
                intruderService.triggerIntruderLockdown {}
                return "[ 🔒 ACCESS DENIED — DEVICE LOCKED ]"
            }
        }

        // Intercept all requests if Guard Mode is ACTIVE
        if (guardManager.isGuardModeActive) {
            if (!isVoiceMatched) {
                // Unknown/Anjaan person detected -> Silent or Lock
                intruderService.triggerIntruderLockdown {}
                return "[ 🔒 VOICE NOT RECOGNIZED — ACTION REJECTED ]"
            }
        }

        // Normal execution path for authorized user
        return "Command passed to execution engine."
    }
}
