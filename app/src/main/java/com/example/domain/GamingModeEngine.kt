package com.example.domain

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class GamingModeEngine(
    private val context: Context,
    private val toolEngine: ToolExecutionEngine
) {
    private val _isGameMoodActive = MutableStateFlow(false)
    val isGameMoodActive: StateFlow<Boolean> = _isGameMoodActive

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun toggleGameMood(enable: Boolean): String {
        _isGameMoodActive.value = enable

        // Manage Do Not Disturb (DND)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (notificationManager.isNotificationPolicyAccessGranted) {
                if (enable) {
                    notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
                } else {
                    notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                }
            }
        }

        return if (enable) {
            "🎮 Game Mood ON! DND enabled and RGB gaming protocol initiated. Go crush them, Boss!"
        } else {
            "Exited Game Mood. Great matches, Boss!"
        }
    }

    suspend fun launchGame(gameName: String, targetDevice: String = "phone"): String {
        return if (targetDevice.equals("pc", ignoreCase = true) || targetDevice.equals("computer", ignoreCase = true)) {
            val pcLaunchCommand = when (gameName.lowercase()) {
                "steam" -> "start steam://"
                "valorant" -> "start valorant"
                "discord" -> "start discord"
                "gta", "gta v" -> "start com.epicgames.launcher://apps/GrandTheftAutoV"
                else -> "start $gameName"
            }
            toolEngine.sendPcRemoteCommand("system", pcLaunchCommand)
            "Launching $gameName on your gaming rig, Boss! Let's get that win!"
        } else {
            toolEngine.openAnyApp(gameName)
        }
    }
}
