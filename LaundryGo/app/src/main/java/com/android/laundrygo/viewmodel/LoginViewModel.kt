package com.android.laundrygo.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.laundrygo.repository.AuthRepository
import com.android.laundrygo.repository.AuthRepositoryImpl
import com.google.firebase.firestore.toObject
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
    data object NavigateToAdminDashboard : LoginEvent // Add this
    data class ShowMessage(val message: String) : LoginEvent
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
            val input = state.email.trim()

            if (input.contains("@")) {
                // Treat as email login
                val result = repository.loginUser(input, state.password)
                result.onSuccess { firebaseUser ->
                    handleLoginSuccess(firebaseUser.uid)
                }.onFailure {
                    _uiState.update { s -> s.copy(errorMessage = it.message) }
                }
            } else {
                // Treat as username login
                repository.getUserByUsername(input).collect { usernameResult ->
                    usernameResult.onSuccess { querySnapshot ->
                        if (querySnapshot.isEmpty) {
                            _uiState.update { s -> s.copy(errorMessage = "Username tidak ditemukan") }
                        } else {
                            val userDocument = querySnapshot.documents.firstOrNull()
                            val email = userDocument?.toObject<com.android.laundrygo.model.User>()?.email
                            Log.d("LoginViewModel", "Email retrieved from username: $email") // Added line
                            if (!email.isNullOrBlank()) {
                                val result = repository.loginUser(email, state.password)
                                result.onSuccess { firebaseUser ->
                                    handleLoginSuccess(firebaseUser.uid)
                                }.onFailure {
                                    _uiState.update { s -> s.copy(errorMessage = it.message) }
                                }
                            } else {
                                _uiState.update { s -> s.copy(errorMessage = "Gagal mendapatkan email dari username") }
                            }
                        }
                    }.onFailure {
                        _uiState.update { s -> s.copy(errorMessage = it.message) }
                    }
                }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun LoginViewModel.handleLoginSuccess(userId: String) {
        viewModelScope.launch {
            repository.getUserDocument(userId).collect { userDocumentResult ->
                userDocumentResult.onSuccess { documentSnapshot ->
                    val isAdmin = documentSnapshot?.getBoolean("admin") ?: false
                    if (isAdmin) {
                        _eventFlow.emit(LoginEvent.NavigateToAdminDashboard)
                    } else {
                        _eventFlow.emit(LoginEvent.NavigateToDashboard)
                    }
                }.onFailure {
                    _uiState.update { s -> s.copy(errorMessage = it.message) }
                }
            }
        }
    }

    fun onGoogleLogin(idToken: String) {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.loginWithGoogle(idToken)
            result.onSuccess { firebaseUser ->
                firebaseUser?.uid?.let { userId ->
                    repository.getUserDocument(userId).collect { userDocumentResult ->
                        userDocumentResult.onSuccess { documentSnapshot ->
                            val isAdmin = documentSnapshot?.getBoolean("admin") ?: false
                            if (isAdmin) {
                                _eventFlow.emit(LoginEvent.NavigateToAdminDashboard)
                            } else {
                                _eventFlow.emit(LoginEvent.NavigateToDashboard)
                            }
                        }.onFailure {
                            _uiState.update { s -> s.copy(errorMessage = it.message) }
                        }
                    }
                }
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