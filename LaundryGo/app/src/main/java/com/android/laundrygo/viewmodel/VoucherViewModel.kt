package com.android.laundrygo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.laundrygo.ui.screens.VoucherInfo // Pastikan path import benar
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class VoucherViewModel : ViewModel() {

    // State untuk menampung daftar voucher. Hanya bisa diubah di dalam ViewModel.
    private val _vouchers = MutableStateFlow<List<VoucherInfo>>(emptyList())
    // State yang akan diobservasi oleh UI (read-only).
    val vouchers: StateFlow<List<VoucherInfo>> = _vouchers.asStateFlow()

    // State untuk menampilkan loading indicator.
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // State untuk menampilkan pesan error.
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Channel untuk mengirim event sekali jalan ke UI (misal: notifikasi).
    private val _claimStatus = MutableSharedFlow<String>()
    val claimStatus: SharedFlow<String> = _claimStatus.asSharedFlow()

    init {
        // Panggil fungsi untuk mengambil data saat ViewModel pertama kali dibuat.
        fetchVouchers()
    }

    private fun fetchVouchers() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // Simulasi pemanggilan API atau database dengan delay 2 detik.
                delay(2000)

                // Data dummy sebagai hasil dari "API call".
                val result = listOf(
                    VoucherInfo(1, "10% off entire order", "Monday, 9 March 2024"),
                    VoucherInfo(2, "Gratis Ongkir", "Tuesday, 10 March 2024"),
                    VoucherInfo(3, "Potongan Rp15.000", "Wednesday, 11 March 2024"),
                    VoucherInfo(4, "Cashback 5.000 Poin", "Friday, 13 March 2024")
                )
                _vouchers.value = result

            } catch (e: Exception) {
                // Jika terjadi error saat pengambilan data.
                _error.value = "Gagal memuat voucher. Silakan coba lagi."
            } finally {
                // Hentikan loading indicator setelah selesai.
                _isLoading.value = false
            }
        }
    }

    fun claimVoucher(voucherId: Int) {
        viewModelScope.launch {
            // Simulasi proses klaim ke server.
            println("Mengklaim voucher dengan ID: $voucherId...")
            delay(1000)

            // Logika setelah berhasil klaim:
            // 1. Hapus voucher dari daftar.
            _vouchers.update { currentList ->
                currentList.filterNot { it.id == voucherId }
            }

            // 2. Kirim notifikasi sukses ke UI.
            _claimStatus.emit("Voucher berhasil diklaim!")
        }
    }
}