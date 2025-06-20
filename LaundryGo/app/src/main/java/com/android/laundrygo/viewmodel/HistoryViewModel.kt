package com.android.laundrygo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.laundrygo.model.Transaction
import com.android.laundrygo.repository.ServiceRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

// 1. Definisikan UiState untuk menampung semua state layar History
data class HistoryUiState(
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

// 2. ViewModel sekarang bergantung pada ServiceRepository
class HistoryViewModel(private val repository: ServiceRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState = _uiState.asStateFlow()

    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    init {
        loadUserHistory()
    }

    private fun loadUserHistory() {
        if (userId.isNullOrEmpty()) {
            _uiState.update { it.copy(isLoading = false, error = "User tidak login.") }
            return
        }

        // 3. Ambil data transaksi nyata dari repository
        repository.getTransactionsForUser(userId)
            .onEach { result ->
                result.fold(
                    onSuccess = { transactions ->
                        // Jika sukses, perbarui state dengan daftar transaksi
                        _uiState.update { it.copy(isLoading = false, transactions = transactions, error = null) }
                    },
                    onFailure = { exception ->
                        // Jika gagal, isi pesan error
                        _uiState.update { it.copy(isLoading = false, error = exception.message) }
                    }
                )
            }
            .launchIn(viewModelScope)
    }
}

// 4. Buat Factory untuk menyediakan dependensi ServiceRepository
class HistoryViewModelFactory(private val repository: ServiceRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            return HistoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}