package com.android.laundrygo.model

import com.google.firebase.firestore.PropertyName

data class CartItem(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val quantity: Int = 0,
    val unit: String = "",
    val category: String = ""
)

