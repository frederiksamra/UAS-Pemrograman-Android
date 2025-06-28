package com.android.laundrygo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.laundrygo.model.User
import com.android.laundrygo.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UserManagementViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    init {
        // Panggil loadUsers dengan penanda bahwa ini adalah pemuatan awal.
        loadUsers(isInitialLoad = true)
    }

    // Kita tambahkan parameter isInitialLoad dengan nilai default false.
    private fun loadUsers(isInitialLoad: Boolean = false) {
        _isLoading.update { true }
        viewModelScope.launch {
            if (isInitialLoad) {
                delay(1000)
            }

            authRepository.getAllUsers().collect { result ->
                result.onSuccess { usersList ->
                    // Saring daftar untuk membuang user yang memiliki status admin.
                    val filteredList = usersList.filter { !it.admin }
                    _users.update { filteredList }
                }.onFailure { error ->
                    // Pesan error PERMISSION_DENIED akan muncul di sini jika masih gagal.
                    _feedbackMessage.update { "Gagal memuat pengguna: ${error.message}" }
                }
                _isLoading.update { false }
            }
        }
    }

    fun deleteUser(userId: String) {
        _isLoading.update { true }
        viewModelScope.launch {
            // Saat menghapus, tidak perlu ada jeda.
            authRepository.deleteUserById(userId)
                .onSuccess {
                    _feedbackMessage.update { "Pengguna berhasil dihapus" }
                    // Muat ulang daftar pengguna setelah berhasil dihapus.
                    loadUsers()
                }
                .onFailure { error ->
                    _feedbackMessage.update { "Gagal menghapus pengguna: ${error.message}" }
                }
            _isLoading.update { false }
        }
    }

    fun clearFeedbackMessage() {
        _feedbackMessage.update { null }
    }
}

class UserManagementViewModelFactory(private val authRepository: AuthRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserManagementViewModel::class.java)) {
            return UserManagementViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
