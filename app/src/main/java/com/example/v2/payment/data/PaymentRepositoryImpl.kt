package com.example.v2.payment.data

import com.example.v2.payment.domain.PaymentRepository
import com.example.v2.payment.model.PaymentProviderType
import com.example.v2.payment.model.PaymentRequest
import com.example.v2.payment.model.PaymentStatus
import kotlinx.coroutines.delay
import java.util.UUID

class PaymentRepositoryImpl : PaymentRepository {
    
    // Simulates a backend call to generate a secure transaction ID
    override suspend fun createPaymentRequest(
        amountPaise: Long,
        recipientName: String?,
        recipientUpiId: String,
        note: String?
    ): PaymentRequest {
        delay(500) // Simulate network delay
        val txId = "TXN_${UUID.randomUUID().toString().replace("-", "").uppercase().take(12)}"
        return PaymentRequest(
            transactionId = txId,
            amount = amountPaise,
            currency = "INR",
            recipientName = recipientName,
            recipientUpiId = recipientUpiId,
            note = note ?: "Payment via Wife Assistant",
            provider = PaymentProviderType.UPI_INTENT_FALLBACK // Defaulting to UPI Intent
        )
    }

    // Simulates a backend call to verify the payment status with the official provider
    override suspend fun verifyPaymentOnBackend(transactionId: String): PaymentStatus {
        delay(1500) // Simulate backend validation delay
        // In a real app, the backend verifies the status from PhonePe/Provider via S2S API
        // For simulation, we assume SUCCESS if it reached this stage successfully
        return PaymentStatus.SUCCESS
    }
}
