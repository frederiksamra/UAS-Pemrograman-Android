package com.android.laundrygo.model

import com.google.firebase.firestore.DocumentId

data class LaundryService(
    @DocumentId val id: String = "", // Untuk menyimpan ID dokumen dari Firestore
    val title: String = "",
    val description: String = "",
    val price: Long = 0,
    val unit: String = "",
    val category: String = ""
)