package com.android.laundrygo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.laundrygo.model.LaundryService
import com.android.laundrygo.repository.ServiceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DollUiState(
    val isLoading: Boolean = true,
    val services: List<LaundryService> = emptyList(),
    val error: String? = null
)

class DollViewModel(private val repository: ServiceRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(DollUiState())
    val uiState = _uiState.asStateFlow()

    init {
        fetchDollServices()
    }

    private fun fetchDollServices() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val result = repository.getServices("doll")

            result.onSuccess { services ->
                _uiState.update { it.copy(isLoading = false, services = services) }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, error = error.message) }
            }
        }
    }

    companion object {
        fun provideFactory(repository: ServiceRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(DollViewModel::class.java)) {
                        return DollViewModel(repository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }

}
