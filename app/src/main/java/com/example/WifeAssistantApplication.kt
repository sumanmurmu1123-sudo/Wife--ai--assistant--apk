package com.example

import android.app.Application
import com.google.firebase.FirebaseApp
import android.util.Log

class WifeAssistantApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
                Log.i("WifeApp", "Firebase initialized successfully in Application class")
            }
        } catch (e: Exception) {
            Log.e("WifeApp", "Firebase initialization failed in Application class: ${e.message}")
        }
    }
}
