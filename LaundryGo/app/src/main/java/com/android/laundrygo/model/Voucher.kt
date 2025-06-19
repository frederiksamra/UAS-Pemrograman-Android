package com.android.laundrygo.model

import com.google.firebase.Timestamp

data class Voucher(
    val voucher_code: String = "",
    val discount_type: String = "fixed", // hanya "fixed"
    val discount_value: Int = 0,
    val is_active: Boolean = true,
    val valid_from: Timestamp? = null,
    val valid_until: Timestamp? = null,
    val documentId: String = "" // untuk menyimpan Firestore ID (opsional)
)
