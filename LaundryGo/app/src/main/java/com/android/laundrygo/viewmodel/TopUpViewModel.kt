package com.android.laundrygo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Tidak ada perubahan pada sealed class dan data class
sealed class TopUpUiState {
    object Idle : TopUpUiState()
    object Processing : TopUpUiState()
    object Success : TopUpUiState()
}

data class TopUpState(
    val currentBalance: Long = 150000,
    val topUpAmounts: List<Long> = listOf(10000, 20000, 25000, 50000, 75000, 100000),
    val paymentMethods: List<String> = listOf("E-Wallet (GoPay, OVO, etc)", "Virtual Account", "Credit/Debit Card"),
    val selectedAmount: Long? = null,
    val selectedPaymentMethod: String? = "E-Wallet (GoPay, OVO, etc)",
    val customAmount: String = "",
    val isCustomAmountSelected: Boolean = false,
    val uiState: TopUpUiState = TopUpUiState.Idle,
    val errorMessage: String? = null
) {
     val selectedMethod: String?
         get() = selectedPaymentMethod
}


class TopUpViewModel : ViewModel() {

    private val _state = MutableStateFlow(TopUpState())
    val state = _state.asStateFlow()

    // --- Tidak ada perubahan pada fungsi-fungsi ini ---
    fun selectAmount(amount: Long) {
        _state.update {
            it.copy(
                selectedAmount = amount,
                customAmount = "",
                isCustomAmountSelected = false,
                errorMessage = null
            )
        }
    }

    fun onCustomAmountChanged(input: String) {
        val sanitizedInput = input.filter { it.isDigit() }
        _state.update {
            it.copy(
                customAmount = sanitizedInput,
                isCustomAmountSelected = sanitizedInput.isNotBlank(),
                selectedAmount = null,
                errorMessage = null
            )
        }
    }

    fun selectPaymentMethod(method: String) {
        _state.update { it.copy(selectedPaymentMethod = method) }
    }

    // --- PERBAIKAN UTAMA DI SINI ---
    fun processPayment() {
        // 1. Ambil snapshot dari state saat ini untuk memastikan konsistensi data
        val currentState = _state.value

        val amountToAdd = if (currentState.isCustomAmountSelected) {
            currentState.customAmount.toLongOrNull() ?: 0L
        } else {
            currentState.selectedAmount ?: 0L
        }

        // Validasi tetap sama, tidak ada perubahan
        if (amountToAdd <= 0) {
            _state.update { it.copy(errorMessage = "Please select or enter a top up amount.") }
            return
        }
        if (currentState.selectedPaymentMethod == null) {
            _state.update { it.copy(errorMessage = "Please select a payment method.") }
            return
        }

        viewModelScope.launch {
            // Set state ke processing
            _state.update { it.copy(uiState = TopUpUiState.Processing, errorMessage = null) }

            // Simulasi proses
            delay(2500)

            // 2. Gunakan data dari `currentState` yang diambil di awal, bukan `_state.value` yang baru
            // Ini untuk mencegah "race condition" jika state berubah selama delay
            val newBalance = currentState.currentBalance + amountToAdd

            _state.update {
                it.copy(
                    uiState = TopUpUiState.Success,
                    currentBalance = newBalance
                )
            }
        }
    }

    fun clearErrorMessage() {
        _state.update { it.copy(errorMessage = null) }
    }

    // --- PENYEDERHANAAN DI SINI ---
    fun finishTopUp() {
        _state.update {
            it.copy(
                uiState = TopUpUiState.Idle,
                selectedAmount = null,
                customAmount = "",
                isCustomAmountSelected = false,
                errorMessage = null
                // Tidak perlu `selectedPaymentMethod = it.selectedPaymentMethod`
                // karena `copy()` secara default akan mempertahankan nilai yang tidak diubah.
            )
        }
    }
}