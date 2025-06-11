package com.android.laundrygo.model

data class ServiceItem(
    val title: String,
    val desc: String? = null,
    val price: String,
    val backgroundColor: Long
)

fun parsePrice(price: String): Int {
    val regex = Regex("IDR ([0-9.]+)")
    val match = regex.find(price)
    val number = match?.groupValues?.get(1)?.replace(".", "") ?: "0"
    return number.toIntOrNull() ?: 0
}