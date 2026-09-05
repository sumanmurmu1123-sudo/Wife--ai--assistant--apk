package com.example.ui.marketing

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.AdCopyFramework
import com.example.domain.DigitalMarketingEngine
import com.example.domain.MarketingChannel

@Composable
fun DigitalMarketingSheet(
    engine: DigitalMarketingEngine,
    onVoiceDiscuss: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Ad Copy, 1: ROAS Calculator, 2: Viral Hooks
    var productName by remember { mutableStateOf("AI SaaS Product") }
    var targetAudience by remember { mutableStateOf("Freelancers & Agencies") }
    var selectedFramework by remember { mutableStateOf(AdCopyFramework.PAS) }
    var selectedChannel by remember { mutableStateOf(MarketingChannel.META_ADS) }

    var adSpendInput by remember { mutableStateOf("5000") }
    var revenueInput by remember { mutableStateOf("18500") }
    var conversionsInput by remember { mutableStateOf("15") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF080C14))
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
                        imageVector = Icons.Default.Campaign,
                        contentDescription = null,
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Digital Marketing & Growth Suite",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                }
            }

            Divider(color = Color(0xFF162238), modifier = Modifier.padding(bottom = 12.dp))

            // Navigation Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF0F1829),
                contentColor = Color(0xFF00E5FF),
                divider = {}
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                    Text("Ad Copy ✍️", modifier = Modifier.padding(10.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                    Text("ROAS / Metrics 📊", modifier = Modifier.padding(10.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                    Text("Viral Hooks 🪝", modifier = Modifier.padding(10.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (selectedTab) {
                    0 -> { // AD COPYWRITING STUDIO
                        item {
                            Text("CAMPAIGN PARAMETERS", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = productName,
                                onValueChange = { productName = it },
                                label = { Text("Product / Service Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = targetAudience,
                                onValueChange = { targetAudience = it },
                                label = { Text("Target Audience Niche") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text("COPYWRITING FRAMEWORK", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AdCopyFramework.entries.forEach { fw ->
                                    val isSelected = selectedFramework == fw
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isSelected) Color(0xFF00E5FF).copy(alpha = 0.2f) else Color(0xFF131D30))
                                            .border(1.dp, if (isSelected) Color(0xFF00E5FF) else Color(0xFF22324D), RoundedCornerShape(10.dp))
                                            .clickable { selectedFramework = fw }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(fw.name, color = if (isSelected) Color(0xFF00E5FF) else Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            val generatedCopy = engine.generateAdCopy(productName, targetAudience, selectedChannel, selectedFramework)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF0E1626)),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0E1626))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("GENERATED HIGH-CONVERTING AD COPY", color = Color(0xFF00E5FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(generatedCopy, color = Color.White, fontSize = 13.sp, lineHeight = 18.sp)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = { onVoiceDiscuss("Wife, refine this ad copy for $productName targeting $targetAudience!") },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.Black)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Refine with Wife AI", color = Color.Black, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    1 -> { // ROAS & METRICS CALCULATOR
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF0E1626)),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0E1626))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("PAID ADS PERFORMANCE CALCULATOR", color = Color(0xFF00E5FF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(10.dp))

                                    OutlinedTextField(
                                        value = adSpendInput,
                                        onValueChange = { adSpendInput = it },
                                        label = { Text("Total Ad Spend (₹)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = revenueInput,
                                        onValueChange = { revenueInput = it },
                                        label = { Text("Total Revenue (₹)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = conversionsInput,
                                        onValueChange = { conversionsInput = it },
                                        label = { Text("Total Conversions / Sales") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    val spend = adSpendInput.toDoubleOrNull() ?: 0.0
                                    val rev = revenueInput.toDoubleOrNull() ?: 0.0
                                    val conv = conversionsInput.toIntOrNull() ?: 0

                                    Text(
                                        text = engine.calculateRoas(spend, rev, conv),
                                        color = Color(0xFF00FF66),
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }

                    2 -> { // VIRAL HOOKS
                        val hooks = engine.getViralReelHooks("Content Creation & Business")
                        item {
                            Text("PROVEN 3-SECOND VIDEO HOOKS", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        items(hooks.size) { idx ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, Color(0xFF1E2D4A), RoundedCornerShape(12.dp))
                                    .background(Color(0xFF0E1626)),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0E1626))
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(hooks[idx], color = Color.White, fontSize = 13.sp, modifier = Modifier.weight(1f))
                                    IconButton(onClick = { onVoiceDiscuss("Let's write a 30-second script starting with: ${hooks[idx]}") }) {
                                        Icon(Icons.Default.RecordVoiceOver, contentDescription = "Use Hook", tint = Color(0xFF00E5FF))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
