package com.android.laundrygo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.laundrygo.model.Voucher
import com.android.laundrygo.repository.AuthRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class VoucherManagementViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _vouchers = MutableStateFlow<List<Voucher>>(emptyList())
    val vouchers: StateFlow<List<Voucher>> = _vouchers.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    init {
        loadVouchers()
    }

    private fun loadVouchers() {
        _isLoading.update { true }
        viewModelScope.launch {
            authRepository.getAllVouchers().collect { result ->
                result.onSuccess { vouchersList ->

                    // 1. Dapatkan tanggal hari ini pada jam 00:00:00
                    val today = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.time

                    // 2. Saring daftar voucher
                    val activeVouchers = vouchersList.filter { voucher ->
                        // Tampilkan voucher jika:
                        // - Tanggal kedaluwarsanya tidak ada (berlaku selamanya), ATAU
                        // - Tanggal kedaluwarsanya BUKAN sebelum hari ini.
                        voucher.valid_until == null || !voucher.valid_until.toDate().before(today)
                    }

                    // 3. Update UI dengan daftar voucher yang sudah difilter
                    _vouchers.update { activeVouchers }

                }.onFailure { error ->
                    _feedbackMessage.update { "Gagal memuat voucher: ${error.message}" }
                }
                _isLoading.update { false }
            }
        }
    }

    fun addVoucher(
        code: String,
        description: String,
        discountValue: Double,
        discountType: String,
        expiryDate: Date?
    ) {
        if (code.isBlank() || description.isBlank() || discountValue <= 0) {
            _feedbackMessage.update { "Harap isi semua kolom dengan benar." }
            return
        }

        _isLoading.update { true }
        viewModelScope.launch {
            val expiryTimestamp = expiryDate?.let { Timestamp(it) }
            val result = authRepository.addVoucher(
                voucherCode = code,
                description = description,
                discountValue = discountValue,
                discountType = discountType,
                expiryDate = expiryTimestamp
            )
            result.onSuccess {
                _feedbackMessage.update { "Voucher berhasil ditambahkan" }
                loadVouchers()
            }.onFailure { error ->
                _feedbackMessage.update { "Gagal menambahkan voucher: ${error.message}" }
            }
            _isLoading.update { false }
        }
    }

    fun clearFeedbackMessage() {
        _feedbackMessage.update { null }
    }
}

class VoucherManagementViewModelFactory(private val authRepository: AuthRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VoucherManagementViewModel::class.java)) {
            return VoucherManagementViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
