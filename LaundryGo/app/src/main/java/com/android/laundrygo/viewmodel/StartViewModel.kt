package com.android.laundrygo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

// Sealed Interface untuk merepresentasikan event navigasi yang mungkin terjadi
sealed interface StartNavigation {
    data object ToLogin : StartNavigation
    data object ToRegister : StartNavigation
}

class StartViewModel : ViewModel() {

    // SharedFlow lebih baik untuk event sekali jalan (seperti navigasi)
    private val _navigationEvent = MutableSharedFlow<StartNavigation>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    /**
     * Dipanggil saat tombol Login ditekan.
     */
    fun onLoginClicked() {
        viewModelScope.launch {
            // Mengirim event untuk navigasi ke halaman Login
            _navigationEvent.emit(StartNavigation.ToLogin)
        }
    }

    /**
     * Dipanggil saat tombol Register ditekan.
     */
    fun onRegisterClicked() {
        viewModelScope.launch {
            // Mengirim event untuk navigasi ke halaman Register
            _navigationEvent.emit(StartNavigation.ToRegister)
        }
    }
}