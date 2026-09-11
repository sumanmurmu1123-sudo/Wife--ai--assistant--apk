package com.example.v2.rix.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.v2.rix.model.BusinessOpportunity
import com.example.v2.rix.model.RiskLevel
import com.example.v2.ui.theme.Cyan
import com.example.v2.ui.theme.NeonPink

@Composable
fun OpportunityCenterScreen(
    onClose: () -> Unit
) {
    val sampleOpportunities = listOf(
        BusinessOpportunity(
            id = "opp_1",
            title = "Freelance Project: Android UI Migration",
            matchScore = 94,
            budgetEstimate = "₹45,000",
            effort = "Medium",
            risk = RiskLevel.LOW,
            recommendedAction = "Prepare Proposal",
            deadline = "Oct 12, 2026",
            isHighPriority = true
        ),
        BusinessOpportunity(
            id = "opp_2",
            title = "Content Collab: Tech Startup Launch",
            matchScore = 88,
            budgetEstimate = "₹20,000",
            effort = "Low",
            risk = RiskLevel.LOW,
            recommendedAction = "Send Pitch Email",
            deadline = "Oct 15, 2026",
            isHighPriority = false
        ),
        BusinessOpportunity(
            id = "opp_3",
            title = "E-Commerce Integration API",
            matchScore = 75,
            budgetEstimate = "₹1,20,000",
            effort = "High",
            risk = RiskLevel.MEDIUM,
            recommendedAction = "Request More Info",
            deadline = "Nov 01, 2026",
            isHighPriority = true
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.95f))
            .padding(top = 40.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Opportunity Center",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            Text(
                text = "Powered by RIX (Real-time Intelligence eXecution)",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(sampleOpportunities) { opp ->
                    OpportunityCard(opp)
                }
            }
        }
    }
}

@Composable
fun OpportunityCard(opp: BusinessOpportunity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E24))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (opp.isHighPriority) {
                        Icon(Icons.Default.LocalFireDepartment, contentDescription = "Hot", tint = NeonPink, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = opp.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Cyan.copy(alpha = 0.2f),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = "\${opp.matchScore}% Match",
                        color = Cyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Budget", color = Color.Gray, fontSize = 12.sp)
                    Text(opp.budgetEstimate, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
                Column {
                    Text("Effort", color = Color.Gray, fontSize = 12.sp)
                    Text(opp.effort, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
                Column {
                    Text("Deadline", color = Color.Gray, fontSize = 12.sp)
                    Text(opp.deadline, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = Color.DarkGray)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = "Risk", tint = if (opp.risk == RiskLevel.LOW) Color.Green else Color.Yellow, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Risk: \${opp.risk.name}", color = Color.LightGray, fontSize = 12.sp)
                }
                
                Button(
                    onClick = { /* Handle action */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Cyan),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(opp.recommendedAction, color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
