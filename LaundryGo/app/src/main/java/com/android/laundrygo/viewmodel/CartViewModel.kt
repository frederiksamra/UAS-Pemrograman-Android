package com.android.laundrygo.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.laundrygo.model.CartItem
import com.android.laundrygo.model.LaundryService
import com.android.laundrygo.repository.ServiceRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// --- State Class ---
data class CartUiState(
    val cartItems: List<CartItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

// --- ViewModel ---
class CartViewModel(private val repository: ServiceRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val TAG = "CartViewModel"

    init {
        loadCartItems()
    }

    private fun loadCartItems() {
        if (userId.isNullOrEmpty()) {
            _uiState.update { it.copy(isLoading = false, error = "User tidak login. Silakan login kembali.") }
            Log.w(TAG, "User is not logged in. Cannot load cart.")
            return
        }

        repository.getCartItems(userId)
            .onEach { result ->
                _uiState.update { currentState ->
                    result.fold(
                        onSuccess = { items ->
                            currentState.copy(cartItems = items, isLoading = false, error = null)
                        },
                        onFailure = { error ->
                            Log.e(TAG, "Error loading cart items: ${error.message}")
                            currentState.copy(isLoading = false, error = error.message)
                        }
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun addItem(service: LaundryService) {
        if (userId.isNullOrEmpty()) return
        Log.d(TAG, "Adding item: ${service.title}")
        viewModelScope.launch {
            repository.addItemToCart(userId, service).collect { result ->
                result.onFailure { error -> Log.e(TAG, "Failed to add item: ${error.message}") }
            }
        }
    }

    fun removeItem(itemId: String) {
        if (userId.isNullOrEmpty()) return
        Log.d(TAG, "Removing item: $itemId")
        viewModelScope.launch {
            repository.removeItemFromCart(userId, itemId).collect { result ->
                result.onFailure { error -> Log.e(TAG, "Failed to remove item: ${error.message}") }
            }
        }
    }

    fun updateQuantity(itemId: String, change: Int) {
        if (userId.isNullOrEmpty()) return
        Log.d(TAG, "Updating quantity for item: $itemId by $change")
        viewModelScope.launch {
            repository.updateItemQuantity(userId, itemId, change).collect { result ->
                result.onFailure { error -> Log.e(TAG, "Failed to update quantity: ${error.message}") }
            }
        }
    }
}


// --- ViewModel Factory ---
class CartViewModelFactory(private val repository: ServiceRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CartViewModel::class.java)) {
            return CartViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}