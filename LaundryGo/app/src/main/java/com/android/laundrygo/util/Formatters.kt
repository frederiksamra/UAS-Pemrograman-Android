package com.android.laundrygo.util

import java.text.NumberFormat
import java.util.Locale

/**
 * Memformat angka Double menjadi String mata uang Rupiah.
 * Contoh: 50000.0 -> "Rp50.000"
 */
fun formatRupiah(amount: Double, withPrefix: Boolean = true): String {
    val localeID = Locale("in", "ID")
    val format = NumberFormat.getCurrencyInstance(localeID)
    format.maximumFractionDigits = 0
    val result = format.format(amount)
    return if (withPrefix) result else result.replace("Rp", "").trim()
}

/**
 * Versi overload untuk memformat angka Long.
 * Contoh: 50000L -> "Rp50.000"
 */
fun formatRupiah(amount: Long, withPrefix: Boolean = true): String {
    val localeID = Locale("in", "ID")
    val format = NumberFormat.getCurrencyInstance(localeID)
    format.maximumFractionDigits = 0
    val result = format.format(amount)
    return if (withPrefix) result else result.replace("Rp", "").trim()
}