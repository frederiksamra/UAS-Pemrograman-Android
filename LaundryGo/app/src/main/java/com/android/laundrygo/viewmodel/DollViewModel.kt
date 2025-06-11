package com.android.laundrygo.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.android.laundrygo.model.ServiceItem
import com.android.laundrygo.model.parsePrice

class DollViewModel : ViewModel() {
    val services = listOf(
        ServiceItem("20 - 40cm Doll", null, "IDR 43.000/Pcs", 0xFFF5F5F5),
        ServiceItem("41 - 60cm Doll", null, "IDR 45.000/Pcs", 0xFFFFFDE7),
        ServiceItem("61 - 100cm Doll", null, "IDR 70.000/Pcs", 0xFFF5F5F5),
        ServiceItem("Up to 100cm Doll", null, "IDR 90.000/Pcs", 0xFFFFFDE7),
        ServiceItem("Huge Doll", null, "IDR 150.000/Pcs", 0xFFF5F5F5)
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
