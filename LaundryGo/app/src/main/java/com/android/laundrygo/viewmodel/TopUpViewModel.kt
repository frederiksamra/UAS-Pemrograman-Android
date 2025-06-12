package com.android.laundrygo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Merepresentasikan tiga kondisi UI yang berbeda
sealed class TopUpUiState {
    object Idle : TopUpUiState()
    object Processing : TopUpUiState()
    object Success : TopUpUiState()
}

data class TopUpState(
    val currentBalance: Long = 150000,
    val topUpAmounts: List<Long> = listOf(13000, 18000, 23000, 28000, 33000, 38000),
    val paymentMethods: List<String> = listOf("E-Wallet", "ATM", "M-Banking"),
    val selectedAmount: Long? = null,
    val selectedPaymentMethod: String? = null,
    val uiState: TopUpUiState = TopUpUiState.Idle
)

class TopUpViewModel : ViewModel() {

    private val _state = MutableStateFlow(TopUpState())
    val state = _state.asStateFlow()

    fun selectAmount(amount: Long) {
        _state.update { it.copy(selectedAmount = amount) }
    }

    fun selectPaymentMethod(method: String) {
        _state.update { it.copy(selectedPaymentMethod = method) }
    }

    fun processPayment() {
        viewModelScope.launch {
            // Ganti state ke Processing
            _state.update { it.copy(uiState = TopUpUiState.Processing) }

            // Simulasi proses pembayaran
            delay(3000)

            // Ganti state ke Success
            val newBalance = _state.value.currentBalance + (_state.value.selectedAmount ?: 0)
            _state.update { it.copy(uiState = TopUpUiState.Success, currentBalance = newBalance) }
        }
    }

    fun finishTopUp() {
        // Kembali ke state awal setelah selesai
        _state.update {
            it.copy(
                uiState = TopUpUiState.Idle,
                selectedAmount = null,
                selectedPaymentMethod = null
            )
        }
    }
}