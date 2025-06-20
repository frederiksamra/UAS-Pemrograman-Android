package com.android.laundrygo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.laundrygo.model.User
import com.android.laundrygo.model.Voucher
import com.android.laundrygo.repository.AuthRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.NumberFormat
import java.util.*

class DashboardViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    private val _userBalance = MutableLiveData<String>()
    val userBalance: LiveData<String> = _userBalance

    private val _vouchers = MutableLiveData<List<Voucher>>()
    val vouchers: LiveData<List<Voucher>> = _vouchers

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        fetchUserData()
        loadVouchers()
    }

    fun fetchUserData() {
        viewModelScope.launch {
            val result = authRepository.getUserProfile()
            result.onSuccess { user -> // 'user' di sini sekarang bertipe User? (bisa null)

                // --- PERBAIKAN DI SINI: Lakukan null check ---
                if (user != null) {
                    // Jika user tidak null, kita aman mengakses propertinya
                    _user.value = user
                    _userName.value = user.name.ifEmpty { "User" }
                    _userBalance.value = formatCurrency(user.balance)
                    _error.value = null
                } else {
                    // Jika user null (misal, dokumen tidak ada di firestore atau user belum login)
                    // kita tangani sebagai sebuah kondisi error atau state kosong.
                    _user.value = null
                    _userName.value = "User"
                    _userBalance.value = "Rp 0"
                    _error.value = "Gagal menemukan profil pengguna."
                }

            }.onFailure { exception ->
                _user.value = null
                _userName.value = "User"
                _userBalance.value = "Rp 0"
                _error.value = "Gagal memuat data: ${exception.message}"
            }
        }
    }

    private fun loadVouchers() {
        viewModelScope.launch {
            try {
                val now = com.google.firebase.Timestamp.now()
                val snapshot = db.collection("vouchers")
                    .whereEqualTo("is_active", true)
                    .get()
                    .await()

                val list = snapshot.documents.mapNotNull { doc ->
                    doc.toObject<Voucher>()?.copy(documentId = doc.id)
                }.filter { voucher ->
                    val validFromOk = voucher.valid_from == null || voucher.valid_from <= now
                    val validUntilOk = voucher.valid_until == null || voucher.valid_until >= now
                    validFromOk && validUntilOk
                }
                _vouchers.value = list
            } catch (e: Exception) {
                _vouchers.value = emptyList()
                _error.value = "Gagal memuat voucher: ${e.message}"
            }
        }
    }

    fun claimVoucher(voucherId: String) {
        viewModelScope.launch {
            // Kita asumsikan authRepository punya fungsi claimVoucher
            // Jika tidak, logika ini perlu disesuaikan dengan repository Anda
            val voucherToClaim = vouchers.value?.firstOrNull { it.documentId == voucherId }
            if (voucherToClaim != null) {
                authRepository.claimVoucher(voucherToClaim).onFailure { exception ->
                    _error.value = "Gagal klaim voucher: ${exception.message}"
                }
                // loadVouchers() tidak perlu dipanggil di sini jika Anda ingin voucher hilang dari UI secara visual
                // Atau panggil untuk refresh dari server
            }
        }
    }

    private fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        format.maximumFractionDigits = 0
        return format.format(amount)
    }

    fun refreshDashboardData() {
        fetchUserData()
        loadVouchers()
    }

    companion object {
        fun provideFactory(repository: AuthRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
                        return DashboardViewModel(repository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }
}