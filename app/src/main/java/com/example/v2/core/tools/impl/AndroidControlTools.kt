package com.example.v2.core.tools.impl

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.os.Build
import android.provider.Settings
import com.example.service.WifeAccessibilityService
import com.example.v2.core.tools.AssistantTool
import com.example.v2.core.tools.ToolCategory
import com.example.v2.core.tools.ToolResult
import com.example.v2.core.tools.ToolStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FlashlightTool(private val context: Context) : AssistantTool {
    override val id = "android.flashlight"
    override val name = "Flashlight"
    override val description = "Hardware torch control with real status verification."
    override val category = ToolCategory.ANDROID_CONTROL
    override val keywords = listOf("flashlight", "torch", "light")
    override val requiredPermissions = emptySet<String>()
    override val parametersSchema = mapOf(
        "type" to "object",
        "properties" to mapOf(
            "action" to mapOf("type" to "string", "description" to "ON, OFF, or TOGGLE")
        )
    )

    private var isTorchOn = false

    override suspend fun checkRealAvailability(context: Context): ToolStatus {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager ?: return ToolStatus.UNAVAILABLE
        return try {
            val cameraIds = cameraManager.cameraIdList
            val hasFlash = cameraIds.any { id ->
                val chars = cameraManager.getCameraCharacteristics(id)
                chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
            if (hasFlash) ToolStatus.AVAILABLE else ToolStatus.UNAVAILABLE
        } catch (e: Exception) {
            ToolStatus.UNAVAILABLE
        }
    }

    override suspend fun execute(params: Map<String, Any?>): ToolResult = withContext(Dispatchers.IO) {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            ?: return@withContext ToolResult(false, "Camera hardware service unavailable.")

        try {
            val cameraIds = cameraManager.cameraIdList
            val backCameraWithFlash = cameraIds.firstOrNull { id ->
                val chars = cameraManager.getCameraCharacteristics(id)
                chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true &&
                        chars.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
            } ?: cameraIds.firstOrNull { id ->
                cameraManager.getCameraCharacteristics(id).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }

            if (backCameraWithFlash == null) {
                return@withContext ToolResult(false, "No hardware flash unit found on this device.")
            }

            val action = params["action"]?.toString()?.uppercase() ?: "TOGGLE"
            val targetState = when (action) {
                "ON" -> true
                "OFF" -> false
                else -> !isTorchOn
            }

            cameraManager.setTorchMode(backCameraWithFlash, targetState)
            isTorchOn = targetState

            ToolResult(true, "Flashlight successfully turned ${if (targetState) "ON" else "OFF"}.")
        } catch (e: Exception) {
            ToolResult(false, "Flashlight hardware error: ${e.message}")
        }
    }
}

class ScreenshotTool(private val context: Context) : AssistantTool {
    override val id = "android.screenshot"
    override val name = "Screenshot"
    override val description = "Captures current device screen display."
    override val category = ToolCategory.ANDROID_CONTROL
    override val keywords = listOf("screenshot", "screen", "capture", "snapshot")
    override val requiredPermissions = emptySet<String>()
    override val parametersSchema = mapOf("type" to "object", "properties" to emptyMap<String, Any>())

    override suspend fun checkRealAvailability(context: Context): ToolStatus {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (WifeAccessibilityService.instance != null) ToolStatus.AVAILABLE else ToolStatus.PERMISSION_REQUIRED
        } else {
            ToolStatus.UNAVAILABLE
        }
    }

    override suspend fun execute(params: Map<String, Any?>): ToolResult {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val service = WifeAccessibilityService.instance
            if (service != null) {
                val success = service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT)
                return if (success) {
                    ToolResult(true, "Screenshot captured successfully.")
                } else {
                    ToolResult(false, "System rejected screenshot request.")
                }
            } else {
                return ToolResult(
                    false,
                    "Accessibility service required to capture screenshots. Enable in Settings.",
                    requiresPermission = true
                )
            }
        }
        return ToolResult(false, "Screenshot requires Android 9.0 (Pie) or higher.")
    }

    override fun openSettingsOrFix(context: Context) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}

class VolumeControlTool(private val context: Context) : AssistantTool {
    override val id = "android.volume"
    override val name = "Volume Control"
    override val description = "Adjusts device media volume with verification."
    override val category = ToolCategory.ANDROID_CONTROL
    override val keywords = listOf("volume", "sound", "loudness", "mute")
    override val requiredPermissions = emptySet<String>()
    override val parametersSchema = mapOf(
        "type" to "object",
        "properties" to mapOf(
            "level" to mapOf("type" to "number", "description" to "Target volume percentage (0-100)")
        )
    )

    override suspend fun checkRealAvailability(context: Context): ToolStatus {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        return if (am != null) ToolStatus.AVAILABLE else ToolStatus.UNAVAILABLE
    }

    override suspend fun execute(params: Map<String, Any?>): ToolResult {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            ?: return ToolResult(false, "Audio service unavailable.")

        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val levelDouble = (params["level"] as? Number)?.toDouble() ?: 70.0
        val targetIndex = ((levelDouble.coerceIn(0.0, 100.0) / 100.0) * maxVolume).toInt()

        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetIndex, AudioManager.FLAG_SHOW_UI)
        val verifiedIndex = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val verifiedPercent = ((verifiedIndex.toFloat() / maxVolume) * 100).toInt()

        return ToolResult(true, "Volume adjusted to $verifiedPercent% (Index: $verifiedIndex/$maxVolume).")
    }
}

class HomeTool(private val context: Context) : AssistantTool {
    override val id = "android.home"
    override val name = "Home"
    override val description = "Navigates to the device home screen."
    override val category = ToolCategory.ANDROID_CONTROL
    override val keywords = listOf("home", "launcher", "desktop")
    override val requiredPermissions = emptySet<String>()
    override val parametersSchema = mapOf("type" to "object", "properties" to emptyMap<String, Any>())

    override suspend fun execute(params: Map<String, Any?>): ToolResult {
        val service = WifeAccessibilityService.instance
        if (service != null) {
            val performed = service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
            if (performed) return ToolResult(true, "Navigated to Home screen via accessibility action.")
        }

        // Fallback: Launch HOME intent
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            ToolResult(true, "Navigated to Home screen.")
        } catch (e: Exception) {
            ToolResult(false, "Failed to navigate home: ${e.message}")
        }
    }
}

class BackTool(private val context: Context) : AssistantTool {
    override val id = "android.back"
    override val name = "Back"
    override val description = "Executes the system Back navigation action."
    override val category = ToolCategory.ANDROID_CONTROL
    override val keywords = listOf("back", "previous", "return")
    override val requiredPermissions = emptySet<String>()
    override val parametersSchema = mapOf("type" to "object", "properties" to emptyMap<String, Any>())

    override suspend fun checkRealAvailability(context: Context): ToolStatus {
        return if (WifeAccessibilityService.instance != null) ToolStatus.AVAILABLE else ToolStatus.PERMISSION_REQUIRED
    }

    override suspend fun execute(params: Map<String, Any?>): ToolResult {
        val service = WifeAccessibilityService.instance
            ?: return ToolResult(false, "Accessibility service required for Back action.", requiresPermission = true)
        val success = service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
        return if (success) ToolResult(true, "Back action executed.") else ToolResult(false, "Back action could not be performed.")
    }
}

class RecentAppsTool(private val context: Context) : AssistantTool {
    override val id = "android.recent_apps"
    override val name = "Recent Apps"
    override val description = "Opens the Android app switcher / overview."
    override val category = ToolCategory.ANDROID_CONTROL
    override val keywords = listOf("recents", "overview", "apps", "switcher")
    override val requiredPermissions = emptySet<String>()
    override val parametersSchema = mapOf("type" to "object", "properties" to emptyMap<String, Any>())

    override suspend fun checkRealAvailability(context: Context): ToolStatus {
        return if (WifeAccessibilityService.instance != null) ToolStatus.AVAILABLE else ToolStatus.PERMISSION_REQUIRED
    }

    override suspend fun execute(params: Map<String, Any?>): ToolResult {
        val service = WifeAccessibilityService.instance
            ?: return ToolResult(false, "Accessibility service required for Recent Apps.", requiresPermission = true)
        val success = service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS)
        return if (success) ToolResult(true, "Opened Recent Apps overview.") else ToolResult(false, "Failed to open Recents.")
    }
}

class LockScreenTool(private val context: Context) : AssistantTool {
    override val id = "android.lock_screen"
    override val name = "Lock Screen"
    override val description = "Locks the device display securely."
    override val category = ToolCategory.ANDROID_CONTROL
    override val keywords = listOf("lock", "screen off", "sleep")
    override val requiredPermissions = emptySet<String>()
    override val parametersSchema = mapOf("type" to "object", "properties" to emptyMap<String, Any>())

    override suspend fun checkRealAvailability(context: Context): ToolStatus {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (WifeAccessibilityService.instance != null) ToolStatus.AVAILABLE else ToolStatus.PERMISSION_REQUIRED
        } else {
            ToolStatus.UNAVAILABLE
        }
    }

    override suspend fun execute(params: Map<String, Any?>): ToolResult {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val service = WifeAccessibilityService.instance
                ?: return ToolResult(false, "Accessibility service required to lock screen.", requiresPermission = true)
            val success = service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN)
            return if (success) ToolResult(true, "Device screen locked.") else ToolResult(false, "Failed to lock screen.")
        }
        return ToolResult(false, "Lock screen requires Android 9.0+.")
    }
}

class NotificationShadeTool(private val context: Context) : AssistantTool {
    override val id = "android.notification_shade"
    override val name = "Notification Shade"
    override val description = "Expands the system notification drawer."
    override val category = ToolCategory.ANDROID_CONTROL
    override val keywords = listOf("notifications", "shade", "pull down", "alerts")
    override val requiredPermissions = emptySet<String>()
    override val parametersSchema = mapOf("type" to "object", "properties" to emptyMap<String, Any>())

    override suspend fun checkRealAvailability(context: Context): ToolStatus {
        return if (WifeAccessibilityService.instance != null) ToolStatus.AVAILABLE else ToolStatus.PERMISSION_REQUIRED
    }

    override suspend fun execute(params: Map<String, Any?>): ToolResult {
        val service = WifeAccessibilityService.instance
            ?: return ToolResult(false, "Accessibility service required to expand notifications.", requiresPermission = true)
        val success = service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS)
        return if (success) ToolResult(true, "Notification shade expanded.") else ToolResult(false, "Failed to expand notification shade.")
    }
}

class QuickSettingsTool(private val context: Context) : AssistantTool {
    override val id = "android.quick_settings"
    override val name = "Quick Settings"
    override val description = "Expands the Android quick toggles panel."
    override val category = ToolCategory.ANDROID_CONTROL
    override val keywords = listOf("quick settings", "toggles", "panel")
    override val requiredPermissions = emptySet<String>()
    override val parametersSchema = mapOf("type" to "object", "properties" to emptyMap<String, Any>())

    override suspend fun checkRealAvailability(context: Context): ToolStatus {
        return if (WifeAccessibilityService.instance != null) ToolStatus.AVAILABLE else ToolStatus.PERMISSION_REQUIRED
    }

    override suspend fun execute(params: Map<String, Any?>): ToolResult {
        val service = WifeAccessibilityService.instance
            ?: return ToolResult(false, "Accessibility service required to open quick settings.", requiresPermission = true)
        val success = service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS)
        return if (success) ToolResult(true, "Quick settings panel opened.") else ToolResult(false, "Failed to open quick settings.")
    }
}

class WifiSettingsTool(private val context: Context) : AssistantTool {
    override val id = "android.wifi_settings"
    override val name = "Wi-Fi Settings"
    override val description = "Opens the device Wi-Fi configuration."
    override val category = ToolCategory.ANDROID_CONTROL
    override val keywords = listOf("wifi", "wlan", "network settings", "internet settings")
    override val requiredPermissions = emptySet<String>()
    override val parametersSchema = mapOf("type" to "object", "properties" to emptyMap<String, Any>())

    override suspend fun execute(params: Map<String, Any?>): ToolResult {
        val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            ToolResult(true, "Wi-Fi settings opened successfully.")
        } catch (e: Exception) {
            ToolResult(false, "Failed to launch Wi-Fi settings: ${e.message}")
        }
    }
}

class BluetoothSettingsTool(private val context: Context) : AssistantTool {
    override val id = "android.bluetooth_settings"
    override val name = "Bluetooth Settings"
    override val description = "Opens the device Bluetooth pairing and management screen."
    override val category = ToolCategory.ANDROID_CONTROL
    override val keywords = listOf("bluetooth", "bt", "pair", "wireless")
    override val requiredPermissions = emptySet<String>()
    override val parametersSchema = mapOf("type" to "object", "properties" to emptyMap<String, Any>())

    override suspend fun execute(params: Map<String, Any?>): ToolResult {
        val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            ToolResult(true, "Bluetooth settings opened successfully.")
        } catch (e: Exception) {
            ToolResult(false, "Failed to launch Bluetooth settings: ${e.message}")
        }
    }
}

class DisplaySettingsTool(private val context: Context) : AssistantTool {
    override val id = "android.display_settings"
    override val name = "Display Settings"
    override val description = "Opens display, brightness, and screen timeout settings."
    override val category = ToolCategory.ANDROID_CONTROL
    override val keywords = listOf("display", "brightness", "screen", "dark mode")
    override val requiredPermissions = emptySet<String>()
    override val parametersSchema = mapOf("type" to "object", "properties" to emptyMap<String, Any>())

    override suspend fun execute(params: Map<String, Any?>): ToolResult {
        val intent = Intent(Settings.ACTION_DISPLAY_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            ToolResult(true, "Display settings opened successfully.")
        } catch (e: Exception) {
            ToolResult(false, "Failed to launch Display settings: ${e.message}")
        }
    }
}

typealias VolumeTool = VolumeControlTool

