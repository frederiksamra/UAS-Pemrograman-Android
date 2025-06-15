package com.android.laundrygo.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
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
import com.android.laundrygo.ui.screens.ProfileScreen
import com.android.laundrygo.ui.screens.RegisterScreen
import com.android.laundrygo.ui.screens.ServiceTypeScreen
import com.android.laundrygo.ui.screens.ShirtPantsScreen
import com.android.laundrygo.ui.screens.ShoesScreen
import com.android.laundrygo.ui.screens.SpecialTreatmentScreen
import com.android.laundrygo.ui.screens.VoucherScreen
import com.android.laundrygo.ui.screens.HistoryScreen
import com.android.laundrygo.ui.screens.HistoryDetailScreen
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.android.laundrygo.ui.screens.TopUpScreen
import com.android.laundrygo.ui.screens.TransactionScreen
import com.android.laundrygo.viewmodel.ProfileViewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.android.laundrygo.ui.theme.LaundryGoTheme
import com.android.laundrygo.viewmodel.TransactionViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.navigation
import com.android.laundrygo.ui.screens.payment.PaymentScreen
import com.android.laundrygo.viewmodel.LoginViewModelFactory
import com.android.laundrygo.viewmodel.PaymentViewModelFactory
import com.android.laundrygo.viewmodel.ProfileViewModelFactory
import com.android.laundrygo.viewmodel.RegisterViewModelFactory

// Mendefinisikan rute untuk setiap grafik agar lebih rapi
object Graph {
    const val ROOT = "root_graph"
    const val AUTHENTICATION = "auth_graph"
    const val MAIN = "main_graph"
}

@Composable
fun AppNavigationGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Graph.AUTHENTICATION, // Memulai dari grafik otentikasi
        route = Graph.ROOT
    ) {
        // --- GRAFIK UNTUK ALUR OTENTIKASI (START, LOGIN, REGISTER) ---
        authGraph(navController)

        // --- GRAFIK UNTUK ALUR UTAMA APLIKASI (SETELAH LOGIN) ---
        mainGraph(navController)
    }
}

// Fungsi ini mengelompokkan semua rute otentikasi
private fun NavGraphBuilder.authGraph(navController: NavHostController) {
    navigation(
        startDestination = Screen.Start.route,
        route = Graph.AUTHENTICATION
    ) {
        composable(route = Screen.Start.route) {
            StartScreen(
                onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }

        composable(route = Screen.Login.route) {
            LoginScreen(
                onBackClicked = { navController.navigateUp() },
                onLoginSuccess = {
                    // Setelah login, pindah ke grafik utama dan hapus grafik otentikasi
                    navController.navigate(Graph.MAIN) {
                        popUpTo(Graph.AUTHENTICATION) { inclusive = true }
                    }
                },
                // onNavigateToForgotPassword tidak lagi diperlukan karena ditangani oleh ViewModel
                viewModel = viewModel(factory = LoginViewModelFactory())
            )
        }

        composable(route = Screen.Register.route) {
            RegisterScreen(
                onBackClicked = { navController.navigateUp() },
                onRegistrationSuccess = { navController.popBackStack() },
                viewModel = viewModel(factory = RegisterViewModelFactory())
            )
        }
    }
}

// Fungsi ini mengelompokkan semua rute aplikasi utama
private fun NavGraphBuilder.mainGraph(navController: NavHostController) {
    navigation(
        startDestination = Screen.Dashboard.route,
        route = Graph.MAIN
    ) {
        composable(route = Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToServiceType = { navController.navigate(Screen.ServiceType.route) },
                onNavigateToLocation = { navController.navigate(Screen.Location.route) },
                onNavigateToCart = { navController.navigate(Screen.Cart.route) },
                onNavigateToVoucher = { navController.navigate(Screen.Voucher.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToTopUp = { navController.navigate(Screen.TopUp.route) },
                onNavigateToHistory = { navController.navigate(Screen.History.route) }
            )
        }

        // Rute untuk DashboardScreen
        composable(route = com.android.laundrygo.navigation.Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToServiceType = { navController.navigate(com.android.laundrygo.navigation.Screen.ServiceType.route) },
                onNavigateToLocation = { navController.navigate(com.android.laundrygo.navigation.Screen.Location.route) },
                onNavigateToCart = {navController.navigate(com.android.laundrygo.navigation.Screen.Cart.route)},
                onNavigateToVoucher = { navController.navigate(com.android.laundrygo.navigation.Screen.Voucher.route) },
                onNavigateToProfile = { navController.navigate(com.android.laundrygo.navigation.Screen.Profile.route) },
                onNavigateToTopUp = { navController.navigate(com.android.laundrygo.navigation.Screen.TopUp.route) },
                onNavigateToHistory = { navController.navigate(com.android.laundrygo.navigation.Screen.History.route) }
            )
        }

        // Rute untuk TopUp
        composable(route = com.android.laundrygo.navigation.Screen.TopUp.route) {
            TopUpScreen(
                onBackClick = { navController.navigateUp() }
            )
        }

        // Rute untuk Bag
        composable(route = com.android.laundrygo.navigation.Screen.Bag.route) {
            BagScreen(
                onBack = { navController.navigateUp() },
                onCartClick = { navController.navigate(com.android.laundrygo.navigation.Screen.Cart.route) },
                onAddClick = {}
            )
        }

        // Rute untuk Doll
        composable(route = com.android.laundrygo.navigation.Screen.Doll.route) {
            DollScreen(
                onBack = { navController.navigateUp() },
                onCartClick = { navController.navigate(com.android.laundrygo.navigation.Screen.Cart.route) },
                onAddClick = {}
            )
        }

        // Rute untuk Location
        composable(route = com.android.laundrygo.navigation.Screen.Location.route) {
            LocationScreen(
                onBack = { navController.navigateUp() }
            )
        }

        // Rute untuk ServiceType
        composable(route = com.android.laundrygo.navigation.Screen.ServiceType.route) {
            ServiceTypeScreen(
                onBack = { navController.navigateUp() },
                // Kita gunakan satu callback onServiceSelected
                onServiceSelected = { serviceName ->
                    when (serviceName) {
                        "Pakaian Harian" -> navController.navigate(com.android.laundrygo.navigation.Screen.ShirtPants.route)
                        "Perawatan Khusus" -> navController.navigate(com.android.laundrygo.navigation.Screen.SpecialTreatment.route)
                        "Boneka" -> navController.navigate(com.android.laundrygo.navigation.Screen.Doll.route)
                        "Tas" -> navController.navigate(com.android.laundrygo.navigation.Screen.Bag.route)
                        "Sepatu" -> navController.navigate(com.android.laundrygo.navigation.Screen.Shoes.route)
                    }
                }
            )
        }

        // Rute untuk ShirtPants
        composable(route = com.android.laundrygo.navigation.Screen.ShirtPants.route) {
            ShirtPantsScreen(
                onBack = { navController.navigateUp() },
                onCartClick = { navController.navigate(com.android.laundrygo.navigation.Screen.Cart.route) },
                onAddClick = {}
            )
        }

        // Rute untuk Shoes
        composable(route = com.android.laundrygo.navigation.Screen.Shoes.route) {
            ShoesScreen(
                onBack = { navController.navigateUp() },
                onCartClick = { navController.navigate(com.android.laundrygo.navigation.Screen.Cart.route) },
                onAddClick = {}

            )
        }

        // Rute untuk SpecialTreatment
        composable(route = com.android.laundrygo.navigation.Screen.SpecialTreatment.route) {
            SpecialTreatmentScreen(
                onBack = { navController.navigateUp() },
                onCartClick = { navController.navigate(com.android.laundrygo.navigation.Screen.Cart.route) },
                onAddClick = {}
            )
        }

        // Rute untuk VoucherScreen
        composable(route = com.android.laundrygo.navigation.Screen.Voucher.route) {
            VoucherScreen(
                onBackClick = {
                    navController.navigateUp() // Aksi untuk kembali ke layar sebelumnya
                }
            )
        }

        // Rute untuk HistoryScreen
        composable(route = com.android.laundrygo.navigation.Screen.History.route) {
            HistoryScreen(
                onBack = { navController.navigateUp() },
                onCheckClick = { orderId ->
                    navController.navigate(com.android.laundrygo.navigation.Screen.HistoryDetail.createRoute(orderId))
                }
            )
        }

        composable(route = com.android.laundrygo.navigation.Screen.HistoryDetail.route) { backStackEntry ->

            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            HistoryDetailScreen(
                orderId = orderId,
                onBack = { navController.navigateUp() }
            )
        }

        val KEY_TOTAL_PRICE = "totalPrice"

        composable(route = Screen.Cart.route) {
            val cartViewModel: CartViewModel = viewModel()
//            val totalPrice by cartViewModel.totalPrice.collectAsState()

            CartScreen(
                viewModel = cartViewModel,
                onBack = { navController.navigateUp() },
                onCheckoutClick = {
//                    navController.navigate("${Screen.Transaction.route}/$totalPrice")
                }
            )
        }

        composable(
            route = "${Screen.Transaction.route}/{$KEY_TOTAL_PRICE}",
            arguments = listOf(navArgument(KEY_TOTAL_PRICE) { type = NavType.FloatType })
        ) { backStackEntry ->
            val price = backStackEntry.arguments?.getFloat(KEY_TOTAL_PRICE) ?: 0f
            val transactionViewModel: TransactionViewModel = viewModel(
                factory = TransactionViewModel.provideFactory(price.toDouble())
            )
            TransactionScreen(
                viewModel = transactionViewModel,
                onBack = { navController.navigateUp() },
                onCheckoutSuccess = {
                    navController.navigate("${Screen.Payment.route}/$price")
                }
            )
        }

        composable(
            route = "${Screen.Payment.route}/{$KEY_TOTAL_PRICE}",
            arguments = listOf(navArgument(KEY_TOTAL_PRICE) { type = NavType.FloatType })
        ) {
            PaymentScreen(
                onBackClicked = { navController.navigateUp() },
                onPaymentSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Graph.MAIN) { inclusive = true }
                    }
                },
                paymentViewModel = viewModel(factory = PaymentViewModelFactory())
            )
        }

        // Rute untuk ProfileScreen
        composable(route = Screen.Profile.route) {
            val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory())
            val isEditMode by profileViewModel.isEditMode.collectAsState()

            BackHandler(enabled = isEditMode) {
                profileViewModel.onCancelEdit()
            }

            ProfileScreen(
                viewModel = profileViewModel,
                onNavigateBack = { navController.navigateUp() },
                onLogout = {
                    navController.navigate(Graph.AUTHENTICATION) {
                        popUpTo(Graph.MAIN) { inclusive = true }
                    }
                }
            )
        }
    }
}