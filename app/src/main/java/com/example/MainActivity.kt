package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.launch
import com.example.data.AssistantState
import com.example.service.WifeForegroundService
import com.example.ui.MainAssistantScreen
import com.example.ui.PermissionsOnboardingScreen

class MainActivity : androidx.appcompat.app.AppCompatActivity() {

    private var hasPermissions by mutableStateOf(false)
    private lateinit var proactiveWifeEngine: com.example.domain.engine.ProactiveWifeEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        proactiveWifeEngine = com.example.domain.engine.ProactiveWifeEngine(this) { message ->
            runOnUiThread {
                println("💖 Wife: $message")
            }
        }
        proactiveWifeEngine.startEngine()

        checkCurrentPermissions()

        setContent {
            if (hasPermissions) {
                val userPrefs = remember { com.example.data.UserPreferences(this@MainActivity) }
                val prefs = getSharedPreferences("wife_prefs", android.content.Context.MODE_PRIVATE)
                var pcIpAddress by remember { mutableStateOf(prefs.getString("pc_ip", "192.168.1.100")!!) }
                val toolEngine = remember { com.example.domain.ToolExecutionEngine(this@MainActivity) }
                val drivingModeEngine = remember { com.example.domain.DrivingModeEngine(this@MainActivity) }
                val isRgbBorderActive by toolEngine.isRgbBorderActive.collectAsState()
                val isSocialModeActive by toolEngine.isSocialModeActive.collectAsState()
                val activePersona by toolEngine.activePersona.collectAsState()
                val isVideoStudioActive by toolEngine.isVideoStudioActive.collectAsState()
                val isVideoCallActive by toolEngine.isVideoCallActive.collectAsState()
                val isVoiceCallActive by toolEngine.isVoiceCallActive.collectAsState()
                val isAutoReplyActive by toolEngine.isAutoReplyActive.collectAsState()
                val isDrivingModeActive by drivingModeEngine.isDrivingModeActive.collectAsState()
                val pendingSecurityAction by toolEngine.securityGuard.pendingAction.collectAsState()
                var bossName by remember { mutableStateOf(userPrefs.bossName) }
                val scope = androidx.compose.runtime.rememberCoroutineScope()
                
                val cameraEngine = remember { com.example.vision.CameraVisionEngine(this@MainActivity) }
                val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

                androidx.compose.runtime.LaunchedEffect(isVideoCallActive) {
                    if (isVideoCallActive) {
                        cameraEngine.startCameraFeed(lifecycleOwner) { jpegBytes ->
                            val intent = Intent(this@MainActivity, WifeForegroundService::class.java).apply {
                                action = "ACTION_SEND_FRAME"
                                putExtra("frame", jpegBytes)
                            }
                            startService(intent)
                        }
                    } else {
                        cameraEngine.stopCameraFeed()
                    }
                }
                
                Box(modifier = Modifier.fillMaxSize()) {
                    MainAssistantScreen(
                        state = AssistantState.IDLE,
                        currentIp = pcIpAddress,
                        onUpdateIp = { 
                            pcIpAddress = it 
                            prefs.edit().putString("pc_ip", it).apply()
                        },
                        onShutdownPcClick = {
                            toolEngine.shutdownComputer(2)
                        },
                        onStartVoice = {
                            val intent = Intent(this@MainActivity, WifeForegroundService::class.java).apply {
                                action = WifeForegroundService.ACTION_START_SESSION
                            }
                            startService(intent)
                            if (::proactiveWifeEngine.isInitialized) {
                                proactiveWifeEngine.onUserSpoke()
                            }
                        },
                        onEndVoice = {
                            val intent = Intent(this@MainActivity, WifeForegroundService::class.java).apply {
                                action = WifeForegroundService.ACTION_STOP_SESSION
                            }
                            startService(intent)
                        },
                        isBorderLightActive = isRgbBorderActive,
                        bossName = bossName,
                        onUpdateBossName = { newName ->
                            bossName = newName
                            userPrefs.bossName = newName
                            // Note: If the service is running, it may not instantly get this UI update unless we broadcast it, 
                            // but it will reload when service restarts. For now this works for demo.
                        },
                        isSocialModeActive = isSocialModeActive,
                        onToggleSocialMode = { enable ->
                            toolEngine.toggleSocialMode(enable)
                            // The sessionManager is instantiated in WifeForegroundService.
                            // So toggling it from MainActivity requires sending an intent or we just use toolEngine state.
                            val intent = Intent(this@MainActivity, WifeForegroundService::class.java).apply {
                                action = "ACTION_UPDATE_SOCIAL_MODE"
                                putExtra("enable", enable)
                            }
                            startService(intent)
                        },
                        activePersona = activePersona,
                        onTogglePersona = { persona ->
                            toolEngine.setPersonaMode(persona)
                            val intent = Intent(this@MainActivity, WifeForegroundService::class.java).apply {
                                action = "ACTION_UPDATE_PERSONA"
                                putExtra("persona", persona.name)
                            }
                            startService(intent)
                        },
                        isVideoStudioActive = isVideoStudioActive,
                        onCloseVideoStudio = { toolEngine.setVideoStudioActive(false) },
                        onTriggerLaugh = {
                            val intent = Intent(this@MainActivity, WifeForegroundService::class.java).apply {
                                action = "ACTION_TRIGGER_LAUGH"
                            }
                            startService(intent)
                        },
                        onTriggerAppDownload = {
                            scope.launch {
                                toolEngine.appDownloadEngine.downloadFromPlayStore("")
                            }
                        },
                        isVideoCallActive = isVideoCallActive,
                        onStartVideoCall = { toolEngine.setVideoCallActive(true) },
                        onEndVideoCall = { toolEngine.setVideoCallActive(false) },
                        onToggleMute = { /* TBD */ },
                        onFlipCamera = { cameraEngine.isFrontCamera = !cameraEngine.isFrontCamera },
                        isVoiceCallActive = isVoiceCallActive,
                        onStartVoiceCall = { toolEngine.startAiVoiceCall() },
                        onEndVoiceCall = { toolEngine.endAiVoiceCall() },
                        onToggleSpeaker = { enable -> toolEngine.toggleSpeaker(enable) },
                        onTriggerGreeting = {
                            val intent = Intent(this@MainActivity, WifeForegroundService::class.java).apply {
                                action = "ACTION_TRIGGER_GREETING"
                            }
                            startService(intent)
                        },
                        isAutoReplyActive = isAutoReplyActive,
                        onToggleAutoReply = { toolEngine.setAutoReply(!isAutoReplyActive) }
                    )

                    pendingSecurityAction?.let { pending ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            com.example.ui.components.SecurityVerificationOverlay(
                                pendingAction = pending,
                                onVerifyFingerprint = {
                                    scope.launch {
                                        toolEngine.securityGuard.executeConfirmedAction(this@MainActivity)
                                    }
                                },
                                onCancel = {
                                    toolEngine.securityGuard.cancelPendingAction()
                                },
                                modifier = Modifier.padding(24.dp)
                            )
                        }
                    }
                }
            } else {
                PermissionsOnboardingScreen(
                    onAllPermissionsGranted = {
                        hasPermissions = true
                        startAssistantForegroundService()
                    }
                )
            }
        }
    }

    private fun checkCurrentPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.CAMERA
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        hasPermissions = permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        if (hasPermissions) {
            startAssistantForegroundService()
        }
    }

    private fun startAssistantForegroundService() {
        val intent = Intent(this, WifeForegroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::proactiveWifeEngine.isInitialized) {
            proactiveWifeEngine.stopEngine()
        }
    }
}
