package com.android.laundrygo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.laundrygo.navigation.AdminScreen
import com.android.laundrygo.repository.AuthRepository
import com.android.laundrygo.repository.AuthRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AdminMainViewModel(private val authRepository: AuthRepository) : ViewModel() {
    val navigationItems = listOf(
        AdminScreen.Users,
        AdminScreen.Vouchers,
        AdminScreen.Orders,
    )

    private val _title = MutableStateFlow(navigationItems.first().label)
    val title = _title.asStateFlow()

    fun onRouteChanged(route: String?) {
        val newScreen = navigationItems.find { it.route == route }
        if (newScreen != null) {
            _title.value = newScreen.label
        }
    }

    fun logout() {
        authRepository.logout()
    }
}

class AdminMainViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminMainViewModel::class.java)) {
            return AdminMainViewModel(AuthRepositoryImpl()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
