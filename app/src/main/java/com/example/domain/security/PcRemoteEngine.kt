package com.example.domain.security

import android.content.Context

class PcRemoteEngine(private val context: Context) {
    fun lockPc(): String {
        return "Simulated PC Lock via SSH/API"
    }
    fun shutdownPc(): String {
        return "Simulated PC Shutdown via SSH/API"
    }
}
