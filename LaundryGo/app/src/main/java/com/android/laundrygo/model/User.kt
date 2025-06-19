package com.android.laundrygo.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class User(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val username: String = "",
    val balance: Double = 0.0,
    @ServerTimestamp val createdAt: Timestamp? = null
)