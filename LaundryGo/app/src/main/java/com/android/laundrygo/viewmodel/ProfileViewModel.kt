package com.android.laundrygo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.laundrygo.repository.AuthRepository
import com.android.laundrygo.repository.AuthRepositoryImpl
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Tampung data untuk ditampilkan ke UI
data class ProfileUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val userId: String = "",
    val name: String = "",
    val username: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = ""
)

// Tampung data yang sedang diedit
typealias EditProfileState = ProfileUiState

sealed interface ProfileEvent {
    data object NavigateToLogin : ProfileEvent
}

class ProfileViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    private val _isEditMode = MutableStateFlow(false)
    val isEditMode = _isEditMode.asStateFlow()

    private val _editState = MutableStateFlow(EditProfileState())
    val editState = _editState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<ProfileEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        loadUserProfile()
    }

    // Mengambil data profil saat ViewModel dibuat
    private fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.getUserProfile()
            result.onSuccess { user ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        userId = user.userId,
                        name = user.name,
                        username = user.username,
                        email = user.email,
                        phone = user.phone,
                        address = user.address
                    )
                }
            }.onFailure { exception ->
                _uiState.update { it.copy(isLoading = false, errorMessage = exception.message) }
            }
        }
    }

    fun onEnterEditMode() {
        // Salin data saat ini dari uiState ke editState
        _editState.value = _uiState.value
        _isEditMode.value = true
    }

    fun onCancelEdit() {
        _isEditMode.value = false
    }

    fun onSaveEdit() {
        viewModelScope.launch {
            _isEditMode.value = false // Keluar dari mode edit, tampilkan loading di UI utama
            _uiState.update { it.copy(isLoading = true) }

            // Buat objek User baru dari state editan
            val updatedUser = com.android.laundrygo.model.User(
                userId = _editState.value.userId,
                name = _editState.value.name,
                username = _editState.value.username, // username tidak bisa diedit, ambil dari state lama
                email = _editState.value.email,
                phone = _editState.value.phone,
                address = _editState.value.address
            )

            // Panggil repository untuk menyimpan perubahan
            val result = repository.updateUserProfile(updatedUser)
            result.onSuccess {
                // Jika sukses, update state utama dengan data baru
                _uiState.value = _editState.value.copy(isLoading = false)
            }.onFailure { exception ->
                // Jika gagal, tampilkan pesan error
                _uiState.update { it.copy(isLoading = false, errorMessage = exception.message) }
            }
        }
    }

    // Fungsi untuk mengupdate setiap field di editState
    fun onNameChange(newName: String) {
        _editState.update { it.copy(name = newName) }
    }
    fun onEmailChange(newEmail: String) {
        _editState.update { it.copy(email = newEmail) }
    }
    fun onPhoneChange(newPhone: String) {
        _editState.update { it.copy(phone = newPhone) }
    }
    fun onAddressChange(newAddress: String) {
        _editState.update { it.copy(address = newAddress) }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout() // Panggil fungsi logout di repository
            _eventFlow.emit(ProfileEvent.NavigateToLogin)
        }
    }
}

// Factory untuk ProfileViewModel
class ProfileViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(AuthRepositoryImpl()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
