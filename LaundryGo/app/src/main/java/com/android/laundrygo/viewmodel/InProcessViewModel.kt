package com.android.laundrygo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class InProcessViewModel : ViewModel() {

    // Menyimpan index tab yang dipilih: 0 = Pick Up, 1 = Washing, 2 = Washed, 3 = Delivery
    private val _selectedIndex = MutableStateFlow(0)
    val selectedIndex: StateFlow<Int> get() = _selectedIndex

    // Status loading (misalnya saat mengambil data dari server)
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    // Pesan error jika ada
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> get() = _errorMessage

    /**
     * Memilih tab berdasarkan index
     */
    fun selectTab(index: Int) {
        _selectedIndex.value = index
    }

    /**
     * Contoh simulasi pengambilan status proses dari backend
     */
    fun fetchProcessStatus() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                // Simulasi loading selama 1 detik
                delay(1000)

                // TODO: Ganti ini dengan pemanggilan API backend jika sudah tersedia
                // Simulasi respons: ubah tab ke Pick Up (atau apapun sesuai backend)
                _selectedIndex.value = 0

            } catch (e: Exception) {
                _errorMessage.value = "Gagal memuat status: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
