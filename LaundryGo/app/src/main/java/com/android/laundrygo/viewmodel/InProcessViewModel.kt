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


    fun selectTransaction(transaction: Transaction) {
        _selectedTransaction.value = transaction
    }


    fun fetchInProcessTransactions() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId.isNullOrEmpty()) {
            _errorMessage.value = "Pengguna belum login."
            _isLoading.value = false
            return
        }

        viewModelScope.launch {
            _isLoading.value = true

            repository.getTransactionsForUser(userId)
                .onEach { result ->
                    result.fold(
                        onSuccess = { transactions ->
                            val filtered = transactions.filter { it.status in 2..6 }
                            _inProcessTransactions.value = filtered
                            _errorMessage.value = null

                            val currentSelected = _selectedTransaction.value
                            if (currentSelected == null || filtered.none { it.id == currentSelected.id }) {
                                _selectedTransaction.value = null
                            }
                        },
                        onFailure = { exception ->
                            _errorMessage.value = "Gagal memuat transaksi: ${exception.message}"
                        }
                    )
                }
                .onCompletion {
                    _isLoading.value = false
                }
                .launchIn(viewModelScope)
        }
    }

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
