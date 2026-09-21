package com.example

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import android.util.Log

class WifeAssistantApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                // Try automatic initialization first (requires google-services.json)
                try {
                    FirebaseApp.initializeApp(this)
                    Log.i("WifeApp", "Firebase initialized successfully via google-services.json")
                } catch (e: Exception) {
                    // Fallback to manual initialization if keys are provided in string resources (via Gradle/Secrets)
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
                        Log.i("WifeApp", "Firebase initialized successfully via manual configuration")
                    } else {
                        Log.w("WifeApp", "Firebase not initialized: google-services.json missing and no manual keys provided")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("WifeApp", "Firebase initialization fatal error: ${e.message}")
        }
    }
}
