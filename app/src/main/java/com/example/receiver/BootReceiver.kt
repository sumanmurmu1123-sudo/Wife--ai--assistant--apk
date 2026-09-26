package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.v2.core.MayaAssistantCore

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || 
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            Log.i("WifeBoot", "[BOOT] Device rebooted. Initializing Maya V2 Core.")
            
            // Initializing core will set up managers and potentially start services
            MayaAssistantCore.getInstance(context)
        }
    }
}
