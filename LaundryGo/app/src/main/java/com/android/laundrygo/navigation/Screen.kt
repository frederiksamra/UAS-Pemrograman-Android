package com.android.laundrygo.navigation

sealed class Screen(val route: String) {
    data object Start : Screen("start_screen")
    data object Login : Screen("login_screen")
    data object Register : Screen("register_screen")
    data object Dashboard : Screen("dashboard_screen")
}