package com.android.laundrygo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.laundrygo.model.User
import com.android.laundrygo.repository.AuthRepository
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
    val userId: String = "", // Menggunakan uid agar konsisten
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
            result.onSuccess { user -> // 'user' di sini bertipe User? (bisa null)

                // --- PERBAIKAN DI SINI: Lakukan null check ---
                if (user != null) {
                    // Jika user tidak null, kita aman mengakses semua propertinya
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            userId = user.userId, // Gunakan 'uid' yang konsisten
                            name = user.name,
                            username = user.username,
                            email = user.email,
                            phone = user.phone,
                            address = user.address,
                            errorMessage = null
                        )
                    }
                } else {
                    // Jika user null, tangani sebagai error
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Profil pengguna tidak ditemukan.") }
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
            _isEditMode.value = false
            _uiState.update { it.copy(isLoading = true) }

            val updatedUser = User(
                userId =  _editState.value.userId, // Gunakan 'uid'
                name = _editState.value.name,
                username = _editState.value.username,
                email = _editState.value.email,
                phone = _editState.value.phone,
                address = _editState.value.address
            )

            val result = repository.updateUserProfile(updatedUser)
            result.onSuccess {
                _uiState.value = _editState.value.copy(isLoading = false)
            }.onFailure { exception ->
                _uiState.update { it.copy(isLoading = false, errorMessage = exception.message) }
            }
        }
    }

    // ... (Fungsi onNameChange, onEmailChange, dll tidak perlu diubah)
    fun onNameChange(newName: String) { _editState.update { it.copy(name = newName) } }
    fun onEmailChange(newEmail: String) { _editState.update { it.copy(email = newEmail) } }
    fun onPhoneChange(newPhone: String) { _editState.update { it.copy(phone = newPhone) } }
    fun onAddressChange(newAddress: String) { _editState.update { it.copy(address = newAddress) } }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _eventFlow.emit(ProfileEvent.NavigateToLogin)
        }
    }
}

// --- PERBAIKAN PADA FACTORY ---
// Factory sekarang menerima AuthRepository sebagai parameter
class ProfileViewModelFactory(private val repository: AuthRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            // Gunakan repository yang diberikan dari luar
            return ProfileViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}