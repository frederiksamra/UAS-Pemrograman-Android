package com.android.laundrygo.model

data class CartItem(
    val id: Int,
    val title: String,
    val description: String? = null,
    val price: String
)

