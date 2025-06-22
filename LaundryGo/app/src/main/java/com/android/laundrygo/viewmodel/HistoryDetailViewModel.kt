package com.android.laundrygo.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.laundrygo.model.Transaction
import com.android.laundrygo.model.Voucher
import com.android.laundrygo.repository.ServiceRepository
import com.android.laundrygo.repository.ServiceRepositoryImpl
import com.android.laundrygo.util.formatRupiah
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

data class ProductDetail(
    val name: String,
    val price: String,
    val qty: String,
    val subtotal: String
)

data class HistoryDetailState(
    val transaction: Transaction? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val totalPayment: Double = 0.0,
    val subtotal: Double = 0.0,
    val discount: Double = 0.0,
    val voucher: Double = 0.0,
    val total: Double = 0.0,
    val appliedVoucher: Voucher? = null // State to hold the fetched voucher
) {
    val totalPaymentFormatted: String
        get() = formatRupiah(totalPayment)
    val subtotalFormatted: String
        get() = formatRupiah(subtotal)
    val discountFormatted: String
        get() = formatRupiah(discount)
    val voucherFormatted: String
        get() = formatRupiah(voucher)
    val finalAmountFormatted: String
        get() = formatRupiah(total)
}

class HistoryDetailViewModel(
    private val orderId: String,
    private val repository: ServiceRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryDetailState())
    val state: StateFlow<HistoryDetailState> = _state.asStateFlow()

    init {
        fetchTransactionDetails(orderId)
    }

    fun fetchTransactionDetails(orderId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, appliedVoucher = null, discount = 0.0) }
            repository.getTransactionById(orderId)
                .collect { transactionResult ->
                    transactionResult.fold(
                        onSuccess = { transaction ->
                            if (transaction != null) {
                                // Calculate subtotal from items
                                val calculatedSubtotal = transaction.items.sumOf { it.price * it.quantity }

                                _state.update {
                                    it.copy(
                                        isLoading = false,
                                        transaction = transaction,
                                        totalPayment = transaction.totalPrice,
                                        subtotal = calculatedSubtotal, // Set calculated subtotal
                                        total = transaction.totalPrice, // totalPrice is the final amount
                                        error = null
                                    )
                                }
                                // Fetch the voucher if a voucherId exists
                                transaction.voucherId?.let { voucherId ->
                                    repository.getVoucherById(voucherId)
                                        .collect { voucherResult ->
                                            voucherResult.fold(
                                                onSuccess = { voucher ->
                                                    voucher?.let {
                                                        _state.update { state ->
                                                            val discountAmount = calculateDiscount(state.subtotal, it)
                                                            Log.d("HistoryDetailVM", "Subtotal: ${state.subtotal}")
                                                            Log.d("HistoryDetailVM", "Discount Amount: ${discountAmount}")
                                                            val newTotal = state.subtotal - discountAmount
                                                            Log.d("HistoryDetailVM", "Total after discount: ${newTotal}")
                                                            state.copy(appliedVoucher = it, discount = discountAmount, total = newTotal)
                                                        }
                                                    }
                                                },
                                                onFailure = { e ->
                                                    Log.e("HistoryDetailViewModel", "Error fetching voucher", e)
                                                }
                                            )
                                        }
                                }
                            } else {
                                _state.update { it.copy(isLoading = false, error = "Transaksi tidak ditemukan.") }
                            }
                        },
                        onFailure = { exception ->
                            _state.update { it.copy(isLoading = false, error = exception.message) }
                        }
                    )
                }
        }
    }

    private fun calculateDiscount(subtotal: Double, voucher: Voucher): Double {
        return if (voucher.discount_type == "fixed") {
            voucher.discount_value.toDouble()
        } else { // "percent"
            subtotal * (voucher.discount_value / 100.0)
        }
    }

    private fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        format.maximumFractionDigits = 0
        return format.format(amount).replace("IDR", "Rp").trim()
    }
}

class HistoryDetailViewModelFactory(private val orderId: String) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryDetailViewModel::class.java)) {
            return HistoryDetailViewModel(orderId, provideRepository()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

    private fun provideRepository(): ServiceRepository {
        return ServiceRepositoryImpl() // Replace with your actual dependency injection if used
    }
}