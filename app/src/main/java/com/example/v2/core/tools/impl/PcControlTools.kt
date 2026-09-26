package com.example.v2.core.tools.impl

import android.content.Context
import com.example.sync.MayaPcSyncClient
import com.example.v2.core.PcConnectionState
import com.example.v2.core.MayaAssistantCore
import com.example.v2.core.tools.AssistantTool
import com.example.v2.core.tools.ToolCategory
import com.example.v2.core.tools.ToolResult
import com.example.v2.core.tools.ToolStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

class PcConnectTool(private val context: Context) : AssistantTool {
    override val id = "pc.connect"
    override val name = "PC Connect"
    override val description = "Establishes socket synchronization with the desktop companion server."
    override val category = ToolCategory.PC_CONTROL
    override val keywords = listOf("pc", "connect", "desktop", "sync", "computer")
    override val requiredPermissions = emptySet<String>()
    override val parametersSchema = mapOf(
        "type" to "object",
        "properties" to mapOf(
            "ip" to mapOf("type" to "string", "description" to "PC local IP address")
        )
    )

    private fun getTargetIp(params: Map<String, Any?>): String {
        val paramIp = params["ip"] as? String
        if (!paramIp.isNullOrBlank()) return paramIp
        val prefs = context.getSharedPreferences("maya_prefs", Context.MODE_PRIVATE)
        return prefs.getString("pc_ip", "192.168.1.100") ?: "192.168.1.100"
    }

    override suspend fun checkRealAvailability(context: Context): ToolStatus {
        val core = MayaAssistantCore.getInstance(context)
        return if (core.pcEngine.connectionState.value == PcConnectionState.CONNECTED) {
            ToolStatus.AVAILABLE
        } else {
            ToolStatus.UNAVAILABLE
        }
    }

    override suspend fun execute(params: Map<String, Any?>): ToolResult = withContext(Dispatchers.IO) {
        val ip = getTargetIp(params)
        val core = MayaAssistantCore.getInstance(context)
        core.pcEngine.connect(ip)
        ToolResult(true, "Initiated connection to desktop companion at $ip:8765. Check status in settings.")
    }
}

class PcCommandTool(private val context: Context) : AssistantTool {
    constructor(core: MayaAssistantCore) : this(core.context)
    override val id = "pc.command"
    override val name = "PC Command"
    override val description = "Dispatches an authorized command to the linked PC."
    override val category = ToolCategory.PC_CONTROL
    override val keywords = listOf("pc command", "execute pc", "desktop run")
    override val requiredPermissions = emptySet<String>()
    override val parametersSchema = mapOf(
        "type" to "object",
        "properties" to mapOf(
            "command" to mapOf("type" to "string", "description" to "Command keyword (e.g. lock, sleep)")
        ),
        "required" to listOf("command")
    )

    override suspend fun checkRealAvailability(context: Context): ToolStatus {
        val core = MayaAssistantCore.getInstance(context)
        return if (core.pcEngine.connectionState.value == PcConnectionState.CONNECTED) {
            ToolStatus.AVAILABLE
        } else {
            ToolStatus.UNAVAILABLE
        }
    }

    override suspend fun execute(params: Map<String, Any?>): ToolResult = withContext(Dispatchers.IO) {
        val cmd = params["command"] as? String ?: "ping"
        val core = MayaAssistantCore.getInstance(context)
        val success = core.pcEngine.executeCommand(cmd)
        if (success) {
            ToolResult(true, "PC command '$cmd' dispatched and acknowledged.")
        } else {
            ToolResult(false, "Failed to send command: PC is disconnected.")
        }
    }
}

class PcAppLaunchTool(private val context: Context) : AssistantTool {
    override val id = "pc.app_launch"
    override val name = "PC App Launch"
    override val description = "Launches applications (e.g., Chrome, VS Code) on the connected PC."
    override val category = ToolCategory.PC_CONTROL
    override val keywords = listOf("pc app", "launch on pc", "open computer app")
    override val requiredPermissions = emptySet<String>()
    override val parametersSchema = mapOf(
        "type" to "object",
        "properties" to mapOf(
            "app" to mapOf("type" to "string", "description" to "Application name to launch on PC")
        ),
        "required" to listOf("app")
    )

    override suspend fun execute(params: Map<String, Any?>): ToolResult = withContext(Dispatchers.IO) {
        val app = params["app"] as? String ?: "browser"
        val core = MayaAssistantCore.getInstance(context)
        val success = core.pcEngine.executeCommand("LAUNCH_APP", mapOf("app" to app))
        if (success) {
            ToolResult(true, "Sent request to launch '$app' on PC.")
        } else {
            ToolResult(false, "Cannot launch app: PC is offline.")
        }
    }
}

class PcFileTool(private val context: Context) : AssistantTool {
    override val id = "pc.file"
    override val name = "PC File"
    override val description = "Queries or opens files and directories on the desktop filesystem."
    override val category = ToolCategory.PC_CONTROL
    override val keywords = listOf("pc file", "desktop files", "open file on pc")
    override val requiredPermissions = emptySet<String>()
    override val parametersSchema = mapOf(
        "type" to "object",
        "properties" to mapOf(
            "path" to mapOf("type" to "string", "description" to "Path or file name on PC")
        )
    )

    override suspend fun execute(params: Map<String, Any?>): ToolResult = withContext(Dispatchers.IO) {
        val path = params["path"] as? String ?: "Downloads"
        val core = MayaAssistantCore.getInstance(context)
        val success = core.pcEngine.executeCommand("FILE_QUERY", mapOf("path" to path))
        if (success) {
            ToolResult(true, "Queried desktop path '$path'.")
        } else {
            ToolResult(false, "Failed to query PC filesystem: PC unreachable.")
        }
    }
}

class PcMediaTool(private val context: Context) : AssistantTool {
    override val id = "pc.media"
    override val name = "PC Media"
    override val description = "Controls desktop media playback (play/pause, volume, tracks)."
    override val category = ToolCategory.PC_CONTROL
    override val keywords = listOf("pc media", "computer music", "pause pc", "play pc")
    override val requiredPermissions = emptySet<String>()
    override val parametersSchema = mapOf(
        "type" to "object",
        "properties" to mapOf(
            "action" to mapOf("type" to "string", "description" to "PLAY, PAUSE, NEXT, PREV, MUTE")
        )
    )

    override suspend fun execute(params: Map<String, Any?>): ToolResult = withContext(Dispatchers.IO) {
        val action = params["action"] as? String ?: "PLAY_PAUSE"
        val core = MayaAssistantCore.getInstance(context)
        val success = core.pcEngine.executeCommand("MEDIA", mapOf("action" to action))
        if (success) {
            ToolResult(true, "Dispatched media control '$action' to PC.")
        } else {
            ToolResult(false, "PC media control failed: PC is disconnected.")
        }
    }
}

class PcShutdownTool(private val context: Context) : AssistantTool {
    override val id = "pc.shutdown"
    override val name = "PC Shutdown"
    override val description = "Initiates remote system shutdown or sleep on the linked PC."
    override val category = ToolCategory.PC_CONTROL
    override val keywords = listOf("shutdown pc", "turn off pc", "sleep pc")
    override val requiredPermissions = emptySet<String>()
    override val parametersSchema = mapOf(
        "type" to "object",
        "properties" to mapOf(
            "mode" to mapOf("type" to "string", "description" to "SHUTDOWN, RESTART, or SLEEP")
        )
    )

    override suspend fun execute(params: Map<String, Any?>): ToolResult = withContext(Dispatchers.IO) {
        val mode = params["mode"] as? String ?: "SLEEP"
        val core = MayaAssistantCore.getInstance(context)
        val success = core.pcEngine.executeCommand("POWER", mapOf("mode" to mode))
        if (success) {
            ToolResult(true, "Dispatched remote $mode signal to PC.")
        } else {
            ToolResult(false, "Remote shutdown failed: PC is not responding.")
        }
    }
}
