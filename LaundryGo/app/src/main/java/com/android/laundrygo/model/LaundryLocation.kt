package com.android.laundrygo.model

data class LaundryLocation(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val country: String,
    val imageUrl: String? = null
)