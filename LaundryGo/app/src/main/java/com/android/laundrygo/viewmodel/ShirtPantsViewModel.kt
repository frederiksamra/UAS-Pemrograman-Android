package com.android.laundrygo.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.laundrygo.model.LaundryService
import com.android.laundrygo.model.ServiceItem
import com.android.laundrygo.model.parsePrice
import com.android.laundrygo.repository.ServiceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ShirtPantsUiState(
    val isLoading: Boolean = true,
    val services: List<LaundryService> = emptyList(),
    val error: String? = null
)

class ShirtPantsViewModel(private val serviceRepository: ServiceRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ShirtPantsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        fetchDailyWearServices()
    }

    private fun fetchDailyWearServices() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val result = serviceRepository.getServices("daily_wear")

            result.onSuccess { services ->
                _uiState.update {
                    it.copy(isLoading = false, services = services)
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(isLoading = false, error = error.message)
                }
            }
        }
    }

    // Fungsi cart bisa ditambahkan di sini nanti
    // fun addToCart(item: LaundryService) { ... }

    companion object {
        fun provideFactory(
            repository: ServiceRepository,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ShirtPantsViewModel::class.java)) {
                    return ShirtPantsViewModel(repository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}

