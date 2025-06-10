package com.android.laundrygo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// 1. Data class untuk menampung semua state yang dibutuhkan oleh UI
data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

// 2. Sealed Interface untuk event sekali jalan (seperti navigasi)
sealed interface LoginEvent {
    data object NavigateToHome : LoginEvent
    data object NavigateToForgotPassword : LoginEvent
}

class LoginViewModel : ViewModel() {

    // StateFlow untuk menampung UI State, agar UI bisa observe
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    // SharedFlow untuk event sekali jalan
    private val _eventFlow = MutableSharedFlow<LoginEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun onUsernameChange(username: String) {
        _uiState.update { it.copy(username = username, errorMessage = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    fun onTogglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onLoginClicked() {
        // Jangan proses jika sedang loading
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // Simulasi proses login di network
                delay(2000)

                val state = _uiState.value
                if (state.username.isBlank() || state.password.isBlank()) {
                    _uiState.update { it.copy(errorMessage = "Username dan password tidak boleh kosong") }
                } else if (state.username == "user" && state.password == "password") {
                    // Sukses -> kirim event untuk navigasi
                    _eventFlow.emit(LoginEvent.NavigateToHome)
                } else {
                    // Gagal
                    _uiState.update { it.copy(errorMessage = "Username atau password salah") }
                }

            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Terjadi kesalahan: ${e.message}") }
            } finally {
                // Pastikan loading dihentikan setelah selesai
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onForgotPasswordClicked() {
        viewModelScope.launch {
            // Contoh: langsung navigasi ke halaman lupa password
            _eventFlow.emit(LoginEvent.NavigateToForgotPassword)
        }
    }
}