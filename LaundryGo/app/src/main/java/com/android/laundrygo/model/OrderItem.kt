package com.android.laundrygo.data.model

data class OrderItem(
    val name: String,
    val price: Int,
    val qty: String,
    val subtotal: Int
)