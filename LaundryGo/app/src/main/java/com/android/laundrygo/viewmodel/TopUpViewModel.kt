package com.android.laundrygo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.laundrygo.repository.AuthRepository
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
    val currentBalance: Double = 0.0,
    val topUpAmounts: List<Double> = listOf(10000.0, 20000.0, 25000.0, 50000.0, 75000.0, 100000.0),
    val paymentMethods: List<String> = listOf("E-Wallet (GoPay, OVO, etc)", "Virtual Account", "Credit/Debit Card"),
    val selectedAmount: Double? = null,
    val selectedPaymentMethod: String? = "E-Wallet (GoPay, OVO, etc)",
    val customAmount: String = "",
    val isCustomAmountSelected: Boolean = false,
    val uiState: TopUpUiState = TopUpUiState.Idle,
    val errorMessage: String? = null
)
class TopUpViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow(TopUpState())
    val state = _state.asStateFlow()

    init {
        fetchCurrentUserBalance()
    }

    private fun fetchCurrentUserBalance() {
        viewModelScope.launch {
            authRepository.getUserProfile().onSuccess { user -> // 'user' bertipe User?
                // --- PERBAIKAN DI SINI ---
                if (user != null) {
                    // Jika user tidak null, kita aman mengakses propertinya
                    _state.update { it.copy(currentBalance = user.balance) }
                } else {
                    // Jika user null, tangani sebagai error
                    _state.update { it.copy(errorMessage = "Gagal memuat data saldo pengguna.") }
                }
            }.onFailure { exception ->
                _state.update { it.copy(errorMessage = "Failed to load balance: ${exception.message}") }
            }
        }
    }

    fun selectAmount(amount: Double) {
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

    fun processPayment() {
        val currentState = _state.value
        val amountToAdd = if (currentState.isCustomAmountSelected) {
            currentState.customAmount.toDoubleOrNull() ?: 0.0
        } else {
            currentState.selectedAmount ?: 0.0
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
                // Fetch ulang saldo setelah berhasil top up
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