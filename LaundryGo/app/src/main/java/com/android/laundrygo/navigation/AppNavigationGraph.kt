package com.android.laundrygo.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.android.laundrygo.viewmodel.ProfileViewModel

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
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToTopUp = { navController.navigate(Screen.TopUp.route) },
                onNavigateToHistory = { navController.navigate(Screen.History.route) }
            )
        }

        // Rute untuk TopUp
        composable(route = Screen.TopUp.route) {
            TopUpScreen(
                onBackClick = { navController.navigateUp() }
            )
        }

        // Rute untuk Bag
        composable(route = Screen.Bag.route) {
            BagScreen(
                onBack = { navController.navigateUp() },
                onCartClick = { navController.navigate(Screen.Cart.route) },
                onAddClick = TODO()
            )
        }

        // Rute untuk Doll
        composable(route = Screen.Doll.route) {
            DollScreen(
                onBack = { navController.navigateUp() },
                onCartClick = { navController.navigate(Screen.Cart.route) },
                onAddClick = TODO()
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
                // Kita gunakan satu callback onServiceSelected
                onServiceSelected = { serviceName ->
                    // Gunakan 'when' untuk menentukan navigasi berdasarkan nama layanan yang diterima
                    when (serviceName) {
                        "Pakaian Harian" -> navController.navigate(Screen.ShirtPants.route)
                        "Perawatan Khusus" -> navController.navigate(Screen.SpecialTreatment.route)
                        "Boneka" -> navController.navigate(Screen.Doll.route)
                        "Tas" -> navController.navigate(Screen.Bag.route)
                        "Sepatu" -> navController.navigate(Screen.Shoes.route)
                    }
                }
            )
        }

        // Rute untuk ShirtPants
        composable(route = Screen.ShirtPants.route) {
            ShirtPantsScreen(
                onBack = { navController.navigateUp() },
                onCartClick = { navController.navigate(Screen.Cart.route) },
                onAddClick = TODO()
            )
        }

        // Rute untuk Shoes
        composable(route = Screen.Shoes.route) {
            ShoesScreen(
                onBack = { navController.navigateUp() },
                onCartClick = { navController.navigate(Screen.Cart.route) },
                onAddClick = TODO()

            )
        }

        // Rute untuk SpecialTreatment
        composable(route = Screen.SpecialTreatment.route) {
            SpecialTreatmentScreen(
                onBack = { navController.navigateUp() },
                onCartClick = { navController.navigate(Screen.Cart.route) },
                onAddClick = TODO()
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

        // Rute untuk VoucherScreen
        composable(route = Screen.Voucher.route) {
            VoucherScreen(
                onBackClick = {
                    navController.navigateUp() // Aksi untuk kembali ke layar sebelumnya
                }
            )
        }

        // Rute untuk ProfileScreen
        composable(route = Screen.Profile.route) {
            // DITAMBAHKAN: Ambil instance ViewModel untuk mengakses state
            val viewModel: ProfileViewModel = viewModel()
            val isEditMode by viewModel.isEditMode.collectAsState()

            // DITAMBAHKAN: Menangani tombol kembali dari sistem (system back-press)
            // Handler ini hanya aktif jika isEditMode == true
            BackHandler(enabled = isEditMode) {
                // Jika pengguna menekan kembali saat mengedit,
                // batalkan mode edit, jangan keluar dari layar.
                viewModel.onCancelEdit()
            }

            ProfileScreen(
                // viewModel diteruskan agar BackHandler dan ProfileScreen
                // menggunakan instance yang sama.
                viewModel = viewModel,
                onNavigateBack = {
                    // Aksi ini hanya akan terpanggil dari dalam ProfileScreen
                    // jika TIDAK dalam mode edit.
                    navController.navigateUp()
                },
                onLogout = {
                    // Aksi setelah logout berhasil (tidak berubah)
                    navController.navigate(Screen.Start.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }
        // Rute untuk HistoryScreen
        composable(route = Screen.History.route) {
            HistoryScreen(
                onBack = { navController.navigateUp() },
                onCheckClick = { orderId ->
                    navController.navigate(Screen.HistoryDetail.createRoute(orderId))
                }
            )
        }

        composable(route = Screen.HistoryDetail.route) { backStackEntry ->

        val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            HistoryDetailScreen(
                orderId = orderId,
                onBack = { navController.navigateUp() }
            )
        }



    }
}