package com.android.laundrygo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.laundrygo.model.User
import com.android.laundrygo.repository.AuthRepository
import com.android.laundrygo.repository.AuthRepositoryImpl
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

sealed interface RegisterEvent {
    data object RegistrationSuccess : RegisterEvent
}

// ViewModel sekarang menerima AuthRepository
class RegisterViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<RegisterEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

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

            val state = _uiState.value
            val userProfile = User(
                name = state.name.trim(),
                email = state.email.trim(),
                phone = state.phone.trim(),
                address = state.address.trim(),
                username = state.username.trim()
            )

            // Memanggil fungsi registrasi di repository
            val result = repository.registerUser(
                email = state.email.trim(),
                password = state.password,
                userProfile = userProfile
            )

            // Menangani hasil dari repository
            result.onSuccess {
                _eventFlow.emit(RegisterEvent.RegistrationSuccess)
            }.onFailure { exception ->
                _uiState.update { it.copy(errorMessage = exception.message ?: "Registrasi gagal") }
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }
}

// Factory diperlukan untuk membuat ViewModel dengan dependensi
class RegisterViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            val repository = AuthRepositoryImpl()
            return RegisterViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}