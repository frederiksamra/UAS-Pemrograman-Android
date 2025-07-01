package com.android.laundrygo.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.laundrygo.model.User
import com.android.laundrygo.model.Voucher
import com.android.laundrygo.repository.AuthRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.NumberFormat
import java.util.*
import kotlinx.coroutines.flow.update

data class DashboardUiState(
    val user: User? = null,
    val userName: String = "User",
    val userBalance: String = "Rp 0",
    val vouchers: List<Voucher> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class DashboardViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    // State biometrik
    private val _biometricAuthState = MutableStateFlow(BiometricAuthState.IDLE)
    val biometricAuthState = _biometricAuthState.asStateFlow()

    init {
        observeUserProfile()
        loadPublicVouchers()
    }

    private fun observeUserProfile() {
        viewModelScope.launch {
            authRepository.getCurrentUserProfile()
                .onEach { result ->
                    result.fold(
                        onSuccess = { user ->
                            if (user != null) {
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        user = user,
                                        userName = user.name.split(" ").firstOrNull() ?: user.name,
                                        userBalance = formatCurrency(user.balance),
                                        error = null
                                    )
                                }
                            } else {
                                _uiState.update { it.copy(isLoading = false, error = "Profil pengguna tidak ditemukan.") }
                            }
                        },
                        onFailure = { exception ->
                            _uiState.update { it.copy(isLoading = false, error = exception.message) }
                        }
                    )
                }.launchIn(this)
        }
    }

    private fun loadPublicVouchers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val now = com.google.firebase.Timestamp.now()
                val publicVouchersSnapshot = db.collection("vouchers")
                    .whereEqualTo("is_active", true)
                    .get()
                    .await()

                val publicVouchersList = publicVouchersSnapshot.documents.mapNotNull { doc ->
                    doc.toObject<Voucher>()?.copy(voucherDocumentId = doc.id)
                }.filter { voucher ->
                    val validFromOk = voucher.valid_from == null || voucher.valid_from <= now
                    val validUntilOk = voucher.valid_until == null || voucher.valid_until >= now
                    validFromOk && validUntilOk
                }

                authRepository.getMyVouchers()
                    .take(1)
                    .collect { claimedVouchersResult ->
                        claimedVouchersResult.fold(
                            onSuccess = { claimedVouchers ->
                                val claimedVoucherIds = claimedVouchers.map { it.voucherDocumentId }.toSet()
                                val filteredPublicVouchers = publicVouchersList.filter { it.voucherDocumentId !in claimedVoucherIds }
                                _uiState.update { it.copy(vouchers = filteredPublicVouchers, isLoading = false) }
                                Log.d("VoucherDebug", "Jumlah voucher publik: ${publicVouchersList.size}, Diklaim: ${claimedVouchers.size}, Ditampilkan: ${filteredPublicVouchers.size}")
                            },
                            onFailure = { e ->
                                _uiState.update { it.copy(error = "Gagal memuat voucher yang sudah diklaim: ${e.message}", isLoading = false) }
                                Log.e("VoucherDebug", "Error loading claimed vouchers", e)
                                _uiState.update { it.copy(vouchers = publicVouchersList, isLoading = false) }
                            }
                        )
                    }

            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Gagal memuat voucher: ${e.message}", isLoading = false) }
                Log.e("VoucherDebug", "Error loading public vouchers", e)
            }
        }
    }

    fun claimVoucher(voucherId: String) {
        viewModelScope.launch {
            val voucherToClaim = _uiState.value.vouchers.firstOrNull { it.voucherDocumentId == voucherId }
            if (voucherToClaim != null) {
                authRepository.claimVoucher(voucherToClaim).onSuccess {
                    // kita langsung perbarui state di aplikasi.

                    // 1. Ambil daftar voucher yang ada saat ini di UI.
                    val currentVouchers = _uiState.value.vouchers

                    // 2. Buat daftar baru dengan membuang voucher yang baru saja diklaim.
                    val updatedVouchers = currentVouchers.filterNot { it.voucherDocumentId == voucherId }

                    // 3. Update UI dengan daftar yang sudah diperbarui.
                    _uiState.update { it.copy(vouchers = updatedVouchers) }

                    // Ini lebih cepat, efisien, dan tidak akan mengalami race condition.

                }.onFailure { exception ->
                    _uiState.update { it.copy(error = "Gagal klaim voucher: ${exception.message}") }
                }
            }
        }
    }

    fun requestBiometricAuth() {
        _biometricAuthState.value = BiometricAuthState.AUTHENTICATING
    }

    fun onBiometricAuthSuccess() {
        _biometricAuthState.value = BiometricAuthState.SUCCESS
    }

    fun onBiometricAuthFailed(isError: Boolean = false, message: String = "") {
        if (isError) {
            _biometricAuthState.value = BiometricAuthState.ERROR
        } else {
            _biometricAuthState.value = BiometricAuthState.FAILED
        }
    }

    fun setBiometricNotAvailable() {
        _biometricAuthState.value = BiometricAuthState.NOT_AVAILABLE
    }

    private fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        format.maximumFractionDigits = 0
        return format.format(amount)
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
