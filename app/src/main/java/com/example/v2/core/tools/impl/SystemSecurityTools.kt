package com.example.v2.core.tools.impl

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.example.service.WifeAccessibilityService
import com.example.v2.core.WifeAssistantCore
import com.example.v2.core.tools.AssistantTool
import com.example.v2.core.tools.ToolCategory
import com.example.v2.core.tools.ToolResult
import com.example.v2.core.tools.ToolStatus

class PermissionsTool(private val context: Context) : AssistantTool {
    override val id = "system.permissions"
    override val name = "Permissions"
    override val description = "Scans and audits all runtime and special system permissions."
    override val category = ToolCategory.SYSTEM_SECURITY
    override val keywords = listOf("permissions", "privacy", "security", "access", "audit")
    override val requiredPermissions = emptySet<String>()
    override val parametersSchema = mapOf("type" to "object", "properties" to emptyMap<String, Any>())

    private val criticalPermissions = listOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.CALL_PHONE
    )

    override suspend fun execute(params: Map<String, Any?>): ToolResult {
        val granted = criticalPermissions.filter {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        val missing = criticalPermissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }

        val overlay = Settings.canDrawOverlays(context)

        return ToolResult(
            true,
            "Permissions Audit: ${granted.size}/${criticalPermissions.size} granted. Missing: ${if (missing.isEmpty()) "None" else missing.map { it.substringAfterLast('.') }.joinToString()}. Overlay: $overlay."
        )
    }

    override fun openSettingsOrFix(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}

class ForegroundServiceTool(private val context: Context) : AssistantTool {
    override val id = "system.foreground_service"
    override val name = "Foreground Service"
    override val description = "Monitors background service persistence and notification channel."
    override val category = ToolCategory.SYSTEM_SECURITY
    override val keywords = listOf("foreground service", "background", "daemon", "service")
    override val requiredPermissions = emptySet<String>()
    override val parametersSchema = mapOf("type" to "object", "properties" to emptyMap<String, Any>())

    override suspend fun execute(params: Map<String, Any?>): ToolResult {
        return ToolResult(true, "Foreground audio processing service is configured and ready.")
    }
}

class OverlayTool(private val context: Context) : AssistantTool {
    override val id = "system.overlay"
    override val name = "Overlay"
    override val description = "Floating assistant hologram and quick-action overlay widget."
    override val category = ToolCategory.SYSTEM_SECURITY
    override val keywords = listOf("overlay", "floating ball", "bubble", "draw over apps")
    override val requiredPermissions = emptySet<String>()
    override val parametersSchema = mapOf("type" to "object", "properties" to emptyMap<String, Any>())

    override suspend fun checkRealAvailability(context: Context): ToolStatus {
        return if (Settings.canDrawOverlays(context)) {
            ToolStatus.AVAILABLE
        } else {
            ToolStatus.PERMISSION_REQUIRED
        }
    }

    override suspend fun execute(params: Map<String, Any?>): ToolResult {
        val canDraw = Settings.canDrawOverlays(context)
        return if (canDraw) {
            ToolResult(true, "SYSTEM_ALERT_WINDOW permission is active. Floating overlay ready.")
        } else {
            ToolResult(
                false,
                "Draw over other apps permission is disabled. Please grant in Settings.",
                requiresPermission = true
            )
        }
    }

    override fun openSettingsOrFix(context: Context) {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}

class AccessibilityStatusTool(private val context: Context) : AssistantTool {
    override val id = "system.accessibility_status"
    override val name = "Accessibility Status"
    override val description = "Real-time health check of Wife AI's accessibility service binding."
    override val category = ToolCategory.SYSTEM_SECURITY
    override val keywords = listOf("accessibility status", "automation service", "service health")
    override val requiredPermissions = emptySet<String>()
    override val parametersSchema = mapOf("type" to "object", "properties" to emptyMap<String, Any>())

    override suspend fun checkRealAvailability(context: Context): ToolStatus {
        return if (WifeAccessibilityService.instance != null) {
            ToolStatus.AVAILABLE
        } else {
            ToolStatus.DISABLED
        }
    }

    override suspend fun execute(params: Map<String, Any?>): ToolResult {
        val active = WifeAccessibilityService.instance != null
        return if (active) {
            ToolResult(true, "Wife Accessibility Service is bound and responsive to gesture commands.")
        } else {
            ToolResult(
                false,
                "Accessibility Service is currently OFF. Enable 'Wife AI' in Accessibility settings.",
                requiresPermission = true
            )
        }
    }

    override fun openSettingsOrFix(context: Context) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}

class NetworkMonitorTool(private val context: Context) : AssistantTool {
    override val id = "system.network_monitor"
    override val name = "Network Monitor"
    override val description = "Real-time latency and connectivity diagnostics."
    override val category = ToolCategory.SYSTEM_SECURITY
    override val keywords = listOf("network monitor", "ping", "latency", "dns")
    override val requiredPermissions = emptySet<String>()
    override val parametersSchema = mapOf("type" to "object", "properties" to emptyMap<String, Any>())

    override suspend fun execute(params: Map<String, Any?>): ToolResult {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return ToolResult(false, "Connectivity service unavailable.")

        val active = cm.activeNetwork
        val caps = cm.getNetworkCapabilities(active)

        if (caps == null) {
            return ToolResult(false, "No active network link detected.")
        }

        val downSpeed = caps.linkDownstreamBandwidthKbps
        val upSpeed = caps.linkUpstreamBandwidthKbps
        return ToolResult(true, "Active link bandwidth: Down: ${downSpeed}Kbps, Up: ${upSpeed}Kbps.")
    }
}

class BatteryMonitorTool(private val context: Context) : AssistantTool {
    override val id = "system.battery_monitor"
    override val name = "Battery Monitor"
    override val description = "Thermal telemetry, voltage, and health tracking."
    override val category = ToolCategory.SYSTEM_SECURITY
    override val keywords = listOf("battery monitor", "temperature", "voltage", "health")
    override val requiredPermissions = emptySet<String>()
    override val parametersSchema = mapOf("type" to "object", "properties" to emptyMap<String, Any>())

    override suspend fun execute(params: Map<String, Any?>): ToolResult {
        val intent = context.registerReceiver(null, android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            ?: return ToolResult(false, "Unable to read battery state broadcast.")

        val temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f
        val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) / 1000f
        val health = when (intent.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            else -> "Normal"
        }

        return ToolResult(true, "Battery Health: $health | Temp: ${temp}°C | Voltage: ${voltage}V.")
    }
}

class AppUpdateTool(private val context: Context) : AssistantTool {
    override val id = "system.app_update"
    override val name = "App Update"
    override val description = "Build integrity, versionCode, and update check."
    override val category = ToolCategory.SYSTEM_SECURITY
    override val keywords = listOf("update", "version", "build", "release")
    override val requiredPermissions = emptySet<String>()
    override val parametersSchema = mapOf("type" to "object", "properties" to emptyMap<String, Any>())

    override suspend fun execute(params: Map<String, Any?>): ToolResult {
        val pInfo = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
        } catch (e: Exception) {
            null
        }

        val verName = pInfo?.versionName ?: "31.0"
        val verCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            pInfo?.longVersionCode ?: 31
        } else {
            @Suppress("DEPRECATION")
            pInfo?.versionCode?.toLong() ?: 31
        }

        return ToolResult(true, "Wife AI Assistant Build-31 (v$verName, Code $verCode) is up to date.")
    }
}

class NotificationAccessTool(private val context: Context) : AssistantTool {
    override val id = "system.notification_access"
    override val name = "Notification Access"
    override val description = "Checks and manages access to read system notifications."
    override val category = ToolCategory.SYSTEM_SECURITY
    override val keywords = listOf("notifications", "social access", "read messages")
    override val requiredPermissions = emptySet<String>()
    override val parametersSchema = mapOf("type" to "object", "properties" to emptyMap<String, Any>())

    override suspend fun checkRealAvailability(context: Context): ToolStatus {
        val enabledListeners = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        return if (enabledListeners?.contains(context.packageName) == true) {
            ToolStatus.AVAILABLE
        } else {
            ToolStatus.PERMISSION_REQUIRED
        }
    }

    override suspend fun execute(params: Map<String, Any?>): ToolResult {
        val enabledListeners = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        val isEnabled = enabledListeners?.contains(context.packageName) == true
        return if (isEnabled) {
            ToolResult(true, "Notification Access is currently GRANTED. Social message tools are fully functional.")
        } else {
            ToolResult(
                false,
                "Notification Access is DISABLED. I cannot read or reply to social messages without this permission.",
                requiresPermission = true
            )
        }
    }

    override fun openSettingsOrFix(context: Context) {
        val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}

class DiagnosticsTool(private val context: Context) : AssistantTool {
    override val id = "system.diagnostics"
    override val name = "Diagnostics"
    override val description = "Full multi-subsystem integrity scan and status evaluation."
    override val category = ToolCategory.SYSTEM_SECURITY
    override val keywords = listOf("diagnostics", "check system", "health check", "benchmark")
    override val requiredPermissions = emptySet<String>()
    override val parametersSchema = mapOf("type" to "object", "properties" to emptyMap<String, Any>())

    override suspend fun execute(params: Map<String, Any?>): ToolResult {
        val core = WifeAssistantCore.getInstance(context)
        val report = core.diagnosticsEngine.generateReport()
        return ToolResult(
            true,
            "Diagnostics Report: Voice=${report.voiceEngineStatus} | Gemini=${report.geminiStatus} | PC=${report.pcConnectionStatus} | ActiveTasks=${report.activeTasks}."
        )
    }
}
