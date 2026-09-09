package com.example.v2.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.v2.ui.components.FloatingNavBar
import com.example.v2.ui.components.NavDestination
import com.example.v2.ui.home.WifeAssistantV2Home
import com.example.v2.ui.talk.WifeAssistantV2Talk
import com.example.v2.voice.VoiceViewModel

import com.example.v2.ui.pc.WifeAssistantV2Pc
import com.example.v2.ui.memories.WifeAssistantV2Memories
import com.example.v2.ui.settings.WifeAssistantV2Settings
import com.example.v2.ui.onboarding.WifeAssistantV2Onboarding
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import android.Manifest

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun WifeAssistantV2App() {
    val permissionsList = mutableListOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.CAMERA
    ).apply {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    
    val permissionsState = rememberMultiplePermissionsState(
        permissions = permissionsList
    )

    if (!permissionsState.allPermissionsGranted) {
        WifeAssistantV2Onboarding(
            onPermissionsGranted = { /* Handled by recomposition when state changes */ }
        )
    } else {
        val voiceViewModel: VoiceViewModel = viewModel()
        var currentDestination by remember { mutableStateOf(NavDestination.HOME) }

        androidx.activity.compose.BackHandler(enabled = currentDestination != NavDestination.HOME) {
            currentDestination = NavDestination.HOME
        }

        Box(modifier = Modifier.fillMaxSize()) {
            // Main Content Area
            when (currentDestination) {
                NavDestination.HOME -> WifeAssistantV2Home(viewModel = voiceViewModel)
                NavDestination.TALK -> WifeAssistantV2Talk(viewModel = voiceViewModel)
                NavDestination.PC -> WifeAssistantV2Pc(viewModel = voiceViewModel)
                NavDestination.MEMORIES -> WifeAssistantV2Memories(viewModel = voiceViewModel)
                NavDestination.SETTINGS -> WifeAssistantV2Settings(viewModel = voiceViewModel)
            }

            // Floating Bottom Navigation
            FloatingNavBar(
                currentDestination = currentDestination,
                onNavigate = { currentDestination = it },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
