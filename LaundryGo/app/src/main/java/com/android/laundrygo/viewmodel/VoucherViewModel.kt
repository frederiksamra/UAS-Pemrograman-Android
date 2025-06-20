package com.android.laundrygo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.laundrygo.model.Voucher
import com.android.laundrygo.repository.AuthRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// Definisikan UiState untuk VoucherScreen
data class VoucherUiState(
    val vouchers: List<Voucher> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class VoucherViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(VoucherUiState())
    val uiState: StateFlow<VoucherUiState> = _uiState.asStateFlow()

    init {
        // Mengambil voucher PRIBADI milik user
        fetchMyVouchers()
    }

    private fun fetchMyVouchers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            authRepository.getMyVouchers()
                .onEach { result ->
                    result.fold(
                        onSuccess = { myVouchers ->
                            _uiState.update { it.copy(isLoading = false, vouchers = myVouchers, error = null) }
                        },
                        onFailure = { exception ->
                            _uiState.update { it.copy(isLoading = false, error = exception.message) }
                        }
                    )
                }.launchIn(this)
        }
    }

    // Fungsi ini adalah placeholder untuk masa depan,
    // misal saat voucher akan diterapkan di keranjang.
    fun onVoucherUsed(voucherId: String) {
        // TODO: Implementasi logika penggunaan voucher, misal menandai 'is_used = true' di Firestore
        // Untuk sekarang, kita bisa tampilkan pesan singkat
        viewModelScope.launch {
            // Logika untuk menggunakan voucher
        }
    }

    fun formatDate(timestamp: com.google.firebase.Timestamp?): String {
        if (timestamp == null) return "Tidak ada batas waktu"
        val sdf = SimpleDateFormat("d MMMM yyyy", Locale("id", "ID"))
        return sdf.format(timestamp.toDate())
    }

    // Factory untuk ViewModel ini
    companion object {
        fun provideFactory(repository: AuthRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(VoucherViewModel::class.java)) {
                        return VoucherViewModel(repository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }
}