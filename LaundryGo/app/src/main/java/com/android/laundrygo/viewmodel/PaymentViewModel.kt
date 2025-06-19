package com.android.laundrygo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.laundrygo.model.CartItem
import com.android.laundrygo.repository.ServiceRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// ----- State untuk UI Halaman Pembayaran/Invoice -----
data class PaymentUiState(
    val transactionId: String = "",
    val isLoading: Boolean = true,
    val error: String? = null,
    val orderItems: List<CartItem> = emptyList(),
    val customerName: String = "",
    val pickupInfo: String = "",
    val totalPrice: Double = 0.0,
    val transactionDate: String = "",
    val userBalance: Double? = null,
    val availablePaymentMethods: List<String> = listOf("Balance", "E-Wallet", "Bank Transfer"),
    val selectedPaymentMethod: String? = null,
    val paymentStatus: PaymentStatus = PaymentStatus.IDLE,
    val showInsufficientBalanceDialog: Boolean = false // <-- State baru untuk dialog
)

enum class PaymentStatus {
    IDLE, LOADING, SUCCESS, ERROR
}


// ----- Class ViewModel -----
class PaymentViewModel(
    private val repository: ServiceRepository,
    private val transactionId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    init {
        loadTransactionDetails()
    }

    private fun loadTransactionDetails() {
        if (transactionId.isEmpty() || userId.isNullOrEmpty()) {
            _uiState.update { it.copy(isLoading = false, error = "ID Transaksi atau User tidak valid.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val transactionResult = repository.getTransactionById(transactionId)
            repository.getCurrentUserProfile().collect { userResult ->
                transactionResult.fold(
                    onSuccess = { transaction ->
                        userResult.fold(
                            onSuccess = { user ->
                                if (transaction != null && user != null) {
                                    // PERBAIKAN: Hapus logika 'balanceSufficient' dari sini.
                                    // Kita hanya memuat data dan tidak menonaktifkan tombol apa pun.
                                    _uiState.update {
                                        it.copy(
                                            isLoading = false,
                                            transactionId = transaction.id,
                                            orderItems = transaction.items,
                                            customerName = transaction.customerName,
                                            pickupInfo = "${transaction.pickupDate} - ${transaction.pickupTime}",
                                            totalPrice = transaction.totalPrice,
                                            transactionDate = transaction.createdAt?.let { date ->
                                                SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID")).format(date)
                                            } ?: "Tanggal tidak tersedia",
                                            userBalance = user.balance,
                                            error = null
                                        )
                                    }
                                } else {
                                    _uiState.update { it.copy(isLoading = false, error = "Data transaksi atau user tidak valid.") }
                                }
                            },
                            onFailure = { userError ->
                                _uiState.update { it.copy(isLoading = false, error = userError.message) }
                            }
                        )
                    },
                    onFailure = { txError ->
                        _uiState.update { it.copy(isLoading = false, error = txError.message) }
                    }
                )
            }
        }
    }

    fun onPaymentMethodSelected(method: String) {
        _uiState.update { it.copy(selectedPaymentMethod = method) }
    }

    fun clearErrorStatus() {
        _uiState.update { it.copy(paymentStatus = PaymentStatus.IDLE, error = null) }
    }

    fun dismissInsufficientBalanceDialog() {
        _uiState.update { it.copy(showInsufficientBalanceDialog = false) }
    }

    fun onPayClicked() {
        if (userId.isNullOrEmpty()) return
        val currentState = _uiState.value
        val selectedMethod = currentState.selectedPaymentMethod ?: return

        _uiState.update { it.copy(paymentStatus = PaymentStatus.LOADING) }

        viewModelScope.launch {
            if (selectedMethod == "Balance") {
                // Proses pembayaran dengan Saldo
                val paymentResult = repository.processBalancePayment(userId, currentState.totalPrice)
                paymentResult.fold(
                    onSuccess = {
                        // Jika bayar saldo berhasil, update status transaksi jadi "Lunas"
                        val statusResult = repository.updateTransactionStatus(transactionId, "Lunas")
                        statusResult.fold(
                            onSuccess = {
                                // Jika update status berhasil, bersihkan keranjang
                                val clearCartResult = repository.clearCart(userId)
                                if(clearCartResult.isSuccess) {
                                    _uiState.update { it.copy(paymentStatus = PaymentStatus.SUCCESS) }
                                } else {
                                    _uiState.update { it.copy(paymentStatus = PaymentStatus.ERROR, error = "Pembayaran berhasil, namun gagal membersihkan keranjang.") }
                                }
                            },
                            onFailure = { exception ->
                                _uiState.update { it.copy(paymentStatus = PaymentStatus.ERROR, error = exception.message) }
                            }
                        )
                    },
                    onFailure = { exception ->
                        // Jika errornya karena saldo tidak cukup, tampilkan dialog
                        if (exception.message?.contains("Saldo tidak mencukupi") == true) {
                            _uiState.update {
                                it.copy(
                                    paymentStatus = PaymentStatus.IDLE,
                                    showInsufficientBalanceDialog = true
                                )
                            }
                        } else {
                            // Untuk error lainnya, tampilkan Toast
                            _uiState.update { it.copy(paymentStatus = PaymentStatus.ERROR, error = exception.message) }
                        }
                    }
                )
            } else {
                // Logika untuk metode pembayaran lain
                kotlinx.coroutines.delay(1500)
                _uiState.update { it.copy(paymentStatus = PaymentStatus.ERROR, error = "Metode pembayaran '$selectedMethod' belum tersedia.") }
            }
        }
    }
}


// ----- Factory untuk ViewModel -----
class PaymentViewModelFactory(
    private val repository: ServiceRepository,
    private val transactionId: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PaymentViewModel::class.java)) {
            return PaymentViewModel(repository, transactionId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}