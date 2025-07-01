package com.android.laundrygo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.laundrygo.navigation.AdminScreen
import com.android.laundrygo.repository.AuthRepository
import com.android.laundrygo.repository.AuthRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// 1. Definisikan state untuk biometrik
enum class BiometricAuthState {
    IDLE, // Belum melakukan apa-apa
    AUTHENTICATING, // Sedang menunggu otentikasi
    SUCCESS, // Berhasil
    FAILED, // Gagal (misal, sidik jari tidak cocok)
    ERROR, // Error (misal, hardware tidak tersedia)
    NOT_AVAILABLE // Fitur tidak tersedia di perangkat
}

class AdminMainViewModel(private val authRepository: AuthRepository) : ViewModel() {
    val navigationItems = listOf(
        AdminScreen.Users,
        AdminScreen.Vouchers,
        AdminScreen.Orders,
    )

    private val _title = MutableStateFlow(navigationItems.first().label)
    val title = _title.asStateFlow()

    // 2. Tambahkan state biometrik ke ViewModel
    private val _biometricAuthState = MutableStateFlow(BiometricAuthState.IDLE)
    val biometricAuthState = _biometricAuthState.asStateFlow()

    fun onRouteChanged(route: String?) {
        val newScreen = navigationItems.find { it.route == route }
        if (newScreen != null) {
            _title.value = newScreen.label
        }
    }

    fun logout() {
        authRepository.logout()
    }

    // 3. Tambahkan fungsi untuk mengontrol alur biometrik
    fun requestBiometricAuth() {
        _biometricAuthState.value = BiometricAuthState.AUTHENTICATING
    }

    fun onBiometricAuthSuccess() {
        _biometricAuthState.value = BiometricAuthState.SUCCESS
    }

    fun onBiometricAuthFailed(isError: Boolean = false, message: String = "") {
        if (isError) {
            _biometricAuthState.value = BiometricAuthState.ERROR
        } else {
            _biometricAuthState.value = BiometricAuthState.FAILED
        }
    }

    fun setBiometricNotAvailable() {
        _biometricAuthState.value = BiometricAuthState.NOT_AVAILABLE
    }
}

class AdminMainViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminMainViewModel::class.java)) {
            return AdminMainViewModel(AuthRepositoryImpl()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
