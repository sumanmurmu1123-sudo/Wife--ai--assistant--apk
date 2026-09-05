package com.example.ui.security

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.CyberSecurityEngine
import com.example.domain.SecurityFramework

@Composable
fun CyberSecuritySheet(
    engine: CyberSecurityEngine,
    onVoiceConsult: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Frameworks, 1: Password Entropy, 2: Code Scanner
    var selectedFramework by remember { mutableStateOf(SecurityFramework.OWASP_TOP_10) }
    var passwordTestInput by remember { mutableStateOf("Tr0ng#P@ssw0rd2026!") }
    var codeScanInput by remember { mutableStateOf("val query = \"SELECT * FROM users WHERE user = '\" + username + \"'\"") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070A10))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = Color(0xFF00E676),
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cybersecurity & vCISO Center",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                }
            }

            HorizontalDivider(color = Color(0xFF14202C), modifier = Modifier.padding(bottom = 12.dp))

            // Navigation Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF0E1724),
                contentColor = Color(0xFF00E676),
                divider = {}
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                    Text("Frameworks 🛡️", modifier = Modifier.padding(10.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                    Text("Credentials 🔐", modifier = Modifier.padding(10.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                    Text("Code Audit 🔍", modifier = Modifier.padding(10.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (selectedTab) {
                    0 -> { // SECURITY FRAMEWORKS AUDIT
                        item {
                            Text("SELECT COMPLIANCE FRAMEWORK", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                SecurityFramework.values().forEach { fw ->
                                    val isSelected = selectedFramework == fw
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isSelected) Color(0xFF00E676).copy(alpha = 0.2f) else Color(0xFF101B2B))
                                            .border(1.dp, if (isSelected) Color(0xFF00E676) else Color(0xFF1D2E45), RoundedCornerShape(10.dp))
                                            .clickable { selectedFramework = fw }
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = fw.name,
                                            color = if (isSelected) Color(0xFF00E676) else Color.LightGray,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF0A121E)),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A121E))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = selectedFramework.displayName,
                                        color = Color(0xFF00E676),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(selectedFramework.focusArea, color = Color.Gray, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = engine.getFrameworkChecklist(selectedFramework),
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        lineHeight = 17.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }

                    1 -> { // PASSWORD & ENTROPY DIAGNOSTIC
                        item {
                            Text("CREDENTIAL ENTROPY CALCULATOR", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = passwordTestInput,
                                onValueChange = { passwordTestInput = it },
                                label = { Text("Test Password / Secret Token") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            val diag = engine.evaluateCredentialStrength(passwordTestInput)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF0A121E)),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A121E))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("STRENGTH RATING", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Text(diag.strengthLevel, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Entropy: ${"%.1f".format(diag.entropyBits)} bits", color = Color(0xFF00E676), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Text("Crack Time: ${diag.crackTimeEstimate}", color = Color.LightGray, fontSize = 12.sp)

                                    if (diag.recommendations.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Recommendations:", color = Color(0xFFFFB703), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        diag.recommendations.forEach { r ->
                                            Text("• $r", color = Color.LightGray, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    2 -> { // CODE VULNERABILITY AUDITOR
                        item {
                            Text("STATIC VULNERABILITY SCANNER", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = codeScanInput,
                                onValueChange = { codeScanInput = it },
                                label = { Text("Paste Code Snippet") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            val auditResult = engine.auditCodeSecurity(codeScanInput)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF0A121E)),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A121E))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("SCAN RESULTS", color = Color(0xFF00E676), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = auditResult,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        lineHeight = 17.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    onVoiceConsult("Wife, conduct a cybersecurity review and explain how to mitigate the top risks!")
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Consult with Wife vCISO", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}
