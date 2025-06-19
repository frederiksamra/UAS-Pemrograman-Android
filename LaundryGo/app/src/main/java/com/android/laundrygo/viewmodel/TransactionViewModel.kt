package com.android.laundrygo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.laundrygo.model.CartItem
import com.android.laundrygo.model.Transaction
import com.android.laundrygo.repository.ServiceRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TransactionUiState(
    val name: String = "",
    val phone: String = "",
    val address: String = "",
    val pickupDateMillis: Long? = System.currentTimeMillis(),
    val selectedTime: String? = null,
    val totalPrice: Double = 0.0,
    val isLoading: Boolean = false,
    val isFormValid: Boolean = false,
    val error: String? = null // Properti untuk menampung pesan error
)

class TransactionViewModel(
    private val repository: ServiceRepository,
    private val totalPrice: Double
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionUiState(totalPrice = totalPrice))
    val uiState: StateFlow<TransactionUiState> = _uiState.asStateFlow()

    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    // Fungsi-fungsi untuk memperbarui state dari input pengguna
    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name) }
        validateForm()
    }

    fun onPhoneChange(phone: String) {
        _uiState.update { it.copy(phone = phone) }
        validateForm()
    }

    fun onAddressChange(address: String) {
        _uiState.update { it.copy(address = address) }
        validateForm()
    }

    fun onDateSelected(dateMillis: Long?) {
        _uiState.update { it.copy(pickupDateMillis = dateMillis) }
        validateForm()
    }

    fun onTimeSelected(time: String) {
        _uiState.update { it.copy(selectedTime = time) }
        validateForm()
    }

    // Fungsi untuk membersihkan pesan error setelah ditampilkan di UI
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun validateForm() {
        _uiState.update { currentState ->
            val isValid = with(currentState) {
                name.isNotBlank() && phone.isNotBlank() && address.isNotBlank() &&
                        pickupDateMillis != null && selectedTime != null
            }
            currentState.copy(isFormValid = isValid)
        }
    }

    // Fungsi utama yang dipanggil saat tombol checkout ditekan
    fun onCheckout(onSuccess: (String) -> Unit) { // <-- Ubah parameter menjadi (String) -> Unit
        if (userId.isNullOrEmpty()) {
            _uiState.update { it.copy(error = "User tidak valid. Silakan login ulang.") }
            return
        }

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val cartResult = repository.getCartItems(userId).first()

            cartResult.fold(
                onSuccess = { cartItems ->
                    if (cartItems.isEmpty()) {
                        _uiState.update { it.copy(isLoading = false, error = "Keranjang Anda kosong, tidak bisa checkout.") }
                        return@launch
                    }

                    val transaction = createTransactionObject(cartItems)
                    val createTxResult = repository.createTransaction(transaction)

                    createTxResult.fold(
                        onSuccess = { newTransactionId ->
                            // LOGIKA clearCart() DIHAPUS DARI SINI
                            _uiState.update { it.copy(isLoading = false) }
                            // Langsung panggil onSuccess dengan ID transaksi baru
                            onSuccess(newTransactionId)
                        },
                        onFailure = { error ->
                            _uiState.update { it.copy(isLoading = false, error = "Gagal membuat transaksi: ${error.message}") }
                        }
                    )
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, error = "Gagal memuat data keranjang: ${error.message}") }
                }
            )
        }
    }

    private fun createTransactionObject(cartItems: List<CartItem>): Transaction {
        val currentState = _uiState.value
        val formattedDate = currentState.pickupDateMillis?.let {
            SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID")).format(Date(it))
        } ?: ""

        return Transaction(
            userId = userId!!,
            customerName = currentState.name,
            customerPhone = currentState.phone,
            customerAddress = currentState.address,
            pickupDate = formattedDate,
            pickupTime = currentState.selectedTime ?: "",
            items = cartItems,
            totalPrice = currentState.totalPrice
        )
    }

    // Factory untuk membuat ViewModel ini dengan dependensinya
    companion object {
        fun provideFactory(repository: ServiceRepository, totalPrice: Double): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
                        return TransactionViewModel(repository, totalPrice) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }
}