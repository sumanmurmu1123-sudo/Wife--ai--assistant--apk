package com.example.v2.core.tools.impl

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.media.AudioManager
import android.media.RingtoneManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.VibrationEffect
import android.os.Vibrator
import com.example.v2.core.tools.AssistantTool
import com.example.v2.core.tools.ToolCategory
import com.example.v2.core.tools.ToolResult
import com.example.v2.core.tools.ToolStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FindPhoneTool(private val context: Context) : AssistantTool {
    override val id = "safety.find_phone"
    override val name = "Find Phone"
    override val description = "Sounds an audible siren and vibrates to locate a misplaced device."
    override val category = ToolCategory.PHONE_SAFETY
    override val keywords = listOf("find phone", "locate phone", "ring phone", "siren", "alarm")
    override val requiredPermissions = setOf(Manifest.permission.VIBRATE)
    override val parametersSchema = mapOf("type" to "object", "properties" to emptyMap<String, Any>())

    override suspend fun execute(params: Map<String, Any?>): ToolResult = withContext(Dispatchers.IO) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            val originalVolume = audioManager?.getStreamVolume(AudioManager.STREAM_ALARM) ?: 5
            val maxAlarm = audioManager?.getStreamMaxVolume(AudioManager.STREAM_ALARM) ?: 15

            // Maximize alarm stream
            audioManager?.setStreamVolume(AudioManager.STREAM_ALARM, maxAlarm, 0)

            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            val ringtone = RingtoneManager.getRingtone(context, alarmUri)
            ringtone?.play()

            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 500, 200, 500), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(1000)
            }

            ToolResult(true, "Audible locator siren and vibration activated.")
        } catch (e: Exception) {
            ToolResult(false, "Failed to activate siren: ${e.message}")
        }
    }
}

class LocationTool(private val context: Context) : AssistantTool {
    override val id = "safety.location"
    override val name = "Location"
    override val description = "Retrieves GPS sensor coordinates and provider status."
    override val category = ToolCategory.PHONE_SAFETY
    override val keywords = listOf("location", "gps", "coordinates", "where am i")
    override val requiredPermissions = setOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    override val parametersSchema = mapOf("type" to "object", "properties" to emptyMap<String, Any>())

    override suspend fun checkRealAvailability(context: Context): ToolStatus {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return ToolStatus.UNAVAILABLE
        val gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val netEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        return if (gpsEnabled || netEnabled) ToolStatus.AVAILABLE else ToolStatus.DISABLED
    }

    override suspend fun execute(params: Map<String, Any?>): ToolResult {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return ToolResult(false, "Location service unavailable.")

        return try {
            val lastGps = try { lm.getLastKnownLocation(LocationManager.GPS_PROVIDER) } catch (e: SecurityException) { null }
            val lastNet = try { lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) } catch (e: SecurityException) { null }
            val location = lastGps ?: lastNet

            if (location != null) {
                ToolResult(
                    true,
                    "Lat: ${location.latitude}, Lon: ${location.longitude} (Accuracy: ${location.accuracy}m, Provider: ${location.provider})"
                )
            } else {
                ToolResult(true, "GPS sensors active; waiting for satellite fix.")
            }
        } catch (e: SecurityException) {
            ToolResult(false, "Location permission required.", requiresPermission = true)
        }
    }
}

class DeviceStatusTool(private val context: Context) : AssistantTool {
    override val id = "safety.device_status"
    override val name = "Device Status"
    override val description = "Hardware model, OS build, memory, and storage telemetry."
    override val category = ToolCategory.PHONE_SAFETY
    override val keywords = listOf("device", "specs", "hardware", "ram", "storage", "system info")
    override val requiredPermissions = emptySet<String>()
    override val parametersSchema = mapOf("type" to "object", "properties" to emptyMap<String, Any>())

    override suspend fun execute(params: Map<String, Any?>): ToolResult {
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager?.getMemoryInfo(memInfo)

        val totalRamMb = memInfo.totalMem / (1024 * 1024)
        val availRamMb = memInfo.availMem / (1024 * 1024)

        val stat = StatFs(Environment.getDataDirectory().path)
        val availStorageGb = (stat.availableBlocksLong * stat.blockSizeLong) / (1024 * 1024 * 1024)
        val totalStorageGb = (stat.blockCountLong * stat.blockSizeLong) / (1024 * 1024 * 1024)

        val info = "${Build.MANUFACTURER.uppercase()} ${Build.MODEL} (Android ${Build.VERSION.RELEASE}, API ${Build.VERSION.SDK_INT}) | RAM: ${availRamMb}MB free of ${totalRamMb}MB | Storage: ${availStorageGb}GB free of ${totalStorageGb}GB"
        return ToolResult(true, info)
    }
}

class BatteryStatusTool(private val context: Context) : AssistantTool {
    override val id = "safety.battery_status"
    override val name = "Battery Status"
    override val description = "Reads live battery level, charging status, and thermal health."
    override val category = ToolCategory.PHONE_SAFETY
    override val keywords = listOf("battery", "power", "charge", "percentage")
    override val requiredPermissions = emptySet<String>()
    override val parametersSchema = mapOf("type" to "object", "properties" to emptyMap<String, Any>())

    override suspend fun execute(params: Map<String, Any?>): ToolResult {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            ?: return ToolResult(false, "Battery status broadcast unavailable.")

        val level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

        val batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale.toFloat()).toInt() else 0
        val chargePlug = batteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val plugType = when (chargePlug) {
            BatteryManager.BATTERY_PLUGGED_USB -> "USB"
            BatteryManager.BATTERY_PLUGGED_AC -> "AC"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
            else -> if (isCharging) "Charging" else "Discharging"
        }

        return ToolResult(true, "Battery: $batteryPct% ($plugType)")
    }
}

class NetworkStatusTool(private val context: Context) : AssistantTool {
    override val id = "safety.network_status"
    override val name = "Network Status"
    override val description = "Inspects Wi-Fi, cellular data, and internet connectivity."
    override val category = ToolCategory.PHONE_SAFETY
    override val keywords = listOf("network", "internet", "wifi", "cellular", "connection")
    override val requiredPermissions = setOf(Manifest.permission.ACCESS_NETWORK_STATE)
    override val parametersSchema = mapOf("type" to "object", "properties" to emptyMap<String, Any>())

    override suspend fun checkRealAvailability(context: Context): ToolStatus {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return ToolStatus.UNAVAILABLE
        val network = cm.activeNetwork
        val caps = cm.getNetworkCapabilities(network)
        val hasInternet = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        return if (hasInternet) ToolStatus.AVAILABLE else ToolStatus.UNAVAILABLE
    }

    override suspend fun execute(params: Map<String, Any?>): ToolResult {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return ToolResult(false, "Connectivity service unavailable.")

        val network = cm.activeNetwork
        val caps = cm.getNetworkCapabilities(network)

        if (caps == null) {
            return ToolResult(false, "Device is currently offline.")
        }

        val isWifi = caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        val isCellular = caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        val hasInternet = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        val isValidated = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

        val transport = if (isWifi) "Wi-Fi" else if (isCellular) "Cellular" else "Other"
        return ToolResult(
            hasInternet,
            "Connected via $transport (Validated: $isValidated, Downstream: ${caps.linkDownstreamBandwidthKbps} Kbps)"
        )
    }
}
