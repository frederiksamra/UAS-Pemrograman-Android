package com.android.laundrygo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.laundrygo.model.Transaction
import com.android.laundrygo.model.Voucher
import com.android.laundrygo.repository.ServiceRepository
import com.android.laundrygo.util.formatRupiah
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OrderDetailState(
    val order: Transaction? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val subtotalFormatted: String = "Rp0",
    val discountFormatted: String = "Rp0",
    val finalPriceFormatted: String = "Rp0"
)

class OrderManagementViewModel(
    private val serviceRepository: ServiceRepository
) : ViewModel() {

    private val _orders = MutableStateFlow<List<Transaction>>(emptyList())
    val orders: StateFlow<List<Transaction>> = _orders.asStateFlow()

    private val _selectedOrderState = MutableStateFlow(OrderDetailState())
    val selectedOrderState: StateFlow<OrderDetailState> = _selectedOrderState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    val statusOptions = listOf("Pick Up", "Washing", "Washed", "Delivery", "Completed")

    init {
        loadOrders()
    }

    private fun loadOrders() {
        _isLoading.update { true }
        viewModelScope.launch {
            try {
                val ordersResult = serviceRepository.getAllOrders().first()

                if (ordersResult.isSuccess) {
                    val ordersList = ordersResult.getOrNull() ?: emptyList()

                    // Filter pesanan yang belum dibayar (status 0) DAN yang sudah selesai (status 7)
                    val filteredOrders = ordersList.filter { it.status != 0 && it.status != 7 }

                    _orders.update { filteredOrders }
                } else {
                    _feedbackMessage.update { "Gagal memuat data: ${ordersResult.exceptionOrNull()?.message}" }
                }
            } catch (e: Exception) {
                _feedbackMessage.update { "Terjadi error: ${e.message}" }
            } finally {
                _isLoading.update { false }
            }
        }
    }

    fun loadOrderDetails(orderId: String) {
        viewModelScope.launch {
            _selectedOrderState.update { it.copy(isLoading = true) }
            try {
                val orderResult = serviceRepository.getTransactionById(orderId).first()
                orderResult.onSuccess { transaction ->
                    if (transaction == null) {
                        _selectedOrderState.update { it.copy(error = "Pesanan tidak ditemukan.", isLoading = false) }
                        return@onSuccess
                    }

                    var discountAmount = 0.0
                    if (!transaction.voucherId.isNullOrBlank()) {
                        val voucherResult = serviceRepository.getVoucherById(transaction.voucherId).first()
                        voucherResult.onSuccess { voucher ->
                            voucher?.let {
                                discountAmount = calculateDiscount(transaction.totalPrice, it)
                            }
                        }
                    }

                    val finalPrice = transaction.totalPrice - discountAmount

                    _selectedOrderState.update {
                        it.copy(
                            order = transaction,
                            isLoading = false,
                            subtotalFormatted = formatRupiah(transaction.totalPrice),
                            discountFormatted = "- ${formatRupiah(discountAmount)}",
                            finalPriceFormatted = formatRupiah(finalPrice)
                        )
                    }
                }.onFailure { error ->
                    _selectedOrderState.update { it.copy(error = error.message, isLoading = false) }
                }
            } catch (e: Exception) {
                _selectedOrderState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    private fun calculateDiscount(totalPrice: Double, voucher: Voucher): Double {
        return if (voucher.discount_type == "percent") {
            (totalPrice * voucher.discount_value) / 100
        } else {
            voucher.discount_value
        }
    }

    fun clearSelectedOrder() {
        _selectedOrderState.update { OrderDetailState() }
    }

    fun updateOrderStatus(transactionId: String, newStatusIndex: Int) {
        _isLoading.update { true }
        viewModelScope.launch {
            val finalStatus = newStatusIndex + 3
            serviceRepository.updateOrderStatus(transactionId, finalStatus)
                .onSuccess {
                    _feedbackMessage.update { "Status pesanan berhasil diperbarui" }
                    loadOrders()
                }
                .onFailure { error ->
                    _feedbackMessage.update { "Gagal memperbarui status pesanan: ${error.message}" }
                }
            _isLoading.update { false }
        }
    }

    fun clearFeedbackMessage() {
        _feedbackMessage.update { null }
    }
}

class OrderManagementViewModelFactory(
    private val serviceRepository: ServiceRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OrderManagementViewModel::class.java)) {
            return OrderManagementViewModel(serviceRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
