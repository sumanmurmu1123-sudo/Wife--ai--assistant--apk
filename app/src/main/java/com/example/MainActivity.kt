package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import android.graphics.Color as AndroidColor
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.example.v2.core.WifeAssistantCore
import androidx.compose.material3.adaptive.currentWindowSize
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.toSize
import com.example.v2.ui.WifeAssistantV2App

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        android.util.Log.d("MainActivity", "onCreate started")
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)
        
        // Initialize Core
        try {
            android.util.Log.d("MainActivity", "Initializing Core...")
            WifeAssistantCore.getInstance(this)
            android.util.Log.d("MainActivity", "Core initialized")
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Core initialization failed", e)
        }
        
        val navigateTo = intent.getStringExtra("navigate_to")
        setContent {
            android.util.Log.d("MainActivity", "Setting content...")
            WifeAssistantV2App(initialNavigation = navigateTo)
        }
    }
}
