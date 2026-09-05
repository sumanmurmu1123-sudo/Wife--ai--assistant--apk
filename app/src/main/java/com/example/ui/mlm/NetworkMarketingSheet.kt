package com.example.ui.mlm

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
import com.example.data.mlm.MarketType
import com.example.domain.NetworkMarketingEngine

@Composable
fun NetworkMarketingSheet(
    mlmEngine: NetworkMarketingEngine,
    onTriggerVoiceScript: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Scripts, 1 = Objections, 2 = Calculator
    var targetName by remember { mutableStateOf("Rahul") }
    var selectedMarket by remember { mutableStateOf(MarketType.HOT) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090A14))
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
                        imageVector = Icons.Default.Groups,
                        contentDescription = null,
                        tint = Color(0xFF00FF66),
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Direct Selling & MLM Partner",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                }
            }

            HorizontalDivider(color = Color(0xFF1E2638), modifier = Modifier.padding(bottom = 12.dp))

            // Navigation Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF121826),
                contentColor = Color(0xFF00FF66),
                divider = {}
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                    Text("Scripts \uD83D\uDCDC", modifier = Modifier.padding(10.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                    Text("Objections \uD83D\uDEE1\uFE0F", modifier = Modifier.padding(10.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                    Text("Commission \uD83D\uDCB0", modifier = Modifier.padding(10.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (selectedTab) {
                    0 -> { // SCRIPT GENERATOR
                        item {
                            Text("PROSPECT DETAILS", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = targetName,
                                onValueChange = { targetName = it },
                                label = { Text("Prospect Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MarketType.values().forEach { mType ->
                                    val isSelected = selectedMarket == mType
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isSelected) Color(0xFF00FF66).copy(alpha = 0.2f) else Color(0xFF161E2E))
                                            .border(1.dp, if (isSelected) Color(0xFF00FF66) else Color(0xFF26334D), RoundedCornerShape(10.dp))
                                            .clickable { selectedMarket = mType }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(mType.name, color = if (isSelected) Color(0xFF00FF66) else Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            val generatedScript = mlmEngine.getInvitationScript(selectedMarket, targetName)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp)),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("RECOMMENDED INVITATION SCRIPT", color = Color(0xFF00FF66), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(generatedScript, color = Color.White, fontSize = 13.sp, lineHeight = 18.sp)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = { onTriggerVoiceScript("Let's practice pitching $targetName with this script!") },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF66)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.Mic, contentDescription = null, tint = Color.Black)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Practice with Wife AI", color = Color.Black, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    1 -> { // OBJECTION HANDLING
                        val objections = listOf(
                            "NO_MONEY" to "1. 'I don't have money to start'",
                            "NO_TIME" to "2. 'I have a job, I don't have time'",
                            "IS_THIS_CHAIN_PYRAMID" to "3. 'Is this a pyramid or chain scheme?'"
                        )
                        items(objections.size) { index ->
                            val (key, title) = objections[index]
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .border(1.dp, Color(0xFF26334D), RoundedCornerShape(14.dp)),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(title, color = Color(0xFFFFB703), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(mlmEngine.getObjectionHandlingFormula(key), color = Color.LightGray, fontSize = 12.sp, lineHeight = 17.sp)
                                }
                            }
                        }
                    }

                    2 -> { // COMMISSION CALCULATOR
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp)),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("COMMISSION / BV CALCULATOR", color = Color(0xFF00FF66), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = mlmEngine.calculateCommissionEstimate(50000.0, 10.0),
                                        color = Color.White,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
