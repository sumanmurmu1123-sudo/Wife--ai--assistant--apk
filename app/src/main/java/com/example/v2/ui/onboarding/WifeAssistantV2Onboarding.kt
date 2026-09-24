package com.example.v2.ui.onboarding

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.v2.ui.theme.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun WifeAssistantV2Onboarding(
    onPermissionsGranted: () -> Unit,
    onBeforePermissionRequest: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentStep by remember { mutableIntStateOf(0) }
    
    val permissionsList = mutableListOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.CALL_PHONE
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val permissionsState = rememberMultiplePermissionsState(permissions = permissionsList)
    
    val hasOverlay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Settings.canDrawOverlays(context) else true
    
    val isAccessibilityEnabled = remember(currentStep) {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as android.view.accessibility.AccessibilityManager
        val enabledServices = am.getEnabledAccessibilityServiceList(android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        enabledServices.any { it.resolveInfo.serviceInfo.packageName == context.packageName }
    }

    val isBatteryOptimizationIgnored = remember(currentStep) {
        val pm = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pm.isIgnoringBatteryOptimizations(context.packageName)
        } else true
    }

    val isNotificationListenerEnabled = remember(currentStep) {
        val enabledListeners = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        enabledListeners?.contains(context.packageName) == true
    }

    // Auto-advance if already done
    LaunchedEffect(currentStep, permissionsState.allPermissionsGranted, hasOverlay, isAccessibilityEnabled, isBatteryOptimizationIgnored, isNotificationListenerEnabled) {
        if (currentStep == 0 && permissionsState.allPermissionsGranted && hasOverlay && isAccessibilityEnabled && isBatteryOptimizationIgnored && isNotificationListenerEnabled) {
            onPermissionsGranted()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkMidnightBlue),
        contentAlignment = Alignment.Center
    ) {
        // Gradient Glow
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Violet.copy(alpha = 0.1f), DarkMidnightBlue))
            )
        )

        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                fadeIn() + slideInHorizontally { it } togetherWith fadeOut() + slideOutHorizontally { -it }
            },
            label = "OnboardingStep"
        ) { step ->
            when (step) {
                0 -> WelcomeStep(onNext = { currentStep = 1 })
                1 -> PermissionsStep(
                    permissionsState = permissionsState,
                    onNext = { 
                        if (permissionsState.allPermissionsGranted) {
                            currentStep = 2
                        }
                    }
                )
                2 -> SpecialAccessStep(
                    hasOverlay = hasOverlay,
                    isAccessibilityEnabled = isAccessibilityEnabled,
                    isBatteryOptimizationIgnored = isBatteryOptimizationIgnored,
                    isNotificationListenerEnabled = isNotificationListenerEnabled,
                    onNext = { onPermissionsGranted() }
                )
            }
        }
        
        // Step Indicator
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .size(if (currentStep == index) 12.dp else 8.dp)
                        .clip(CircleShape)
                        .background(if (currentStep == index) Cyan else Color.White.copy(alpha = 0.3f))
                )
            }
        }
    }
}

@Composable
fun WelcomeStep(onNext: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Cyan.copy(alpha = 0.1f))
                .border(2.dp, Cyan, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("💍", fontSize = 64.sp)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Meet your Wife AI",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "I'm not just an assistant. I'm your digital companion. Let me handle your calls, messages, and automation while we talk.",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Cyan),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text("Get Started, Handsome", color = DarkMidnightBlue, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = DarkMidnightBlue)
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionsStep(
    permissionsState: com.google.accompanist.permissions.MultiplePermissionsState,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Grant Permissions",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        PermissionRequirementItem("🎙️", "Microphone", "So I can hear your voice and talk back.", permissionsState.allPermissionsGranted)
        PermissionRequirementItem("📞", "Phone & Contacts", "To make calls and identify who's calling.", permissionsState.allPermissionsGranted)
        PermissionRequirementItem("🔔", "Notifications", "To alert you when you have messages.", permissionsState.allPermissionsGranted)
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = {
                if (permissionsState.allPermissionsGranted) {
                    onNext()
                } else {
                    permissionsState.launchMultiplePermissionRequest()
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (permissionsState.allPermissionsGranted) Color(0xFF00FF88) else Cyan
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            val text = if (permissionsState.allPermissionsGranted) "Next Step" else "Grant Access"
            Text(text, color = DarkMidnightBlue, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SpecialAccessStep(
    hasOverlay: Boolean,
    isAccessibilityEnabled: Boolean,
    isBatteryOptimizationIgnored: Boolean,
    isNotificationListenerEnabled: Boolean,
    onNext: () -> Unit
) {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Special Access",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        PermissionRequirementItem("✨", "Overlay Permission", "Allows me to appear as a floating orb.", hasOverlay)
        PermissionRequirementItem("🤖", "Accessibility Service", "Enables phone automation tasks.", isAccessibilityEnabled)
        PermissionRequirementItem("🔋", "Battery Optimization", "Prevents system from killing me.", isBatteryOptimizationIgnored)
        PermissionRequirementItem("👂", "Notification Listener", "Allows me to read incoming messages.", isNotificationListenerEnabled)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = {
                when {
                    !hasOverlay -> {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        )
                        context.startActivity(intent)
                    }
                    !isAccessibilityEnabled -> {
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    }
                    !isBatteryOptimizationIgnored -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                data = Uri.parse("package:${context.packageName}")
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Fallback to settings
                                val settingsIntent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                context.startActivity(settingsIntent)
                            }
                        }
                    }
                    !isNotificationListenerEnabled -> {
                        val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS").apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    }
                    else -> onNext()
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (hasOverlay && isAccessibilityEnabled && isBatteryOptimizationIgnored && isNotificationListenerEnabled) 
                    Color(0xFF00FF88) else Cyan
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            val text = when {
                !hasOverlay -> "Enable Floating Orb"
                !isAccessibilityEnabled -> "Enable Automation"
                !isBatteryOptimizationIgnored -> "Ignore Battery Limits"
                !isNotificationListenerEnabled -> "Enable Message Reading"
                else -> "Finish Setup"
            }
            Text(text, color = DarkMidnightBlue, fontWeight = FontWeight.Bold)
        }
        
        if (!hasOverlay || !isAccessibilityEnabled || !isBatteryOptimizationIgnored || !isNotificationListenerEnabled) {
            Text(
                text = "After enabling, please return to the app.",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@Composable
fun PermissionRequirementItem(icon: String, title: String, description: String, isGranted: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isGranted) Color(0xFF00FF88).copy(alpha = 0.1f) else Color.White.copy(alpha = 0.05f))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 24.sp)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(description, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
        }
        if (isGranted) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF00FF88), modifier = Modifier.size(20.dp))
        }
    }
}
