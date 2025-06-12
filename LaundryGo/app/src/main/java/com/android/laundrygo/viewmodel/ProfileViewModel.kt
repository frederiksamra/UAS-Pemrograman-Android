package com.android.laundrygo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Data class untuk state utama, tidak berubah
data class ProfileUiState(
    val name: String = "Saoirse",
    val username: String = "Saoirse800",
    val email: String = "saoirse@gmail.com",
    val phone: String = "07624316283",
    val address: String = "Jl. Melati No. 15A\nKota Baru, Jakarta Selatan\nIndonesia"
)

sealed interface ProfileEvent {
    data object NavigateToLogin : ProfileEvent
}

class ProfileViewModel : ViewModel() {

    // State utama yang menampilkan data (tidak akan diubah selama mode edit)
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    // DITAMBAHKAN: State untuk melacak mode edit
    private val _isEditMode = MutableStateFlow(false)
    val isEditMode = _isEditMode.asStateFlow()

    // DITAMBAHKAN: State untuk menampung data yang sedang diedit pengguna
    private val _editState = MutableStateFlow(ProfileUiState())
    val editState = _editState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<ProfileEvent>()
    val eventFlow = _eventFlow.asSharedFlow()


    // DITAMBAHKAN: Fungsi untuk masuk ke mode edit
    fun onEnterEditMode() {
        // Salin data saat ini dari uiState ke editState
        _editState.value = _uiState.value
        _isEditMode.value = true
    }

    // DITAMBAHKAN: Fungsi untuk membatalkan editan
    fun onCancelEdit() {
        _isEditMode.value = false
    }

    // DITAMBAHKAN: Fungsi untuk menyimpan perubahan
    fun onSaveEdit() {
        // Di aplikasi nyata, di sini Anda akan memanggil API/Database untuk menyimpan data
        // dari _editState.value.

        // Setelah berhasil menyimpan, update state utama (_uiState)
        _uiState.value = _editState.value

        // Keluar dari mode edit
        _isEditMode.value = false
    }

    // DITAMBAHKAN: Fungsi untuk mengupdate setiap field di editState
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
            _eventFlow.emit(ProfileEvent.NavigateToLogin)
        }
    }
}