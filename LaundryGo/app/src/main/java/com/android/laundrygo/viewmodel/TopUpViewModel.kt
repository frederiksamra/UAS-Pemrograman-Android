package com.android.laundrygo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.laundrygo.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class TopUpUiState {
    object Idle : TopUpUiState()
    object Processing : TopUpUiState()
    object Success : TopUpUiState()
}

data class TopUpState(
    val currentBalance: Long = 0L, // Nilai awal 0, akan di-fetch dari Firestore
    val topUpAmounts: List<Long> = listOf(10000, 20000, 25000, 50000, 75000, 100000),
    val paymentMethods: List<String> = listOf("E-Wallet (GoPay, OVO, etc)", "Virtual Account", "Credit/Debit Card"),
    val selectedAmount: Long? = null,
    val selectedPaymentMethod: String? = "E-Wallet (GoPay, OVO, etc)",
    val customAmount: String = "",
    val isCustomAmountSelected: Boolean = false,
    val uiState: TopUpUiState = TopUpUiState.Idle,
    val errorMessage: String? = null
)
class TopUpViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow(TopUpState())
    val state = _state.asStateFlow()

    // BARU: Fetch saldo awal saat ViewModel dibuat
    init {
        fetchCurrentUserBalance()
    }

    private fun fetchCurrentUserBalance() {
        viewModelScope.launch {
            authRepository.getUserProfile().onSuccess { user ->
                _state.update { it.copy(currentBalance = user.balance) }
            }.onFailure { exception ->
                _state.update { it.copy(errorMessage = "Failed to load balance: ${exception.message}") }
            }
        }
    }

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

    // DIUBAH TOTAL: Logika proses pembayaran yang sebenarnya
    fun processPayment() {
        val currentState = _state.value
        val amountToAdd = if (currentState.isCustomAmountSelected) {
            currentState.customAmount.toLongOrNull() ?: 0L
        } else {
            currentState.selectedAmount ?: 0L
        }

        if (amountToAdd <= 0) {
            _state.update { it.copy(errorMessage = "Please select or enter a top up amount.") }
            return
        }
        if (currentState.selectedPaymentMethod == null) {
            _state.update { it.copy(errorMessage = "Please select a payment method.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(uiState = TopUpUiState.Processing) }

            val result = authRepository.performTopUp(amountToAdd)

            result.onSuccess {
                // Jika berhasil, fetch ulang profil untuk mendapatkan saldo terbaru
                fetchCurrentUserBalance()
                _state.update { it.copy(uiState = TopUpUiState.Success) }
            }.onFailure { exception ->
                _state.update {
                    it.copy(
                        uiState = TopUpUiState.Idle,
                        errorMessage = "Top up failed: ${exception.message}"
                    )
                }
            }
        }
    }

    fun clearErrorMessage() {
        _state.update { it.copy(errorMessage = null) }
    }

    fun finishTopUp() {
        _state.update {
            it.copy(
                uiState = TopUpUiState.Idle,
                selectedAmount = null,
                customAmount = "",
                isCustomAmountSelected = false
            )
        }
    }

    // BARU: Factory untuk membuat instance ViewModel dengan dependensi (pola yang sudah Anda setujui)
    companion object {
        fun provideFactory(
            repository: AuthRepository,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(TopUpViewModel::class.java)) {
                    return TopUpViewModel(repository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}