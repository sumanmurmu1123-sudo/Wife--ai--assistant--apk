package com.example.v2.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.v2.core.StateManager
import com.example.v2.ui.theme.Cyan
import com.example.v2.ui.theme.DarkMidnightBlue
import com.example.v2.ui.theme.NeonPink

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompatibilityDiagnosticsScreen(onBack: () -> Unit) {
    val state by StateManager.state.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Compatibility v4.05", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkMidnightBlue)
            )
        },
        containerColor = DarkMidnightBlue
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Device Identity",
                    color = Cyan,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                DiagnosticItem("Manufacturer", state.manufacturer, Icons.Default.Info)
                DiagnosticItem("Model", state.model, Icons.Default.Info)
                DiagnosticItem("Android Version", "API ${state.androidVersion}", Icons.Default.Info)
                DiagnosticItem("RAM Class", if (state.isLowRamDevice) "Low RAM" else "Standard", Icons.Default.Info)
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Text(
                    "Hardware Capabilities",
                    color = Cyan,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(getHardwareItems(state)) { item ->
                DiagnosticItem(item.label, item.status, item.icon, item.isSupported)
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "OEM / Background Optimization",
                    color = Cyan,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Manufacturers like ${state.manufacturer} may restrict background operation.",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "If voice disconnects in background, please disable 'Battery Optimization' for Wife AI in system settings.",
                            color = NeonPink,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DiagnosticItem(label: String, value: String, icon: ImageVector, isSuccess: Boolean = true) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (isSuccess) Cyan else NeonPink,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, color = Color.Gray, fontSize = 12.sp)
            Text(value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
        Spacer(modifier = Modifier.weight(1f))
        if (isSuccess) {
            Icon(Icons.Default.CheckCircle, null, tint = Cyan.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
        } else {
            Icon(Icons.Default.Error, null, tint = NeonPink, modifier = Modifier.size(16.dp))
        }
    }
}

data class DiagItem(val label: String, val status: String, val icon: ImageVector, val isSupported: Boolean)

private fun getHardwareItems(state: com.example.v2.core.AssistantState): List<DiagItem> {
    return listOf(
        DiagItem("Microphone", if (state.hasMicrophone) "Available" else "NOT FOUND", Icons.Default.CheckCircle, state.hasMicrophone),
        DiagItem("Bluetooth", if (state.hasBluetooth) "Supported" else "UNAVAILABLE", Icons.Default.CheckCircle, state.hasBluetooth),
        DiagItem("GPS / Location", if (state.hasGps) "Supported" else "UNAVAILABLE", Icons.Default.CheckCircle, state.hasGps),
        DiagItem("Camera", if (state.hasCamera) "Supported" else "UNAVAILABLE", Icons.Default.CheckCircle, state.hasCamera),
        DiagItem("Biometrics", if (state.hasBiometrics) "Supported" else "UNAVAILABLE", Icons.Default.CheckCircle, state.hasBiometrics),
        DiagItem("Telephony", if (state.hasTelephony) "Supported" else "UNAVAILABLE", Icons.Default.CheckCircle, state.hasTelephony),
        DiagItem("Overlay (Window)", if (state.overlayPermissionGranted) "Granted" else "DENIED", Icons.Default.CheckCircle, state.overlayPermissionGranted)
    )
}
