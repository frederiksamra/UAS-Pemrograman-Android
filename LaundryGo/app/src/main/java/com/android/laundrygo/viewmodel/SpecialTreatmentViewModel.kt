package com.android.laundrygo.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.android.laundrygo.model.ServiceItem
import com.android.laundrygo.model.parsePrice

class SpecialTreatmentViewModel : ViewModel() {
    val services = listOf(
        ServiceItem("Special Suits", null, "IDR 43.000/Pcs", 0xFFF5F5F5),
        ServiceItem("Special Coat", null, "IDR 30.000/Pcs", 0xFFFFFDE7),
        ServiceItem("Special Short Dress", null, "IDR 50.000/Pcs", 0xFFF5F5F5),
        ServiceItem("Special Long Dress", null, "IDR 27.000/Pcs", 0xFFFFFDE7)
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

