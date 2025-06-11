package com.android.laundrygo.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.android.laundrygo.model.ServiceItem
import com.android.laundrygo.model.parsePrice

class ShoesViewModel : ViewModel() {
    val services = listOf(
        ServiceItem("Deep Cleaning Shoes", null, "IDR 60.000/Pair of shoes", 0xFFF5F5F5),
        ServiceItem("Premium Cleaning + Unyellowing", null, "IDR 80.000/Pair of shoes", 0xFFFFFDE7),
        ServiceItem("Fast Cleaning Shoes", null, "IDR 20.000/Pair of shoes", 0xFFF5F5F5)
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

