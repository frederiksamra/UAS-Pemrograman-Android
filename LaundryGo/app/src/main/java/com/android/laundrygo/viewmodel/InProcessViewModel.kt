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

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _inProcessTransactions = MutableStateFlow<List<Transaction>>(emptyList())
    val inProcessTransactions: StateFlow<List<Transaction>> = _inProcessTransactions.asStateFlow()

    private val _selectedTransaction = MutableStateFlow<Transaction?>(null)
    val selectedTransaction: StateFlow<Transaction?> = _selectedTransaction.asStateFlow()

    init {
        fetchInProcessTransactions()
    }

    /**
     * Memperbarui transaksi yang sedang dipilih oleh UI.
     */
    fun selectTransaction(transaction: Transaction) {
        _selectedTransaction.value = transaction
    }

    /**
     * Mengambil dan memfilter transaksi yang sedang berjalan dari Firestore.
     * Versi ini sudah dikoreksi untuk bekerja dengan repository yang mengembalikan Flow<Result<T>>.
     */
    fun fetchInProcessTransactions() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId.isNullOrEmpty()) {
            _errorMessage.value = "Pengguna tidak terautentikasi."
            _isLoading.value = false
            return
        }

        viewModelScope.launch {
            _isLoading.value = true

            repository.getTransactionsForUser(userId)
                .onEach { result ->
                    // Menggunakan fold untuk menangani Result.success atau Result.failure dengan aman
                    result.fold(
                        onSuccess = { transactions ->
                            // 1. Logika filter yang benar, diterapkan pada List<Transaction>
                            val filteredTransactions = transactions.filter { transaction ->
                                transaction.status >= 2 && transaction.status < 7 // Status Lunas (2) s/d Delivery (6)
                            }
                            _inProcessTransactions.value = filteredTransactions
                            _errorMessage.value = null // Hapus pesan error lama jika berhasil

                            // 2. Logika untuk selectedTransaction yang konsisten
                            val currentSelected = _selectedTransaction.value
                            if (currentSelected == null || !filteredTransactions.any { it.id == currentSelected.id }) {
                                _selectedTransaction.value = filteredTransactions.firstOrNull()
                            }
                        },
                        onFailure = { exception ->
                            // 3. Menangani kegagalan dari Result
                            _errorMessage.value = "Gagal memuat transaksi: ${exception.message}"
                        }
                    )
                }
                .onCompletion {
                    // 4. onCompletion akan dipanggil setelah Flow selesai, baik berhasil maupun gagal.
                    // Ini memastikan loading indicator selalu dimatikan.
                    _isLoading.value = false
                }
                .launchIn(viewModelScope)
        }
    }

    // Factory untuk InProcessViewModel (sudah benar, tidak perlu diubah)
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