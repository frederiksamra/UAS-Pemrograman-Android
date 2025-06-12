// In your TransactionViewModel.kt file
package com.android.laundrygo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TransactionUiState(
    val name: String = "",
    val phone: String = "",
    val address: String = "",
    val pickupDateMillis: Long? = null,
    val selectedTime: String? = null,
    val totalPrice: Double = 0.0,
    val isLoading: Boolean = false,
    val isFormValid: Boolean = false
)

class TransactionViewModel(
    // totalPrice sekarang bersifat private karena hanya digunakan untuk inisialisasi
    private val totalPrice: Double
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        TransactionUiState(totalPrice = totalPrice)
    )
    val uiState: StateFlow<TransactionUiState> = _uiState.asStateFlow()

    fun onNameChange(name: String) {
        // Menggunakan update untuk pembaruan state yang aman (atomic)
        _uiState.update { currentState ->
            val updatedState = currentState.copy(name = name)
            updatedState.copy(isFormValid = isFormStateValid(updatedState))
        }
    }

    fun onPhoneChange(phone: String) {
        _uiState.update { currentState ->
            val updatedState = currentState.copy(phone = phone)
            updatedState.copy(isFormValid = isFormStateValid(updatedState))
        }
    }

    fun onAddressChange(address: String) {
        _uiState.update { currentState ->
            val updatedState = currentState.copy(address = address)
            updatedState.copy(isFormValid = isFormStateValid(updatedState))
        }
    }

    fun onDateSelected(dateMillis: Long?) {
        _uiState.update { currentState ->
            val updatedState = currentState.copy(pickupDateMillis = dateMillis)
            updatedState.copy(isFormValid = isFormStateValid(updatedState))
        }
    }

    fun onTimeSelected(time: String) {
        _uiState.update { currentState ->
            val updatedState = currentState.copy(selectedTime = time)
            updatedState.copy(isFormValid = isFormStateValid(updatedState))
        }
    }

    fun onCheckout(onSuccess: () -> Unit) {
        _uiState.update { it.copy(isLoading = true) }

        // ✅ PERBAIKAN: Menggunakan viewModelScope, bukan GlobalScope.
        // Ini memastikan coroutine akan otomatis dibatalkan jika ViewModel hancur,
        // mencegah memory leak dan pekerjaan yang tidak perlu.
        viewModelScope.launch {
            delay(2000) // Simulasi panggilan jaringan/database
            _uiState.update { it.copy(isLoading = false) }
            onSuccess()
        }
    }

    // Fungsi validasi dibuat private dan hanya mengembalikan boolean
    // Logika validasi ditempatkan di satu tempat agar mudah dikelola.
    private fun isFormStateValid(state: TransactionUiState): Boolean {
        // 'with' membuat kode lebih ringkas
        return with(state) {
            name.isNotBlank() &&
                    phone.isNotBlank() &&
                    address.isNotBlank() &&
                    pickupDateMillis != null &&
                    selectedTime != null
        }
    }

    // Factory tidak perlu diubah, sudah menggunakan pola yang benar.
    companion object {
        fun provideFactory(totalPrice: Double): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
                        return TransactionViewModel(totalPrice = totalPrice) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            }
        }
    }
}