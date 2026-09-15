import re

with open('app/src/main/java/com/example/v2/ui/settings/WifeAssistantV2Settings.kt', 'r') as f:
    content = f.read()

# Add imports
imports_to_add = """
import android.content.Intent
import android.net.Uri
import android.provider.Settings as AndroidSettings
import com.example.hologram.HologramBubbleService
import com.example.hologram.WifeServiceManager
import com.example.hologram.WifeServiceState
"""
content = re.sub(r'import com\.example\.v2\.voice\.VoiceViewModel', r'import com.example.v2.voice.VoiceViewModel' + imports_to_add, content)

# Remove var floatingHologram
content = re.sub(r'\s*var floatingHologram by remember \{ mutableStateOf\(prefs\.getBoolean\("floating_hologram", false\)\) \}', '', content)

# Replace the GlassSwitchRow with Background Service Controls
bg_service_ui = """
                    // Background Service Controls
                    val serviceState by WifeServiceManager.serviceState.collectAsState()
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(GlassSurface)
                            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Background Service",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        val (statusText, statusColor) = when (serviceState) {
                            WifeServiceState.SERVICE_STOPPED -> "○ Stopped" to Color.Gray
                            WifeServiceState.SERVICE_STARTING -> "● Starting..." to Violet
                            WifeServiceState.SERVICE_RUNNING -> "● Running" to Cyan
                            WifeServiceState.SERVICE_ERROR -> "● Error" to NeonPink
                            WifeServiceState.OVERLAY_PERMISSION_REQUIRED -> "● Overlay Permission Required" to NeonPink
                            WifeServiceState.SERVICE_UNAVAILABLE -> "○ Unavailable" to Color.Gray
                        }
                        
                        Text(
                            text = statusText,
                            color = statusColor,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                        )
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    if (serviceState == WifeServiceState.OVERLAY_PERMISSION_REQUIRED) {
                                        val intent = Intent(
                                            AndroidSettings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                            Uri.parse("package:${context.packageName}")
                                        )
                                        context.startActivity(intent)
                                    } else {
                                        val intent = Intent(context, HologramBubbleService::class.java)
                                        context.startService(intent)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Cyan)
                            ) {
                                Text(if (serviceState == WifeServiceState.OVERLAY_PERMISSION_REQUIRED) "Grant Overlay" else "Start Service", color = DarkMidnightBlue)
                            }
                            
                            Button(
                                onClick = {
                                    val intent = Intent(context, HologramBubbleService::class.java)
                                    context.stopService(intent)
                                    WifeServiceManager.updateState(WifeServiceState.SERVICE_STOPPED)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = GlassBorder)
                            ) {
                                Text("Stop Service", color = Color.White)
                            }
                        }
                    }
"""

content = re.sub(
    r'\s*GlassSwitchRow\(\s*title = "Floating Hologram Ball",\s*subtitle = "Always-on floating cute bubble on screen",\s*checked = floatingHologram,\s*onCheckedChange = \{ floatingHologram = it; saveBoolean\("floating_hologram", it\) \}\s*\)',
    bg_service_ui,
    content
)

with open('app/src/main/java/com/example/v2/ui/settings/WifeAssistantV2Settings.kt', 'w') as f:
    f.write(content)
