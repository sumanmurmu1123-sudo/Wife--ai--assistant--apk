package com.example.v2.core.tools.impl

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.example.service.WifeAccessibilityService
import com.example.v2.core.tools.AssistantTool
import com.example.v2.core.tools.ToolCategory
import com.example.v2.core.tools.ToolResult
import com.example.v2.core.tools.ToolStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AccessibilityTool(private val context: Context) : AssistantTool {
    override val id = "automation.accessibility"
    override val name = "Accessibility"
    override val description = "Device accessibility automation service controller."
    override val category = ToolCategory.AUTOMATION
    override val keywords = listOf("accessibility", "automation", "gestures", "service")
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
            ToolResult(true, "Wife AI Accessibility Service is connected and active.")
        } else {
            ToolResult(
                false,
                "Accessibility Service is currently disabled in Android Settings.",
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

class TapTextTool(private val context: Context) : AssistantTool {
    override val id = "automation.tap_text"
    override val name = "Tap Text"
    override val description = "Locates and clicks on-screen text elements."
    override val category = ToolCategory.AUTOMATION
    override val keywords = listOf("tap text", "click text", "press text", "select")
    override val requiredPermissions = emptySet<String>()
    override val parametersSchema = mapOf(
        "type" to "object",
        "properties" to mapOf(
            "text" to mapOf("type" to "string", "description" to "The on-screen text to click")
        ),
        "required" to listOf("text")
    )

    override suspend fun checkRealAvailability(context: Context): ToolStatus {
        return if (WifeAccessibilityService.instance != null) ToolStatus.AVAILABLE else ToolStatus.DISABLED
    }

    override suspend fun execute(params: Map<String, Any?>): ToolResult = withContext(Dispatchers.Main) {
        val service = WifeAccessibilityService.instance
            ?: return@withContext ToolResult(false, "Accessibility Service is not enabled.", requiresPermission = true)

        val targetText = params["text"] as? String ?: "OK"
        val clicked = service.tapText(targetText)

        if (clicked) {
            ToolResult(true, "Successfully located and clicked element matching '$targetText'.")
        } else {
            ToolResult(false, "Could not find clickable on-screen element with text '$targetText'.")
        }
    }
}

class TapCoordinateTool(private val context: Context) : AssistantTool {
    override val id = "automation.tap_coordinate"
    override val name = "Tap Coordinate"
    override val description = "Dispatches a simulated touch tap at specific screen coordinates."
    override val category = ToolCategory.AUTOMATION
    override val keywords = listOf("tap", "click", "touch", "coordinate", "press")
    override val requiredPermissions = emptySet<String>()
    override val parametersSchema = mapOf(
        "type" to "object",
        "properties" to mapOf(
            "x" to mapOf("type" to "number", "description" to "X screen coordinate"),
            "y" to mapOf("type" to "number", "description" to "Y screen coordinate")
        ),
        "required" to listOf("x", "y")
    )

    override suspend fun checkRealAvailability(context: Context): ToolStatus {
        return if (WifeAccessibilityService.instance != null) ToolStatus.AVAILABLE else ToolStatus.DISABLED
    }

    override suspend fun execute(params: Map<String, Any?>): ToolResult {
        val service = WifeAccessibilityService.instance
            ?: return ToolResult(false, "Accessibility Service is not enabled.", requiresPermission = true)

        val x = (params["x"] as? Number)?.toFloat() ?: 500f
        val y = (params["y"] as? Number)?.toFloat() ?: 1000f

        val dispatched = service.tapCoordinate(x, y)
        return if (dispatched) {
            ToolResult(true, "Dispatched tap gesture at coordinates ($x, $y).")
        } else {
            ToolResult(false, "Failed to dispatch gesture at coordinates ($x, $y).")
        }
    }
}

class SwipeTool(private val context: Context) : AssistantTool {
    override val id = "automation.swipe"
    override val name = "Swipe"
    override val description = "Performs directional screen swipes."
    override val category = ToolCategory.AUTOMATION
    override val keywords = listOf("swipe", "drag", "slide", "gesture")
    override val requiredPermissions = emptySet<String>()
    override val parametersSchema = mapOf(
        "type" to "object",
        "properties" to mapOf(
            "direction" to mapOf("type" to "string", "description" to "UP, DOWN, LEFT, or RIGHT")
        )
    )

    override suspend fun checkRealAvailability(context: Context): ToolStatus {
        return if (WifeAccessibilityService.instance != null) ToolStatus.AVAILABLE else ToolStatus.DISABLED
    }

    override suspend fun execute(params: Map<String, Any?>): ToolResult {
        val service = WifeAccessibilityService.instance
            ?: return ToolResult(false, "Accessibility Service is not enabled.", requiresPermission = true)

        val direction = (params["direction"] as? String)?.uppercase() ?: "UP"
        val (startX, startY, endX, endY) = when (direction) {
            "DOWN" -> listOf(500f, 400f, 500f, 1200f)
            "LEFT" -> listOf(800f, 800f, 200f, 800f)
            "RIGHT" -> listOf(200f, 800f, 800f, 800f)
            else -> listOf(500f, 1200f, 500f, 400f) // UP
        }

        val success = service.swipe(startX, startY, endX, endY)
        return if (success) {
            ToolResult(true, "Dispatched swipe $direction gesture.")
        } else {
            ToolResult(false, "Failed to dispatch swipe gesture.")
        }
    }
}

class ScrollTool(private val context: Context) : AssistantTool {
    override val id = "automation.scroll"
    override val name = "Scroll"
    override val description = "Scrolls active scrollable lists or pages."
    override val category = ToolCategory.AUTOMATION
    override val keywords = listOf("scroll", "page down", "page up", "feed")
    override val requiredPermissions = emptySet<String>()
    override val parametersSchema = mapOf(
        "type" to "object",
        "properties" to mapOf(
            "forward" to mapOf("type" to "boolean", "description" to "Scroll forward if true, backward if false")
        )
    )

    override suspend fun checkRealAvailability(context: Context): ToolStatus {
        return if (WifeAccessibilityService.instance != null) ToolStatus.AVAILABLE else ToolStatus.DISABLED
    }

    override suspend fun execute(params: Map<String, Any?>): ToolResult {
        val service = WifeAccessibilityService.instance
            ?: return ToolResult(false, "Accessibility Service is not enabled.", requiresPermission = true)

        val forward = params["forward"] as? Boolean ?: true
        val scrolled = service.scroll(forward)
        return if (scrolled) {
            ToolResult(true, "Scrolled container ${if (forward) "forward" else "backward"}.")
        } else {
            ToolResult(false, "No active scrollable container found in foreground window.")
        }
    }
}

class TypeTextTool(private val context: Context) : AssistantTool {
    override val id = "automation.type_text"
    override val name = "Type Text"
    override val description = "Injects text into focused input fields."
    override val category = ToolCategory.AUTOMATION
    override val keywords = listOf("type", "input", "keyboard", "text field", "write")
    override val requiredPermissions = emptySet<String>()
    override val parametersSchema = mapOf(
        "type" to "object",
        "properties" to mapOf(
            "text" to mapOf("type" to "string", "description" to "Text to type into focused input")
        ),
        "required" to listOf("text")
    )

    override suspend fun checkRealAvailability(context: Context): ToolStatus {
        return if (WifeAccessibilityService.instance != null) ToolStatus.AVAILABLE else ToolStatus.DISABLED
    }

    override suspend fun execute(params: Map<String, Any?>): ToolResult = withContext(Dispatchers.Main) {
        val service = WifeAccessibilityService.instance
            ?: return@withContext ToolResult(false, "Accessibility Service is not enabled.", requiresPermission = true)

        val text = params["text"] as? String ?: "Wife AI"
        val typed = service.typeText(text)
        if (typed) {
            ToolResult(true, "Injected text '$text' into focused input field.")
        } else {
            ToolResult(false, "No focused editable input field found.")
        }
    }
}

class OpenAppTool(private val context: Context) : AssistantTool {
    override val id = "automation.open_app"
    override val name = "Open App"
    override val description = "Launches any installed application by name or package."
    override val category = ToolCategory.AUTOMATION
    override val keywords = listOf("open app", "launch app", "start app")
    override val requiredPermissions = emptySet<String>()
    override val parametersSchema = mapOf(
        "type" to "object",
        "properties" to mapOf(
            "app_name" to mapOf("type" to "string", "description" to "Name or package of the app to launch")
        ),
        "required" to listOf("app_name")
    )

    override suspend fun execute(params: Map<String, Any?>): ToolResult = withContext(Dispatchers.Main) {
        val appName = params["app_name"] as? String ?: "settings"
        val pm = context.packageManager

        // Check if exact package
        var launchIntent = pm.getLaunchIntentForPackage(appName)
        var resolvedName = appName

        if (launchIntent == null) {
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val apps = pm.queryIntentActivities(mainIntent, 0)
            val match = apps.firstOrNull {
                val label = it.loadLabel(pm).toString()
                label.contains(appName, ignoreCase = true) || it.activityInfo.packageName.contains(appName, ignoreCase = true)
            }
            if (match != null) {
                launchIntent = pm.getLaunchIntentForPackage(match.activityInfo.packageName)
                resolvedName = match.loadLabel(pm).toString()
            }
        }

        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            ToolResult(true, "Successfully launched $resolvedName.")
        } else {
            ToolResult(false, "App '$appName' is not installed on this device.")
        }
    }
}

class CloseAppTool(private val context: Context) : AssistantTool {
    override val id = "automation.close_app"
    override val name = "Close App"
    override val description = "Closes current app and returns to home."
    override val category = ToolCategory.AUTOMATION
    override val keywords = listOf("close app", "exit app", "quit", "dismiss")
    override val requiredPermissions = emptySet<String>()
    override val parametersSchema = mapOf("type" to "object", "properties" to emptyMap<String, Any>())

    override suspend fun execute(params: Map<String, Any?>): ToolResult {
        val service = WifeAccessibilityService.instance
        if (service != null) {
            service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
            service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
            return ToolResult(true, "Closed foreground app and navigated home.")
        }

        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(homeIntent)
        return ToolResult(true, "Navigated to home screen.")
    }
}
