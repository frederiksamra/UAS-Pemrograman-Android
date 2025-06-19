package com.android.laundrygo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.laundrygo.model.Transaction
import com.android.laundrygo.repository.ServiceRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class InProcessViewModel(private val repository: ServiceRepository) : ViewModel() {

    private val _selectedIndex = MutableStateFlow(0)
    val selectedIndex: StateFlow<Int> get() = _selectedIndex

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> get() = _errorMessage

    // State untuk menampung transaksi yang sedang berjalan
    private val _activeTransaction = MutableStateFlow<Transaction?>(null)
    val activeTransaction: StateFlow<Transaction?> get() = _activeTransaction

    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    init {
        // Saat ViewModel dibuat, langsung cari transaksi yang sedang diproses
        fetchInProcessTransaction()
    }

    fun selectTab(index: Int) {
        _selectedIndex.value = index
    }

    /**
     * Mengambil data transaksi yang sedang dalam proses dari Firestore
     */
    fun fetchInProcessTransaction() {
        if (userId.isNullOrEmpty()) {
            _errorMessage.value = "User tidak login."
            _isLoading.value = false
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            // Mengambil semua transaksi user
            repository.getTransactionsForUser(userId)
                .onEach { result ->
                    result.fold(
                        onSuccess = { transactions ->
                            // Cari transaksi pertama yang statusnya bukan "Lunas" atau "Selesai"
                            val inProcessTx = transactions.firstOrNull {
                                it.status != "Lunas" && it.status != "Selesai" // Sesuaikan dengan status Anda
                            }
                            _activeTransaction.value = inProcessTx
                            updateTabBasedOnStatus(inProcessTx?.status)
                            _isLoading.value = false
                        },
                        onFailure = {
                            _errorMessage.value = "Gagal memuat status: ${it.message}"
                            _isLoading.value = false
                        }
                    )
                }.launchIn(this)
        }
    }

    private fun updateTabBasedOnStatus(status: String?) {
        _selectedIndex.value = when (status) {
            "Menunggu Penjemputan" -> 0 // Pick Up
            "Dicuci" -> 1 // Washing
            "Selesai Dicuci" -> 2 // Washed
            "Dalam Pengantaran" -> 3 // Delivery
            else -> 0 // Default ke tab pertama jika tidak ada atau status tidak dikenali
        }
    }

    // Factory untuk InProcessViewModel
    companion object {
        fun provideFactory(repository: ServiceRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(InProcessViewModel::class.java)) {
                        return InProcessViewModel(repository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }
}