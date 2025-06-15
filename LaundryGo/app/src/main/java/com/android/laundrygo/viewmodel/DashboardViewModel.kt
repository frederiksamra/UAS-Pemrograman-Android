package com.android.laundrygo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.laundrygo.model.User
import com.android.laundrygo.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.NumberFormat
import java.util.Locale

class DashboardViewModel(private val authRepository: AuthRepository) : ViewModel() {

    // BARU: Tambahkan LiveData untuk menampung seluruh objek User
    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    // LiveData yang sudah ada untuk kemudahan akses di UI
    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    private val _userBalance = MutableLiveData<String>()
    val userBalance: LiveData<String> = _userBalance

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        fetchUserData()
    }

    fun fetchUserData() {
        viewModelScope.launch {
            val result = authRepository.getUserProfile()

            result.onSuccess { user ->
                // DIUBAH: Update semua LiveData saat data berhasil didapat
                _user.value = user
                _userName.value = user.name.ifEmpty { "User" }
                // Format saldo dari Long ke String Rupiah dan update LiveData
                _userBalance.value = formatCurrency(user.balance)
                _error.value = null

            }.onFailure { exception ->
                _user.value = null
                _error.value = "Gagal memuat data: ${exception.message}"
                _userName.value = "User"
                _userBalance.value = "0"
            }
        }
    }

    // Fungsi helper untuk format mata uang
    private fun formatCurrency(amount: Long): String {
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        format.maximumFractionDigits = 0
        // Hapus 'Rp' standar dan ganti dengan format yang kita mau, misal "1.500.000"
        return format.format(amount).replace("Rp", "").trim()
    }

    // Navigation methods
    fun navigateToNearestLocation() {
        // Implement navigation logic to maps or location screen
    }

    fun openTopUpScreen() {
        // Implement top-up screen navigation or dialog
    }

    // Method to claim a voucher
    fun claimVoucher(voucherId: String) {
        viewModelScope.launch {
            try {
                // Implement voucher claiming logic
                // This could involve updating Firestore document or calling an API
            } catch (e: Exception) {
                _error.value = "Failed to claim voucher: ${e.message}"
            }
        }
    }

    // Method to refresh dashboard data
    fun refreshDashboardData() {
        fetchUserData()
    }

    companion object {
        fun provideFactory(
            repository: AuthRepository,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
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

