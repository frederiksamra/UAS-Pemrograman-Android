package com.android.laundrygo.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.laundrygo.model.CartItem
import com.android.laundrygo.model.Voucher
import com.android.laundrygo.repository.AuthRepository
import com.android.laundrygo.repository.ServiceRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// ----- State untuk UI Halaman Pembayaran/Invoice -----
data class PaymentUiState(
    val transactionId: String = "",
    val isLoading: Boolean = true,
    val error: String? = null,

    // Detail Transaksi
    val orderItems: List<CartItem> = emptyList(),
    val customerName: String = "",
    val pickupInfo: String = "",
    val transactionDate: String = "",

    // New property for storing the selected pickup time
    val selectedPickupTime: Date? = null,

    // Info Saldo & Pembayaran
    val userBalance: Double? = null,
    val availablePaymentMethods: List<String> = listOf("Balance", "E-Wallet", "Bank Transfer"),
    val selectedPaymentMethod: String? = null,

    // Info Voucher & Harga
    val myAvailableVouchers: List<Voucher> = emptyList(),
    val appliedVoucher: Voucher? = null,
    val subtotal: Double = 0.0,
    val discountAmount: Double = 0.0,
    val finalAmount: Double = 0.0, // Harga akhir setelah diskon

    // Status Proses
    val paymentStatus: PaymentStatus = PaymentStatus.IDLE,
    val showInsufficientBalanceDialog: Boolean = false
)

enum class PaymentStatus {
    IDLE, LOADING, SUCCESS, ERROR
}

// ----- Class ViewModel -----
class PaymentViewModel(
    private val serviceRepository: ServiceRepository,
    private val authRepository: AuthRepository,
    private val transactionId: String,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    init {
        loadInitialData()
        observeUserProfile() // Start observing the user profile
        listenForVoucherResult()
    }

    private fun observeUserProfile() {
        viewModelScope.launch {
            authRepository.getCurrentUserProfile()
                .onEach { result ->
                    result.fold(
                        onSuccess = { user ->
                            _uiState.update { it.copy(userBalance = user?.balance) }
                        },
                        onFailure = { exception ->
                            Log.e("PaymentViewModel", "Error observing user profile", exception)
                            _uiState.update { it.copy(error = exception.message) }
                        }
                    )
                }
                .launchIn(this)
        }
    }

    private fun listenForVoucherResult() {
        savedStateHandle.getLiveData<Voucher>("selected_voucher").observeForever { voucher ->
            voucher?.let {
                onVoucherSelected(it)
                savedStateHandle.remove<Voucher>("selected_voucher")
            }
        }
    }

    private fun loadInitialData() {
        if (transactionId.isEmpty() || userId.isNullOrEmpty()) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = "ID Transaksi atau User tidak valid."
                )
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val transactionResult = serviceRepository.getTransactionById(transactionId)
            // Removed fetching user profile here, it's now observed in observeUserProfile()
            val myVouchersResult = authRepository.getMyVouchers().first()

            val myVouchers = myVouchersResult.getOrNull() ?: emptyList()

            transactionResult.collect { result -> // Collect the Flow<Result<Transaction?>>
                val transaction = result.getOrNull() // Get the Transaction from the Result

                if (transaction != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            transactionId = transaction.id,
                            orderItems = transaction.items,
                            customerName = transaction.customerName,
                            pickupInfo = "${transaction.pickupDate} - ${transaction.pickupTime}",
                            subtotal = transaction.totalPrice,
                            finalAmount = transaction.totalPrice,
                            transactionDate = transaction.createdAt?.toFormattedString() ?: "N/A",
                            myAvailableVouchers = myVouchers.filter { !it.is_used },
                            error = null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Gagal memuat detail transaksi."
                        )
                    }
                }
            }
        }
    }

    fun onPickupTimeSelected(time: Date) {
        _uiState.update {
            it.copy(
                selectedPickupTime = time,
                pickupInfo = time.toFormattedString()
            )
        }
        Log.d("PaymentViewModel", "Pickup time selected: ${time.toFormattedString()}")
    }

    fun onVoucherSelected(voucher: Voucher?) {
        _uiState.update { it.copy(appliedVoucher = voucher) }
        calculateFinalAmount()
    }

    private fun calculateFinalAmount() {
        _uiState.update { state ->
            val voucher = state.appliedVoucher
            var discount = 0.0
            if (voucher != null) {
                discount = if (voucher.discount_type == "fixed") {
                    voucher.discount_value.toDouble()
                } else { // "percent"
                    state.subtotal * (voucher.discount_value / 100.0)
                }
            }
            val finalAmount = (state.subtotal - discount).coerceAtLeast(0.0)
            state.copy(discountAmount = discount, finalAmount = finalAmount)
        }
    }

    fun onPaymentMethodSelected(method: String) {
        _uiState.update { it.copy(selectedPaymentMethod = method) }
    }

    fun clearErrorStatus() {
        _uiState.update { it.copy(paymentStatus = PaymentStatus.IDLE, error = null) }
    }

    fun dismissInsufficientBalanceDialog() {
        _uiState.update {
            it.copy(
                showInsufficientBalanceDialog = false,
                paymentStatus = PaymentStatus.IDLE
            )
        }
    }

    fun onPayClicked() {
        if (userId.isNullOrEmpty()) return
        val currentState = _uiState.value
        val selectedMethod = currentState.selectedPaymentMethod ?: return

        _uiState.update { it.copy(paymentStatus = PaymentStatus.LOADING) }

        viewModelScope.launch {
            if (selectedMethod == "Balance") {
                val currentBalance = currentState.userBalance ?: 0.0
                if (currentBalance < currentState.finalAmount) {
                    _uiState.update {
                        it.copy(
                            paymentStatus = PaymentStatus.IDLE,
                            showInsufficientBalanceDialog = true
                        )
                    }
                    return@launch
                }

                val voucherIdToUse = currentState.appliedVoucher?.voucherDocumentId

                val paymentResult = authRepository.processPayment(
                    userId = userId,
                    transactionId = transactionId,
                    amountToDeduct = currentState.finalAmount,
                    voucherToUseId = voucherIdToUse // Use the voucher ID
                )

                if (paymentResult.isSuccess) {
                    serviceRepository.clearCart(userId)
                    serviceRepository.updateTransactionPaymentMethod(transactionId, selectedMethod)
                    // Update transaction with voucher ID
                    if (voucherIdToUse != null) {
                        serviceRepository.updateTransactionVoucherId(transactionId, voucherIdToUse)
                    }
                    _uiState.update { it.copy(paymentStatus = PaymentStatus.SUCCESS) }
                } else {
                    _uiState.update { it.copy(paymentStatus = PaymentStatus.ERROR, error = paymentResult.exceptionOrNull()?.message) }
                }

            } else {
                kotlinx.coroutines.delay(1500)
                _uiState.update {
                    it.copy(
                        paymentStatus = PaymentStatus.ERROR,
                        error = "Metode pembayaran '$selectedMethod' belum tersedia."
                    )
                }
            }
        }
    }

    // ----- Factory untuk ViewModel -----
    class PaymentViewModelFactory(
        private val serviceRepository: ServiceRepository,
        private val authRepository: AuthRepository,
        private val transactionId: String,
        private val savedStateHandle: SavedStateHandle
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PaymentViewModel::class.java)) {
                return PaymentViewModel(
                    serviceRepository,
                    authRepository,
                    transactionId,
                    savedStateHandle
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    // Helper untuk format tanggal
    private fun Date.toFormattedString(): String {
        val format = SimpleDateFormat("d MMMM 'pukul' HH:mm", Locale("id", "ID"))
        return format.format(this)
    }
}