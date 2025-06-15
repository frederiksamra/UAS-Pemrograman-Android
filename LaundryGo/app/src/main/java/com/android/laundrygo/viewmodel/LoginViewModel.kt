package com.android.laundrygo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.laundrygo.repository.AuthRepository
import com.android.laundrygo.repository.AuthRepositoryImpl
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed interface LoginEvent {
    data object NavigateToDashboard : LoginEvent
    data class ShowMessage(val message: String) : LoginEvent // Event baru untuk menampilkan pesan
}

class LoginViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<LoginEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun onEmailChange(email: String) { // Diubah dari onUsernameChange
        _uiState.update { it.copy(email = email, errorMessage = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    fun onTogglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onLoginClicked() {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val state = _uiState.value
            val result = repository.loginUser(state.email.trim(), state.password)
            result.onSuccess {
                _eventFlow.emit(LoginEvent.NavigateToDashboard)
            }.onFailure {
                _uiState.update { s -> s.copy(errorMessage = it.message) }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun onGoogleLogin(idToken: String) {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.loginWithGoogle(idToken)
            result.onSuccess {
                _eventFlow.emit(LoginEvent.NavigateToDashboard)
            }.onFailure {
                _uiState.update { s -> s.copy(errorMessage = it.message) }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun onForgotPasswordClicked() {
        if (_uiState.value.isLoading) return
        val email = _uiState.value.email.trim()
        if (email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Silakan masukkan email Anda terlebih dahulu") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.sendPasswordResetEmail(email)
            result.onSuccess {
                _eventFlow.emit(LoginEvent.ShowMessage("Email reset password telah dikirim ke $email"))
            }.onFailure {
                _uiState.update { s -> s.copy(errorMessage = it.message) }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}

// Factory untuk membuat ViewModel dengan dependensi
class LoginViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(AuthRepositoryImpl()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}