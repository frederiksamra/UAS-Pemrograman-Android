package com.android.laundrygo.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.android.laundrygo.model.ServiceItem
import com.android.laundrygo.model.parsePrice

class ShirtPantsViewModel : ViewModel() {
    val services = listOf(
        ServiceItem("King Package", "Wash express, ironing, pick up and drop off", "IDR 35.000/Kg", 0xFFF5F5F5),
        ServiceItem("General Package", "Express washing and ironing", "IDR 30.000/Kg", 0xFFFFFDE7),
        ServiceItem("Extra Regular Package", "Regular washing, ironing, pick up and delivery", "IDR 20.000/Kg", 0xFFF5F5F5),
        ServiceItem("Regular Package", "Regular washing and ironing", "IDR 14.000/Kg", 0xFFFFFDE7)
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

