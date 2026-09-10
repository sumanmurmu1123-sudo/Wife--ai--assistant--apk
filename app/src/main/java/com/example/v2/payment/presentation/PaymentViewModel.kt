package com.example.v2.payment.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.v2.payment.data.PaymentRepositoryImpl
import com.example.v2.payment.model.PaymentRequest
import com.example.v2.payment.model.PaymentStatus
import com.example.v2.payment.model.PaymentUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PaymentViewModel : ViewModel() {
    private val repository = PaymentRepositoryImpl()

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    fun initiatePaymentRequest(amount: Double, recipientName: String?, upiId: String, note: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // Convert to paise safely
                val amountPaise = (amount * 100).toLong()
                
                val request = repository.createPaymentRequest(
                    amountPaise = amountPaise,
                    recipientName = recipientName,
                    recipientUpiId = upiId,
                    note = note
                )
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    status = PaymentStatus.REVIEW_REQUIRED,
                    request = request
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to create payment request: ${e.message}"
                )
            }
        }
    }

    fun userConfirmedPayment() {
        if (_uiState.value.status == PaymentStatus.REVIEW_REQUIRED) {
            _uiState.value = _uiState.value.copy(status = PaymentStatus.INITIATING)
        }
    }

    // Called when the UI has launched the Intent and is waiting for user
    fun paymentIntentLaunched() {
        if (_uiState.value.status == PaymentStatus.INITIATING) {
            _uiState.value = _uiState.value.copy(status = PaymentStatus.AWAITING_USER_AUTHORIZATION)
        }
    }

    // Handle the result from the UPI App
    fun handleUpiResult(data: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(status = PaymentStatus.PROCESSING, isLoading = true)
            
            val statusMap = mutableMapOf<String, String>()
            data?.split("&")?.forEach {
                val parts = it.split("=")
                if (parts.size == 2) {
                    statusMap[parts[0].lowercase()] = parts[1].lowercase()
                }
            }

            val status = statusMap["status"] ?: "unknown"
            
            when {
                status == "success" || status == "submitted" -> {
                    // VERIFY ON BACKEND
                    verifyOnBackend()
                }
                status == "failure" -> {
                    _uiState.value = _uiState.value.copy(
                        status = PaymentStatus.FAILED,
                        isLoading = false,
                        error = "Payment failed at provider."
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(
                        status = PaymentStatus.CANCELLED,
                        isLoading = false
                    )
                }
            }
        }
    }

    private suspend fun verifyOnBackend() {
        val req = _uiState.value.request ?: return
        try {
            val finalStatus = repository.verifyPaymentOnBackend(req.transactionId)
            _uiState.value = _uiState.value.copy(
                status = finalStatus,
                isLoading = false,
                receiptReferenceId = req.transactionId
            )
        } catch (e: Exception) {
             _uiState.value = _uiState.value.copy(
                status = PaymentStatus.UNKNOWN,
                isLoading = false,
                error = "Status unknown. Please check your UPI history."
            )
        }
    }

    fun reset() {
        _uiState.value = PaymentUiState()
    }

    fun buildUpiIntentUri(request: PaymentRequest): Uri {
        val amountInRupees = String.format(java.util.Locale.US, "%.2f", request.amount / 100.0)
        return Uri.Builder()
            .scheme("upi")
            .authority("pay")
            .appendQueryParameter("pa", request.recipientUpiId)
            .appendQueryParameter("pn", request.recipientName ?: "User")
            .appendQueryParameter("tr", request.transactionId)
            .appendQueryParameter("am", amountInRupees)
            .appendQueryParameter("cu", "INR")
            .appendQueryParameter("tn", request.note ?: "")
            .build()
    }
}
