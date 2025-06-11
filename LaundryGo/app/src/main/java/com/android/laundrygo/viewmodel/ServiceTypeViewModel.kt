package com.android.laundrygo.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.android.laundrygo.model.ServiceItem
import com.android.laundrygo.model.parsePrice

// ViewModel untuk ServiceType
class ServiceTypeViewModel : ViewModel() {
    val services = listOf(
        ServiceItem("Shirt and Pants", null, "IDR 35.000/Kg", 0xFFF5F5F5),
        ServiceItem("Special Treatment", null, "IDR 43.000/Pcs", 0xFFFFFDE7),
        ServiceItem("Doll", null, "IDR 43.000/Pcs", 0xFFF5F5F5),
        ServiceItem("Bag", null, "IDR 70.000/Pcs", 0xFFFFFDE7),
        ServiceItem("Shoes", null, "IDR 60.000/Pair", 0xFFF5F5F5)
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

