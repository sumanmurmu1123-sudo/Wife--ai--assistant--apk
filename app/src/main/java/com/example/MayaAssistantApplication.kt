package com.example

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import android.util.Log

class MayaAssistantApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.i("MayaApp", "MayaAssistantApplication created!")
        
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                // Try automatic initialization first (requires google-services.json)
                try {
                    FirebaseApp.initializeApp(this)
                    Log.i("MayaApp", "Firebase initialized successfully via google-services.json")
                } catch (e: Exception) {
                    // Fallback to manual initialization if keys are provided in string resources
                    val apiKey = getString(R.string.fb_api_key)
                    val appId = getString(R.string.fb_app_id)
                    val projectId = getString(R.string.fb_project_id)

                    if (apiKey != "MISSING" && appId != "MISSING" && projectId != "MISSING") {
                        val options = FirebaseOptions.Builder()
                            .setApiKey(apiKey)
                            .setApplicationId(appId)
                            .setProjectId(projectId)
                            .build()
                        FirebaseApp.initializeApp(this, options)
                        Log.i("MayaApp", "Firebase initialized successfully via manual configuration")
                    } else {
                        Log.w("MayaApp", "Firebase not initialized: google-services.json missing and no manual keys provided")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MayaApp", "Firebase initialization fatal error: ${e.message}")
        }
    }
}
