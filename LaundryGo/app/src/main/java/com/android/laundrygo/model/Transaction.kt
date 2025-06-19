package com.android.laundrygo.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Transaction(
    @DocumentId
    val id: String = "",

    val userId: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val customerAddress: String = "",

    val pickupDate: String = "",
    val pickupTime: String = "",

    val items: List<CartItem> = emptyList(),
    val totalPrice: Double = 0.0,

    var status: String = "Menunggu Pembayaran",

    @ServerTimestamp
    val createdAt: Date? = null
)