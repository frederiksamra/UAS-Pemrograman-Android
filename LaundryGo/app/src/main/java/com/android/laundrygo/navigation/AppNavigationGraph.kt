package com.android.laundrygo.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.android.laundrygo.ui.screens.DashboardScreen
import com.android.laundrygo.ui.screens.StartScreen
import com.android.laundrygo.ui.screens.LoginScreen
import com.android.laundrygo.ui.screens.RegisterScreen

@Composable
fun AppNavigationGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Start.route // Layar pertama yang dibuka
    ) {
        // Rute untuk StartScreen
        composable(route = Screen.Start.route) {
            StartScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route)
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        // Rute untuk LoginScreen
        composable(route = Screen.Login.route) {
            LoginScreen(
                onBackClicked = {
                    navController.navigateUp() // Kembali ke layar sebelumnya di tumpukan
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Start.route) {
                            inclusive = true
                        }
                    }
                },
                onNavigateToForgotPassword = {
                    // navController.navigate(Screen.ForgotPassword.route)
                }
            )
        }

        // Rute untuk RegisterScreen
        composable(route = Screen.Register.route) {
            RegisterScreen(
                onBackClicked = {
                    navController.navigateUp()
                },
                onRegistrationSuccess = {
                    // Setelah registrasi, kembali ke halaman sebelumnya (StartScreen)
                    navController.popBackStack()
                }
            )
        }

        // Rute untuk DashboardScreen
        composable(route = Screen.Dashboard.route) {
            DashboardScreen(
                // Di sini Anda bisa menambahkan parameter navigasi dari dashboard
                // ke layar lain, contohnya:
                // onNavigateToProfile = { navController.navigate("profile") }
            )
        }
    }
}