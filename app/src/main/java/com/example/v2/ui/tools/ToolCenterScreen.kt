package com.example.v2.ui.tools

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.service.WifeAccessibilityService
import com.example.v2.core.MayaAssistantCore
import com.example.v2.core.tools.AssistantTool
import com.example.v2.core.tools.ToolCategory
import com.example.v2.core.tools.ToolRuntimeState
import com.example.v2.core.tools.ToolStatus
import com.example.v2.ui.theme.*
import kotlinx.coroutines.launch

private val PRIMARY_CATEGORIES = listOf(
    ToolCategory.VOICE_AI,
    ToolCategory.ANDROID_CONTROL,
    ToolCategory.AUTOMATION,
    ToolCategory.PHONE_SAFETY,
    ToolCategory.PC_CONTROL,
    ToolCategory.MEMORY,
    ToolCategory.TASK_AUTOMATION,
    ToolCategory.INTERNET,
    ToolCategory.GOVERNMENT_JOBS,
    ToolCategory.MEDIA_CREATIVE,
    ToolCategory.SYSTEM_SECURITY
)

@Composable
fun ToolCenterScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val core = remember { MayaAssistantCore.getInstance(context) }
    val toolStateManager = core.toolStateManager
    val toolRegistry = core.toolRegistry

    // Observe real tool runtime states from ToolStateManager
    val runtimeStates by toolStateManager.toolStates.collectAsState()
    val allRegisteredTools by toolRegistry.registryState.collectAsState()
    val isAccessibilityActive by WifeAccessibilityService.isServiceActive.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<ToolCategory?>(null) }
    var selectedStatusFilter by remember { mutableStateOf<ToolStatus?>(null) }
    var inspectingTool by remember { mutableStateOf<AssistantTool?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }

    // Rotating animation for refresh icon
    val infiniteTransition = rememberInfiniteTransition(label = "refresh")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Filter tools
    val filteredTools = remember(allRegisteredTools, runtimeStates, searchQuery, selectedCategory, selectedStatusFilter) {
        allRegisteredTools.filter { tool ->
            val toolCat = tool.category.normalizedCategory
            val matchesCategory = selectedCategory == null || toolCat == selectedCategory?.normalizedCategory
            val matchesSearch = searchQuery.isBlank() ||
                    tool.name.contains(searchQuery, ignoreCase = true) ||
                    tool.description.contains(searchQuery, ignoreCase = true) ||
                    tool.id.contains(searchQuery, ignoreCase = true) ||
                    tool.keywords.any { it.contains(searchQuery, ignoreCase = true) }
            val state = runtimeStates[tool.id]?.status ?: ToolStatus.AVAILABLE
            val matchesStatus = selectedStatusFilter == null || state == selectedStatusFilter

            matchesCategory && matchesSearch && matchesStatus
        }
    }

    // Telemetry counts
    val totalCount = allRegisteredTools.size
    val availableCount = allRegisteredTools.count {
        runtimeStates[it.id]?.status == ToolStatus.AVAILABLE || runtimeStates[it.id]?.status == ToolStatus.SUCCESS
    }
    val permissionCount = allRegisteredTools.count {
        runtimeStates[it.id]?.status == ToolStatus.PERMISSION_REQUIRED
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkMidnightBlue)
            .testTag("tool_center_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            // 1. Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(GlassSurface)
                        .border(1.dp, GlassBorder, CircleShape)
                        .testTag("back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ALL TOOLS",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Cyan.copy(alpha = 0.2f))
                                .border(1.dp, Cyan.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "ENGINE v2",
                                color = Cyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = "Real hardware verification & live execution monitor",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                }

                // Refresh Status Button
                IconButton(
                    onClick = {
                        isRefreshing = true
                        coroutineScope.launch {
                            toolStateManager.refreshAllToolStates()
                            isRefreshing = false
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(GlassSurface)
                        .border(1.dp, GlassBorder, CircleShape)
                        .testTag("refresh_telemetry_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Health",
                        tint = Cyan,
                        modifier = if (isRefreshing) Modifier.rotate(rotation) else Modifier
                    )
                }
            }

            // 2. Telemetry Overview Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TelemetryCard(
                    title = "TOTAL",
                    value = "$totalCount",
                    accentColor = Cyan,
                    modifier = Modifier.weight(1f)
                )
                TelemetryCard(
                    title = "READY",
                    value = "$availableCount",
                    accentColor = Color(0xFF00FF88),
                    modifier = Modifier.weight(1f)
                )
                TelemetryCard(
                    title = "PERMS",
                    value = "$permissionCount",
                    accentColor = SoftGold,
                    modifier = Modifier.weight(1f)
                )
                TelemetryCard(
                    title = "SERVICE",
                    value = if (isAccessibilityActive) "ON" else "OFF",
                    accentColor = if (isAccessibilityActive) Color(0xFF00FF88) else Color(0xFFFF5252),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            val intent = android.content.Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        }
                )
            }

            // 3. Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = "Search 50+ tools by name, ID or action...",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = Cyan)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.White.copy(alpha = 0.6f))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Cyan,
                    unfocusedBorderColor = GlassBorder,
                    focusedContainerColor = GlassSurface,
                    unfocusedContainerColor = GlassSurface,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .testTag("tool_search_input")
            )

            // 4. Category Filter Chips (Horizontal Scroll)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    label = { Text("ALL (${allRegisteredTools.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Cyan,
                        selectedLabelColor = Color.Black,
                        containerColor = GlassSurface,
                        labelColor = Color.White.copy(alpha = 0.8f)
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedCategory == null,
                        borderColor = GlassBorder,
                        selectedBorderColor = Cyan
                    )
                )

                PRIMARY_CATEGORIES.forEach { category ->
                    val count = allRegisteredTools.count { it.category.normalizedCategory == category }
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = {
                            selectedCategory = if (selectedCategory == category) null else category
                        },
                        label = {
                            Text(
                                "${category.displayName.uppercase()} ($count)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Cyan,
                            selectedLabelColor = Color.Black,
                            containerColor = GlassSurface,
                            labelColor = Color.White.copy(alpha = 0.8f)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedCategory == category,
                            borderColor = GlassBorder,
                            selectedBorderColor = Cyan
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 5. Active Tools List
            if (filteredTools.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.3f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No tools matching filter",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredTools, key = { it.id }) { tool ->
                        val runtimeState = runtimeStates[tool.id] ?: ToolRuntimeState(
                            toolId = tool.id,
                            name = tool.name,
                            category = tool.category,
                            status = ToolStatus.AVAILABLE
                        )

                        ToolExecutionCard(
                            tool = tool,
                            runtimeState = runtimeState,
                            onExecute = {
                                coroutineScope.launch {
                                    toolStateManager.executeTool(tool.id, emptyMap())
                                }
                            },
                            onFix = {
                                tool.openSettingsOrFix(context)
                            },
                            onInspect = {
                                inspectingTool = tool
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        // Tool Inspection Dialog
        inspectingTool?.let { tool ->
            val runtimeState = runtimeStates[tool.id] ?: ToolRuntimeState(
                toolId = tool.id,
                name = tool.name,
                category = tool.category,
                status = ToolStatus.AVAILABLE
            )
            ToolInspectionDialog(
                tool = tool,
                runtimeState = runtimeState,
                onDismiss = { inspectingTool = null },
                onExecute = { params ->
                    coroutineScope.launch {
                        toolStateManager.executeTool(tool.id, params)
                    }
                },
                onFix = {
                    tool.openSettingsOrFix(context)
                }
            )
        }
    }
}

@Composable
fun TelemetryCard(
    title: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(GlassSurface)
            .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
            .padding(vertical = 8.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            color = accentColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
fun ToolExecutionCard(
    tool: AssistantTool,
    runtimeState: ToolRuntimeState,
    onExecute: () -> Unit,
    onFix: () -> Unit,
    onInspect: () -> Unit
) {
    val isRunning = runtimeState.status == ToolStatus.RUNNING

    val statusColor = when (runtimeState.status) {
        ToolStatus.AVAILABLE -> Color(0xFF00FF88)
        ToolStatus.RUNNING -> Cyan
        ToolStatus.SUCCESS -> Color(0xFF00FF88)
        ToolStatus.FAILED, ToolStatus.ERROR, ToolStatus.TIMEOUT -> Color(0xFFFF416C)
        ToolStatus.PERMISSION_REQUIRED -> SoftGold
        ToolStatus.DISABLED, ToolStatus.UNAVAILABLE, ToolStatus.CANCELLED -> Color(0xFF8E99AC)
    }

    val statusLabel = when (runtimeState.status) {
        ToolStatus.AVAILABLE -> "READY"
        ToolStatus.RUNNING -> "RUNNING..."
        ToolStatus.SUCCESS -> "SUCCESS"
        ToolStatus.FAILED -> "FAILED"
        ToolStatus.ERROR -> "ERROR"
        ToolStatus.TIMEOUT -> "TIMEOUT"
        ToolStatus.CANCELLED -> "CANCELLED"
        ToolStatus.PERMISSION_REQUIRED -> "PERMISSION"
        ToolStatus.DISABLED -> "DISABLED"
        ToolStatus.UNAVAILABLE -> "OFFLINE"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(GlassSurface)
            .border(
                width = if (isRunning) 1.5.dp else 1.dp,
                color = if (isRunning) Cyan else GlassBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(14.dp)
            .testTag("tool_card_${tool.id}")
    ) {
        // Top Row: Category pill & Status badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(getCategoryColor(tool.category).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCategoryIcon(tool.category),
                        contentDescription = null,
                        tint = getCategoryColor(tool.category),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = tool.name,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = tool.id,
                        color = Cyan.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Status Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(statusColor.copy(alpha = 0.15f))
                    .border(1.dp, statusColor.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = statusLabel,
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = tool.description,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp,
            lineHeight = 16.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        // Last execution result preview
        val resultMessage = runtimeState.lastResultMessage ?: runtimeState.lastErrorMessage
        val isSuccess = runtimeState.lastResultMessage != null && runtimeState.lastErrorMessage == null
        if (resultMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isSuccess) Color(0xFF00FF88).copy(alpha = 0.08f)
                        else Color(0xFFFF416C).copy(alpha = 0.08f)
                    )
                    .border(
                        1.dp,
                        if (isSuccess) Color(0xFF00FF88).copy(alpha = 0.2f)
                        else Color(0xFFFF416C).copy(alpha = 0.2f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                        contentDescription = null,
                        tint = if (isSuccess) Color(0xFF00FF88) else Color(0xFFFF416C),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = resultMessage,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Actions Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Test Run Button
            Button(
                onClick = onExecute,
                enabled = !isRunning,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Cyan,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .testTag("run_tool_${tool.id}")
            ) {
                if (isRunning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.Black,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("RUNNING", fontSize = 11.sp, fontWeight = FontWeight.Black)
                } else {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("EXECUTE", fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
            }

            // Fix Button if Permission or Disabled
            if (runtimeState.status == ToolStatus.PERMISSION_REQUIRED || runtimeState.status == ToolStatus.DISABLED) {
                OutlinedButton(
                    onClick = onFix,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = SoftGold
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.horizontalGradient(listOf(SoftGold, SoftGold))
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("fix_tool_${tool.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("FIX", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Inspect Button
            IconButton(
                onClick = onInspect,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(GlassSurface)
                    .border(1.dp, GlassBorder, RoundedCornerShape(10.dp))
                    .testTag("inspect_tool_${tool.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Code,
                    contentDescription = "Inspect Schema",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun ToolInspectionDialog(
    tool: AssistantTool,
    runtimeState: ToolRuntimeState,
    onDismiss: () -> Unit,
    onExecute: (Map<String, Any?>) -> Unit,
    onFix: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Cyan.copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("tool_inspection_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = tool.name,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = tool.id,
                            color = Cyan,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Metadata list
                DetailRow("Category", tool.category.displayName)
                DetailRow("Status", runtimeState.status.name)
                DetailRow("Success Count", "${runtimeState.successCount}")
                DetailRow("Failure Count", "${runtimeState.failureCount}")
                if (tool.requiredPermissions.isNotEmpty()) {
                    DetailRow("Required Perms", tool.requiredPermissions.joinToString(", ") { it.substringAfterLast('.') })
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "PARAMETERS SCHEMA",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF080C14))
                        .border(1.dp, GlassBorder, RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = tool.parametersSchema.toString(),
                        color = Cyan.copy(alpha = 0.9f),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                val resultMessage = runtimeState.lastResultMessage ?: runtimeState.lastErrorMessage
                val isSuccess = runtimeState.lastResultMessage != null && runtimeState.lastErrorMessage == null
                if (resultMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "LAST EXECUTION OUTPUT",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSuccess) Color(0xFF00FF88).copy(alpha = 0.1f) else Color(0xFFFF416C).copy(alpha = 0.1f))
                            .border(1.dp, if (isSuccess) Color(0xFF00FF88).copy(alpha = 0.3f) else Color(0xFFFF416C).copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = resultMessage,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { onExecute(emptyMap()) },
                        colors = ButtonDefaults.buttonColors(containerColor = Cyan, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("RUN TEST", fontWeight = FontWeight.Bold)
                    }

                    if (runtimeState.status == ToolStatus.PERMISSION_REQUIRED || runtimeState.status == ToolStatus.DISABLED) {
                        Button(
                            onClick = onFix,
                            colors = ButtonDefaults.buttonColors(containerColor = SoftGold, contentColor = Color.Black),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("SETTINGS", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
        Text(text = value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

fun getCategoryIcon(category: ToolCategory): ImageVector {
    return when (category.normalizedCategory) {
        ToolCategory.VOICE_AI -> Icons.Default.Mic
        ToolCategory.ANDROID_CONTROL -> Icons.Default.PhoneAndroid
        ToolCategory.AUTOMATION -> Icons.Default.TouchApp
        ToolCategory.PHONE_SAFETY -> Icons.Default.Shield
        ToolCategory.PC_CONTROL -> Icons.Default.Computer
        ToolCategory.MEMORY -> Icons.Default.Psychology
        ToolCategory.TASK_AUTOMATION -> Icons.Default.Schedule
        ToolCategory.INTERNET -> Icons.Default.Language
        ToolCategory.GOVERNMENT_JOBS -> Icons.Default.Work
        ToolCategory.MEDIA_CREATIVE -> Icons.Default.Movie
        ToolCategory.SYSTEM_SECURITY -> Icons.Default.Security
        else -> Icons.Default.Build
    }
}

fun getCategoryColor(category: ToolCategory): Color {
    return when (category.normalizedCategory) {
        ToolCategory.VOICE_AI -> Cyan
        ToolCategory.ANDROID_CONTROL -> Color(0xFF3B82F6)
        ToolCategory.AUTOMATION -> Color(0xFF9D00FF)
        ToolCategory.PHONE_SAFETY -> Color(0xFF00FF88)
        ToolCategory.PC_CONTROL -> Color(0xFF00E5FF)
        ToolCategory.MEMORY -> SoftGold
        ToolCategory.TASK_AUTOMATION -> Color(0xFFFF7A00)
        ToolCategory.INTERNET -> Color(0xFF00C853)
        ToolCategory.GOVERNMENT_JOBS -> Color(0xFFFFB300)
        ToolCategory.MEDIA_CREATIVE -> NeonPink
        ToolCategory.SYSTEM_SECURITY -> Color(0xFF64B5F6)
        else -> Cyan
    }
}
