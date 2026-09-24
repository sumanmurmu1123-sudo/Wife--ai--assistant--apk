package com.example.v2.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.v2.ui.components.FloatingNavBar
import com.example.v2.ui.components.NavDestination
import com.example.v2.ui.home.WifeAssistantV2Home
import com.example.v2.ui.talk.WifeAssistantV2Talk
import com.example.v2.ui.gemini.WifeAssistantV2Gemini
import com.example.v2.voice.VoiceViewModel

import com.example.v2.ui.pc.WifeAssistantV2Pc
import com.example.v2.ui.memories.WifeAssistantV2Memories
import com.example.v2.ui.settings.WifeAssistantV2Settings
import com.example.v2.ui.onboarding.WifeAssistantV2Onboarding
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import android.Manifest

import com.example.v2.payment.presentation.PaymentScreen
import com.example.v2.payment.presentation.PaymentViewModel
import com.example.v2.instagram.presentation.InstagramReelScreen
import com.example.v2.instagram.presentation.InstagramReelViewModel
import com.example.v2.ui.phonecontrol.PhoneControlScreen
import com.example.v2.rix.ui.OpportunityCenterScreen
import com.example.v2.video.ui.editor.VideoStudioScreen
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun WifeAssistantV2App(initialNavigation: String? = null) {
    val criticalPermissions = listOf(Manifest.permission.RECORD_AUDIO)
    val optionalPermissions = mutableListOf(
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.CAMERA
    ).apply {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    
    val criticalPermissionsState = rememberMultiplePermissionsState(permissions = criticalPermissions)
    val optionalPermissionsState = rememberMultiplePermissionsState(permissions = optionalPermissions)

    val context = androidx.compose.ui.platform.LocalContext.current
    val hasOverlay = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
        android.provider.Settings.canDrawOverlays(context)
    } else true
    
    val isBatteryOptimizationIgnored = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
        (context.getSystemService(android.content.Context.POWER_SERVICE) as android.os.PowerManager)
            .isIgnoringBatteryOptimizations(context.packageName)
    } else true

    val isNotificationListenerEnabled = android.provider.Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        ?.contains(context.packageName) == true

    if (!criticalPermissionsState.allPermissionsGranted || !hasOverlay || !isBatteryOptimizationIgnored || !isNotificationListenerEnabled) {
        WifeAssistantV2Onboarding(
            onPermissionsGranted = { /* Handled by recomposition */ },
            onBeforePermissionRequest = {
                com.example.v2.core.StateManager.updateState { it.copy(overlayState = com.example.v2.core.OverlayState.SUSPENDED_FOR_PERMISSION) }
            }
        )
    } else {
        val voiceViewModel: VoiceViewModel = viewModel()
        val voiceState by voiceViewModel.state.collectAsState()
        val audioLevel by voiceViewModel.audioLevel.collectAsState()
        val paymentViewModel: PaymentViewModel = viewModel()
        val instagramViewModel: InstagramReelViewModel = viewModel()
        
        var currentDestination by remember { mutableStateOf(NavDestination.HOME) }
        var isUnlocked by remember { mutableStateOf(false) }
        var showPaymentScreen by remember { mutableStateOf(false) }
        
        // Handle initial navigation if provided
        LaunchedEffect(initialNavigation) {
            if (initialNavigation == "settings_api_cloud") {
                currentDestination = NavDestination.SETTINGS
            }
        }

        var showInstagramScreen by remember { mutableStateOf(false) }
        var showPhoneControlScreen by remember { mutableStateOf(false) }
        var showOpportunityCenterScreen by remember { mutableStateOf(false) }
        var showVideoStudioScreen by remember { mutableStateOf(false) }

        val micPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            android.util.Log.i("WifeVoice", "[PERMISSION] RESULT_${if (isGranted) "GRANTED" else "DENIED"}")
            // Rule: Restore overlay after dialog is gone
            com.example.v2.core.StateManager.updateState { it.copy(overlayState = com.example.v2.core.OverlayState.VISIBLE) }
            
            if (isGranted) {
                voiceViewModel.onMicrophoneTapped(context)
            }
        }

        LaunchedEffect(Unit) {
            launch {
                voiceViewModel.permissionRequestEvent.collect { permission ->
                    if (permission == android.Manifest.permission.RECORD_AUDIO) {
                        micPermissionLauncher.launch(permission)
                    }
                }
            }
            launch {
                voiceViewModel.paymentEvent.collect { intent ->
                    showPaymentScreen = true
                    paymentViewModel.initiatePaymentRequest(
                        amount = intent.amount,
                        recipientName = intent.recipientName,
                        upiId = intent.upiId.ifEmpty { "unknown@upi" },
                        note = "Payment via Wife Assistant"
                    )
                }
            }
            launch {
                voiceViewModel.instagramReelEvent.collect {
                    showInstagramScreen = true
                }
            }
            launch {
                voiceViewModel.phoneControlEvent.collect {
                    showPhoneControlScreen = true
                }
            }
            launch {
                voiceViewModel.opportunityCenterEvent.collect {
                    showOpportunityCenterScreen = true
                }
            }
            launch {
                voiceViewModel.videoStudioEvent.collect {
                    showVideoStudioScreen = true
                }
            }
        }

        val isAnyOverlayVisible = showPaymentScreen || showInstagramScreen || showPhoneControlScreen || showOpportunityCenterScreen || showVideoStudioScreen

        var backPressedTime by remember { mutableLongStateOf(0L) }
        
        androidx.activity.compose.BackHandler(enabled = currentDestination == NavDestination.HOME && !isAnyOverlayVisible) {
            val now = System.currentTimeMillis()
            if (now - backPressedTime < 2000) {
                (context as? android.app.Activity)?.finish()
            } else {
                backPressedTime = now
                android.widget.Toast.makeText(context, "Press back again to exit", android.widget.Toast.LENGTH_SHORT).show()
            }
        }

        androidx.activity.compose.BackHandler(enabled = isAnyOverlayVisible || (currentDestination != NavDestination.HOME && currentDestination != NavDestination.LOCK_SCREEN)) {
            when {
                showPaymentScreen -> showPaymentScreen = false
                showInstagramScreen -> showInstagramScreen = false
                showPhoneControlScreen -> showPhoneControlScreen = false
                showOpportunityCenterScreen -> showOpportunityCenterScreen = false
                showVideoStudioScreen -> showVideoStudioScreen = false
                else -> currentDestination = NavDestination.HOME
            }
        }

        BoxWithConstraints(modifier = Modifier.fillMaxSize().background(com.example.v2.ui.theme.DarkMidnightBlue)) {
            val isExpanded = maxWidth > 840.dp
            val sidePadding = if (isExpanded) 120.dp else 0.dp

            // Edge Light Effect (Global)
            if (currentDestination != NavDestination.LOCK_SCREEN) {
                com.example.v2.ui.components.EdgeLight(
                    state = voiceState,
                    audioLevel = audioLevel
                )
            }

            // Main Content Area
            Box(modifier = Modifier.fillMaxSize().padding(horizontal = sidePadding)) {
                if (!isUnlocked) {
                    com.example.v2.ui.lock.LockScreen(
                        viewModel = voiceViewModel,
                        onUnlock = { isUnlocked = true }
                    )
                } else {
                    when (currentDestination) {
                        NavDestination.LOCK_SCREEN, NavDestination.HOME -> WifeAssistantV2Home(
                            viewModel = voiceViewModel,
                            onNavigateToProfile = { currentDestination = NavDestination.PROFILE },
                            onNavigateToTools = { currentDestination = NavDestination.TOOLS },
                            onNavigateToGemini = { currentDestination = NavDestination.GEMINI }
                        )
                        NavDestination.TALK -> WifeAssistantV2Talk(viewModel = voiceViewModel)
                        NavDestination.GEMINI -> WifeAssistantV2Gemini(
                            viewModel = voiceViewModel,
                            onNavigateBack = { currentDestination = NavDestination.HOME }
                        )
                        NavDestination.PC -> WifeAssistantV2Pc(viewModel = voiceViewModel)
                        NavDestination.MEMORIES -> WifeAssistantV2Memories(viewModel = voiceViewModel)
                        NavDestination.SETTINGS -> {
                            val initialRoute = if (initialNavigation == "settings_api_cloud") {
                                com.example.v2.ui.settings.SettingsRoute.API_CLOUD
                            } else {
                                com.example.v2.ui.settings.SettingsRoute.HOME
                            }
                            WifeAssistantV2Settings(viewModel = voiceViewModel, initialRoute = initialRoute)
                        }
                        NavDestination.PROFILE -> com.example.v2.ui.profile.WifeAssistantV2Profile(viewModel = voiceViewModel)
                        NavDestination.TOOLS -> com.example.v2.ui.tools.ToolCenterScreen(
                            onNavigateBack = { currentDestination = NavDestination.HOME }
                        )
                    }
                }
            }

            // Floating Bottom Navigation
            if (isUnlocked) {
                Box(
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    FloatingNavBar(
                        currentDestination = currentDestination,
                        onNavigate = { currentDestination = it },
                        voiceState = voiceState,
                        audioLevel = audioLevel,
                        onMicClick = { voiceViewModel.onMicrophoneTapped(context) },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                    
                    // Floating Mic Button above the nav bar
                    com.example.v2.ui.components.FloatingMicButton(
                        voiceState = voiceState,
                        onClick = { voiceViewModel.onMicrophoneTapped(context) },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .padding(bottom = 104.dp) // Height of nav bar (76) + bottom padding (16) + gap (12)
                    )
                }
            }
            
            if (showPaymentScreen) {
                PaymentScreen(
                    viewModel = paymentViewModel,
                    onClose = { showPaymentScreen = false }
                )
            }
            if (showInstagramScreen) {
                InstagramReelScreen(
                    viewModel = instagramViewModel,
                    onClose = { showInstagramScreen = false }
                )
            }
            if (showPhoneControlScreen) {
                PhoneControlScreen(
                    viewModel = voiceViewModel,
                    onClose = { showPhoneControlScreen = false }
                )
            }
            if (showOpportunityCenterScreen) {
                OpportunityCenterScreen(
                    onClose = { showOpportunityCenterScreen = false }
                )
            }
            
            if (showVideoStudioScreen) {
                // The VideoStudioScreen could just be a full screen surface, or a modal
                Box(modifier = Modifier.fillMaxSize()) {
                    VideoStudioScreen()
                    
                    // Simple close button
                    androidx.compose.material3.IconButton(
                        onClick = { showVideoStudioScreen = false },
                        modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                    ) {
                        androidx.compose.material3.Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = androidx.compose.ui.graphics.Color.White
                        )
                    }
                }
            }
        }
    }
}
