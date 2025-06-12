package com.android.laundrygo.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.android.laundrygo.ui.screens.BagScreen
import com.android.laundrygo.ui.screens.CartScreen
import com.android.laundrygo.ui.screens.CartViewModel
import com.android.laundrygo.ui.screens.DashboardScreen
import com.android.laundrygo.ui.screens.DollScreen
import com.android.laundrygo.ui.screens.LocationScreen
import com.android.laundrygo.ui.screens.StartScreen
import com.android.laundrygo.ui.screens.LoginScreen
import com.android.laundrygo.ui.screens.RegisterScreen
import com.android.laundrygo.ui.screens.ServiceTypeScreen
import com.android.laundrygo.ui.screens.ShirtPantsScreen
import com.android.laundrygo.ui.screens.ShoesScreen
import com.android.laundrygo.ui.screens.SpecialTreatmentScreen
import com.android.laundrygo.ui.screens.TopUpScreen
import com.android.laundrygo.ui.screens.VoucherScreen

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
                onNavigateToServiceType = { navController.navigate(Screen.ServiceType.route) },
                onNavigateToLocation = { navController.navigate(Screen.Location.route) },
                onNavigateToCart = {navController.navigate(Screen.Cart.route)},
                onNavigateToVoucher = { navController.navigate(Screen.Voucher.route) },
                onNavigateToTopUp = { navController.navigate(Screen.TopUp.route) }
                // Di sini Anda bisa menambahkan parameter navigasi dari dashboard
                // ke layar lain, contohnya:
                // onNavigateToProfile = { navController.navigate("profile") }
            )
        }

        // Rute untuk Bag
        composable(route = Screen.Bag.route) {
            BagScreen(
                onBack = { navController.navigateUp() }
            )
        }

        // Rute untuk Doll
        composable(route = Screen.Doll.route) {
            DollScreen(
                onBack = { navController.navigateUp() }
            )
        }

        // Rute untuk Location
        composable(route = Screen.Location.route) {
            LocationScreen(
                onBack = { navController.navigateUp() }
            )
        }

        // Rute untuk ServiceType
        composable(route = Screen.ServiceType.route) {
            ServiceTypeScreen(
                onBack = { navController.navigateUp() },
                onBag = { navController.navigate(Screen.Bag.route) },
                onDoll = { navController.navigate(Screen.Doll.route) },
                onShirtPants = { navController.navigate(Screen.ShirtPants.route) },
                onShoes = { navController.navigate(Screen.Shoes.route) },
                onSpecialTreatment = { navController.navigate(Screen.SpecialTreatment.route) }

            )
        }

        // Rute untuk ShirtPants
        composable(route = Screen.ShirtPants.route) {
            ShirtPantsScreen(
                onBack = { navController.navigateUp() }
            )
        }

        // Rute untuk Shoes
        composable(route = Screen.Shoes.route) {
            ShoesScreen(
                onBack = { navController.navigateUp() }

            )
        }

        // Rute untuk SpecialTreatment
        composable(route = Screen.SpecialTreatment.route) {
            SpecialTreatmentScreen(
                onBack = { navController.navigateUp() }

            )
        }

        // Rute untuk TopUp
        composable(route = Screen.TopUp.route) {
            TopUpScreen(
                onBackClick = { navController.navigateUp() }
            )
        }

        // Rute untuk Cart
        composable(route = Screen.Cart.route) {
            // Membuat instance ViewModel yang terikat pada tujuan navigasi ini
            val cartViewModel: CartViewModel = viewModel()
            CartScreen(
                viewModel = cartViewModel,
                onBack = { navController.navigateUp() },
                onCheckoutClick = {
                    // Setelah checkout, kembali ke layar sebelumnya (Dashboard)
                    // Anda juga bisa navigasi ke layar "Pesanan Berhasil" di sini
                    navController.popBackStack()
                }
            )
        }

        // Rute untuk VoucherScreen (BARU)
        composable(route = Screen.Voucher.route) {
            VoucherScreen(
                onBackClick = {
                    navController.navigateUp() // Aksi untuk kembali ke layar sebelumnya
                }
            )
        }
    }
}