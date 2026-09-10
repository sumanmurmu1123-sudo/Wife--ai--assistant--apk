package com.example.v2.payment.model

enum class PaymentProviderType {
    PHONEPE,
    GOOGLE_PAY,
    PAYTM,
    UPI_INTENT_FALLBACK
}

enum class PaymentStatus {
    IDLE,
    REVIEW_REQUIRED,
    USER_CONFIRMED,
    INITIATING,
    AWAITING_USER_AUTHORIZATION,
    PROCESSING,
    SUCCESS,
    FAILED,
    CANCELLED,
    UNKNOWN
}

data class PaymentRequest(
    val transactionId: String,
    val amount: Long, // Stored in paise (amount in INR * 100)
    val currency: String = "INR",
    val recipientName: String?,
    val recipientUpiId: String,
    val note: String?,
    val provider: PaymentProviderType
)

data class PaymentResult(
    val status: PaymentStatus,
    val transactionId: String,
    val providerReferenceId: String?,
    val message: String?
)

data class PaymentUiState(
    val status: PaymentStatus = PaymentStatus.IDLE,
    val request: PaymentRequest? = null,
    val error: String? = null,
    val isLoading: Boolean = false,
    val receiptReferenceId: String? = null
)
