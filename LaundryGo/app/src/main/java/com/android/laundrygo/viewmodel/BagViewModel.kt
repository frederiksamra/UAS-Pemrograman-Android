package com.android.laundrygo.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.android.laundrygo.model.ServiceItem
import com.android.laundrygo.model.parsePrice

class BagViewModel : ViewModel() {
    val services = listOf(
        ServiceItem("Deep Cleaning Bag", null, "IDR 70.000/Pcs", 0xFFF5F5F5),
        ServiceItem("Premium Cleaning Bag", null, "IDR 90.000/Pcs", 0xFFFFFDE7),
        ServiceItem("Fast Cleaning Bag", null, "IDR 40.000/Pcs", 0xFFF5F5F5)
    )
    val cart = mutableStateListOf<ServiceItem>()
    fun addToCart(item: ServiceItem) {
        cart.add(item)
    }
    fun removeFromCart(item: ServiceItem) {
        cart.remove(item)
    }
    fun getTotalPrice(): Int {
        return cart.sumOf { parsePrice(it.price) }
    }
}
