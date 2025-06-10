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

// State untuk menampung semua data di layar registrasi
data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val username: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

// Event yang dikirim dari UI ke ViewModel
sealed interface RegisterUserEvent {
    data class NameChanged(val value: String) : RegisterUserEvent
    data class EmailChanged(val value: String) : RegisterUserEvent
    data class PhoneChanged(val value: String) : RegisterUserEvent
    data class AddressChanged(val value: String) : RegisterUserEvent
    data class UsernameChanged(val value: String) : RegisterUserEvent
    data class PasswordChanged(val value: String) : RegisterUserEvent
    data object TogglePasswordVisibility : RegisterUserEvent
    data object RegisterClicked : RegisterUserEvent
}

// Event yang dikirim dari ViewModel ke UI (sekali jalan)
sealed interface RegisterEvent {
    data object RegistrationSuccess : RegisterEvent
}

class RegisterViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<RegisterEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    // Satu fungsi untuk menangani semua event dari UI
    fun onEvent(event: RegisterUserEvent) {
        when (event) {
            is RegisterUserEvent.NameChanged -> _uiState.update { it.copy(name = event.value, errorMessage = null) }
            is RegisterUserEvent.EmailChanged -> _uiState.update { it.copy(email = event.value, errorMessage = null) }
            is RegisterUserEvent.PhoneChanged -> _uiState.update { it.copy(phone = event.value, errorMessage = null) }
            is RegisterUserEvent.AddressChanged -> _uiState.update { it.copy(address = event.value, errorMessage = null) }
            is RegisterUserEvent.UsernameChanged -> _uiState.update { it.copy(username = event.value, errorMessage = null) }
            is RegisterUserEvent.PasswordChanged -> _uiState.update { it.copy(password = event.value, errorMessage = null) }
            is RegisterUserEvent.TogglePasswordVisibility -> _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            is RegisterUserEvent.RegisterClicked -> handleRegistration()
        }
    }

    private fun handleRegistration() {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // Validasi sederhana
                val state = _uiState.value
                val isFormValid = state.name.isNotBlank() &&
                        state.email.isNotBlank() &&
                        state.phone.isNotBlank() &&
                        state.address.isNotBlank() &&
                        state.username.isNotBlank() &&
                        state.password.isNotBlank()

                // Simulasi proses registrasi
                delay(2000)

                if (isFormValid) {
                    // TODO: Tambahkan validasi yang lebih kompleks di sini (misal: format email, panjang password)
                    _eventFlow.emit(RegisterEvent.RegistrationSuccess)
                } else {
                    _uiState.update { it.copy(errorMessage = "Semua field harus diisi") }
                }

            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Registrasi gagal: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}