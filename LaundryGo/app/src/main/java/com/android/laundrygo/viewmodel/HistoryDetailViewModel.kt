package com.android.laundrygo.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class ProductDetail(
    val name: String,
    val price: String,
    val qty: String,
    val subtotal: String
)

data class HistoryDetailState(
    val orderId: String = "LG012345678",
    val name: String = "Saorise",
    val phone: String = "07624316283",
    val totalPayment: String = "IDR 30.000",
    val paymentMethod: String = "COD",
    val paymentStatus: String = "Done",
    val address: String = "Jl. Melati No. 15A\nKota Baru, Jakarta Selatan\nIndonesia",
    val productList: List<ProductDetail> = listOf(
        ProductDetail("Fast Cleaning Shoes", "20.000", "1", "20.000"),
        ProductDetail("Iron only", "9.000", "1.2Kg", "10.000")
    ),
    val subtotal: String = "IDR 30.000",
    val discount: String = "IDR 0",
    val voucher: String = "IDR 0",
    val total: String = "IDR 30.000"
)

class HistoryDetailViewModel : ViewModel() {

    private val _state = MutableStateFlow(HistoryDetailState())
    val state: StateFlow<HistoryDetailState> = _state
}
