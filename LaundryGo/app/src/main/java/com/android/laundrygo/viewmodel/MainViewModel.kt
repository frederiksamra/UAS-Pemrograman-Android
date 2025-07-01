package com.android.laundrygo.viewmodel

import androidx.lifecycle.ViewModel
import com.android.laundrygo.navigation.Graph
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MainViewModel : ViewModel() {
    private val _isLocked = MutableStateFlow(false)
    val isLocked = _isLocked.asStateFlow()

    // State untuk menentukan tujuan navigasi awal
    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination = _startDestination.asStateFlow()

    fun lock() {
        _isLocked.update { true }
    }

    fun unlock() {
        _isLocked.update { false }
    }

    // Fungsi untuk menentukan rute awal dari MainActivity
    fun decideStartDestination(lastScreen: String? = null) {
        val user = FirebaseAuth.getInstance().currentUser
        val destination = if (user != null) {
            if (lastScreen != null) {
                lastScreen
            } else if (user.email == "admin@laundrygo.com") {
                Graph.ADMIN
            } else {
                Graph.MAIN
            }
        } else {
            Graph.AUTHENTICATION
        }
        _startDestination.value = destination
    }
}