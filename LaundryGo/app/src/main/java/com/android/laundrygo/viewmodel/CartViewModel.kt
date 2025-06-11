package com.android.laundrygo.viewmodel
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.android.laundrygo.model.CartItem
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CartViewModel : ViewModel() {

    // MutableStateFlow untuk menyimpan daftar item (private)
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())

    // StateFlow yang akan diobservasi oleh UI (public, read-only)
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    init {
        // Memuat item awal saat ViewModel dibuat
        loadInitialItems()
    }

    private fun loadInitialItems() {
        // Contoh data seperti di XML
        val initialItems = listOf(
            CartItem(
                id = 1,
                title = "Deep Cleaning",
                price = "IDR 60.000/Pair of shoes"
            ),
            CartItem(
                id = 2,
                title = "King Package",
                description = "Mash express, ironing, pick up and drop off",
                price = "IDR 35.000/Kg"
            ),
            CartItem(
                id = 3,
                title = "Special Long Dress",
                price = "IDR 27.000/Pcs"
            )
        )
        _cartItems.value = initialItems
    }

    // Fungsi untuk menghapus item dari keranjang
    fun removeItem(itemId: Int) {
        viewModelScope.launch {
            val currentItems = _cartItems.value.toMutableList()
            currentItems.removeAll { it.id == itemId }
            _cartItems.value = currentItems
        }
    }

    // Fungsi ini bisa diimplementasikan untuk navigasi atau menampilkan total
    fun onCheckoutClicked() {
        // Logika untuk proses checkout
        println("Checkout button clicked!")
    }

    fun onBackClicked() {
        // Logika untuk kembali ke layar sebelumnya
        println("Back button clicked!")
    }
}
