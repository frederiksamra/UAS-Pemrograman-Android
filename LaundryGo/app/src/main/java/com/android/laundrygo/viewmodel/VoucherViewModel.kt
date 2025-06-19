package com.android.laundrygo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.laundrygo.model.Voucher
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class VoucherViewModel : ViewModel() {

    private val db = Firebase.firestore

    private val _vouchers = MutableStateFlow<List<Voucher>>(emptyList())
    val vouchers: StateFlow<List<Voucher>> = _vouchers.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _claimStatus = MutableSharedFlow<String>()
    val claimStatus: SharedFlow<String> = _claimStatus.asSharedFlow()

    init {
        fetchVouchers()
    }

    fun fetchVouchers() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            db.collection("vouchers")
                .whereEqualTo("is_active", true)
                .get()
                .addOnSuccessListener { result ->
                    val now = com.google.firebase.Timestamp.now()
                    val voucherList = result.mapNotNull { doc ->
                        val voucher = doc.toObject(Voucher::class.java).copy(documentId = doc.id)
                        if ((voucher.valid_from == null || now >= voucher.valid_from) &&
                            (voucher.valid_until == null || now <= voucher.valid_until)) {
                            voucher
                        } else null
                    }
                    _vouchers.value = voucherList
                    _isLoading.value = false
                }
                .addOnFailureListener {
                    _error.value = it.message
                    _isLoading.value = false
                }
        }
    }

    fun claimVoucher(voucherId: String) {
        viewModelScope.launch {
            _vouchers.update { current ->
                current.filterNot { it.documentId == voucherId }
            }
            _claimStatus.emit("Voucher berhasil diklaim!")
        }
    }

    fun formatDate(timestamp: com.google.firebase.Timestamp?): String {
        if (timestamp == null) return "-"
        val sdf = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
        return sdf.format(timestamp.toDate())
    }
}
