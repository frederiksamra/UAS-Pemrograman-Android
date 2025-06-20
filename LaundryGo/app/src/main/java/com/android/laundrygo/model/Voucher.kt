package com.android.laundrygo.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import kotlinx.parcelize.Parcelize

@Parcelize
data class Voucher(
    // Anotasi @DocumentId tetap ada untuk MEMBACA ID
    @DocumentId
    val voucherDocumentId: String = "",

    // Properti lain tetap sama
    val voucher_code: String = "",
    val discount_type: String = "fixed",
    val discount_value: Double = 0.0,

    // Anotasi @get:Exclude akan MENCEGAH field ini DISIMPAN ke Firestore
    @get:Exclude
    var is_active: Boolean = true,

    val valid_from: Timestamp? = null,
    val valid_until: Timestamp? = null,
    val is_used: Boolean = false
) : Parcelable