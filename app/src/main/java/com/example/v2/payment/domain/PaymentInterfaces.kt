package com.example.v2.payment.domain

import com.example.v2.payment.model.PaymentRequest
import com.example.v2.payment.model.PaymentResult
import com.example.v2.payment.model.PaymentStatus

interface PaymentProvider {
    suspend fun initiatePayment(request: PaymentRequest): PaymentResult
    suspend fun verifyPayment(transactionId: String): PaymentStatus
}

interface PaymentRepository {
    suspend fun createPaymentRequest(amountPaise: Long, recipientName: String?, recipientUpiId: String, note: String?): PaymentRequest
    suspend fun verifyPaymentOnBackend(transactionId: String): PaymentStatus
}
