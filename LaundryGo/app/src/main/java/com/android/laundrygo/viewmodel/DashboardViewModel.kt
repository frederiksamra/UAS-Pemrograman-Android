package com.android.laundrygo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class DashboardViewModel : ViewModel() {
    // Firebase instances
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // User information
    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    private val _userBalance = MutableLiveData<String>()
    val userBalance: LiveData<String> = _userBalance

    // Error handling
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    init {
        fetchUserData()
    }

    fun fetchUserData() {
        viewModelScope.launch {
            try {
                // Get current user's UID
                val currentUser = auth.currentUser
                    ?: throw Exception("No authenticated user found")

                // Fetch user document from Firestore
                val userDoc = firestore.collection("users")
                    .document(currentUser.uid)
                    .get()
                    .await()

                // Extract user name (assuming there's a 'name' or 'username' field)
                val userName = userDoc.getString("name")
                    ?: userDoc.getString("username")
                    ?: currentUser.displayName
                    ?: "User"

                // Extract balance (assuming there's a 'balance' field)
                val userBalance = userDoc.getString("balance") ?: "0"

                // Update LiveData
                _userName.value = userName
                _userBalance.value = userBalance

            } catch (e: Exception) {
                // Handle errors
                _error.value = "Failed to load user data: ${e.message}"
                _userName.value = "User"
                _userBalance.value = "0"
            }
        }
    }

    // Navigation methods
    fun navigateToNearestLocation() {
        // Implement navigation logic to maps or location screen
    }

    fun openTopUpScreen() {
        // Implement top-up screen navigation or dialog
    }

    // Method to claim a voucher
    fun claimVoucher(voucherId: String) {
        viewModelScope.launch {
            try {
                // Implement voucher claiming logic
                // This could involve updating Firestore document or calling an API
            } catch (e: Exception) {
                _error.value = "Failed to claim voucher: ${e.message}"
            }
        }
    }

    // Method to refresh dashboard data
    fun refreshDashboardData() {
        fetchUserData()
    }
}