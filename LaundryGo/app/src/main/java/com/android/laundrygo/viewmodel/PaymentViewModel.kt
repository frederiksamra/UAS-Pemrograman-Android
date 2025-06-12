package com.android.laundrygo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.laundrygo.data.model.OrderItem // Pastikan import ini ada
import com.android.laundrygo.data.repository.FakePaymentRepositoryImpl
import com.android.laundrygo.data.repository.PaymentRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// ----- Definisi State dan Enum digabungkan di sini -----

data class PaymentUiState(
    val orderItems: List<OrderItem> = emptyList(),
    val subtotal: Int = 0,
    val discount: Int = 0,
    val voucher: Int = 0,
    val totalPrice: Int = 0,
    val transactionDate: String = "",
    val availablePaymentMethods: List<String> = listOf("Balance", "E-Wallet", "Bank Transfer"),
    val selectedPaymentMethod: String? = null,
    val paymentStatus: PaymentStatus = PaymentStatus.IDLE
)

enum class PaymentStatus {
    IDLE, LOADING, SUCCESS, ERROR
}


// ----- Class ViewModel -----

class PaymentViewModel(private val paymentRepository: PaymentRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadOrderDetails()
    }

    private fun loadOrderDetails() {
        viewModelScope.launch {
            val items = paymentRepository.getOrderDetails()
            val subtotal = items.sumOf { it.subtotal }
            val total = subtotal
            val date = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID")).format(Date())

            _uiState.update { currentState ->
                currentState.copy(
                    orderItems = items,
                    subtotal = subtotal,
                    totalPrice = total,
                    transactionDate = date,
                    paymentStatus = PaymentStatus.IDLE
                )
            }
        }
    }

    fun onPaymentMethodSelected(method: String) {
        _uiState.update { it.copy(selectedPaymentMethod = method) }
    }

    fun onPayClicked() {
        viewModelScope.launch {
            _uiState.update { it.copy(paymentStatus = PaymentStatus.LOADING) }
            delay(2000)
            _uiState.update { it.copy(paymentStatus = PaymentStatus.SUCCESS) }
        }
    }
}


// ----- Factory untuk ViewModel -----

class PaymentViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PaymentViewModel::class.java)) {
            val repository = FakePaymentRepositoryImpl()
            return PaymentViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}