package com.example.v2.payment.presentation

import android.app.Activity.RESULT_OK
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.v2.payment.model.PaymentStatus
import com.example.v2.payment.model.PaymentUiState
import kotlinx.coroutines.delay

@Composable
fun PaymentScreen(
    viewModel: PaymentViewModel,
    onClose: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data?.getStringExtra("response")
        viewModel.handleUpiResult(data)
    }

    LaunchedEffect(uiState.status) {
        if (uiState.status == PaymentStatus.INITIATING) {
            val req = uiState.request
            if (req != null) {
                val uri = viewModel.buildUpiIntentUri(req)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                // Let Android resolve available UPI apps
                try {
                    launcher.launch(intent)
                    viewModel.paymentIntentLaunched()
                } catch (e: Exception) {
                    // No UPI app found
                    viewModel.handleUpiResult("status=failure&message=No UPI App found")
                }
            }
        }
    }

    if (uiState.status != PaymentStatus.IDLE) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E24))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    
                    when (uiState.status) {
                        PaymentStatus.REVIEW_REQUIRED -> {
                            Text("PAYMENT REVIEW", color = Color(0xFFE0B0FF), fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            val req = uiState.request!!
                            val rupees = String.format(java.util.Locale.US, "%.2f", req.amount / 100.0)
                            
                            Text("₹$rupees", color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            ReviewRow("Recipient", req.recipientName ?: "Unknown")
                            ReviewRow("UPI ID", req.recipientUpiId)
                            ReviewRow("Purpose", req.note ?: "Optional")
                            ReviewRow("Method", "UPI / PhonePe")
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                TextButton(onClick = { viewModel.reset(); onClose() }) {
                                    Text("CANCEL", color = Color.White.copy(alpha = 0.6f))
                                }
                                Button(
                                    onClick = { viewModel.userConfirmedPayment() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9D4EDD))
                                ) {
                                    Text("CONFIRM & PAY", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        
                        PaymentStatus.PROCESSING, PaymentStatus.AWAITING_USER_AUTHORIZATION -> {
                            CircularProgressIndicator(color = Color(0xFF9D4EDD))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (uiState.status == PaymentStatus.PROCESSING) "Verifying transaction securely..." else "Awaiting authorization in UPI App...",
                                color = Color.White
                            )
                        }
                        
                        PaymentStatus.SUCCESS -> {
                            Text("💜", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Payment Successful", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Transaction ID: ${uiState.receiptReferenceId}", color = Color.White.copy(alpha = 0.6f))
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { viewModel.reset(); onClose() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9D4EDD))
                            ) {
                                Text("DONE")
                            }
                        }
                        
                        PaymentStatus.FAILED, PaymentStatus.CANCELLED, PaymentStatus.UNKNOWN -> {
                            Text(if (uiState.status == PaymentStatus.CANCELLED) "Payment Cancelled" else "Payment Failed", color = Color(0xFFFF5252), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(uiState.error ?: "Payment was not completed.", color = Color.White.copy(alpha = 0.8f))
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { viewModel.reset(); onClose() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f))
                            ) {
                                Text("CLOSE", color = Color.White)
                            }
                        }
                        
                        else -> { }
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp)
        Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}
