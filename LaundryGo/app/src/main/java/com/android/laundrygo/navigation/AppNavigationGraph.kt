package com.android.laundrygo.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.android.laundrygo.repository.AuthRepository
import com.android.laundrygo.repository.AuthRepositoryImpl
import com.android.laundrygo.repository.ServiceRepository
import com.android.laundrygo.repository.ServiceRepositoryImpl
import com.android.laundrygo.ui.screens.*
import com.android.laundrygo.ui.theme.LaundryGoTheme
import com.android.laundrygo.viewmodel.*

object Graph {
    const val ROOT = "root_graph"
    const val AUTHENTICATION = "auth_graph"
    const val MAIN = "main_graph"
    const val ADMIN = "admin_graph"
}

@Composable
fun AppNavigationGraph() {
    val navController = rememberNavController()

    val serviceRepository: ServiceRepository = remember { ServiceRepositoryImpl() }
    val authRepository: AuthRepository = remember { AuthRepositoryImpl() }

    NavHost(
        navController = navController,
        startDestination = Graph.AUTHENTICATION,
        route = Graph.ROOT
    ) {
        authGraph(navController)
        mainGraph(navController, serviceRepository, authRepository)
        adminGraph(navController)
    }
}

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
            val factory = remember { LoginViewModelFactory() }
            LoginScreen(
                onBackClicked = { navController.navigateUp() },
                onLoginSuccess = {
                    navController.navigate(Graph.MAIN) {
                        popUpTo(Graph.AUTHENTICATION) { inclusive = true }
                    }
                },
                onNavigateToAdminDashboard = {
                    navController.navigate(Graph.ADMIN) {
                        popUpTo(Graph.AUTHENTICATION) { inclusive = true }
                    }
                },
                viewModel = viewModel(factory = factory)
            )
        }
        composable(route = Screen.Register.route) {
            val factory = remember { RegisterViewModelFactory() }
            RegisterScreen(
                onBackClicked = { navController.navigateUp() },
                onRegistrationSuccess = { navController.popBackStack() },
                viewModel = viewModel(factory = factory)
            )
        }
    }
}

// 1. Menerima repository sebagai parameter untuk dependency injection
private fun NavGraphBuilder.mainGraph(
    navController: NavHostController,
    serviceRepository: ServiceRepository,
    authRepository: AuthRepository
) {
    navigation(
        startDestination = Screen.Dashboard.route,
        route = Graph.MAIN
    ) {
        // Helper untuk Shared CartViewModel yang sekarang menggunakan repository dari parameter
        val getSharedCartViewModel: @Composable (NavBackStackEntry) -> CartViewModel = { entry ->
            val parentEntry = remember(entry) { navController.getBackStackEntry(Graph.MAIN) }
            val factory = remember { CartViewModelFactory(serviceRepository) }
            viewModel(parentEntry, factory = factory)
        }

        // --- Layar Utama ---
        composable(route = Screen.Dashboard.route) {
            val factory = remember { DashboardViewModel.provideFactory(authRepository) }
            DashboardScreen(
                viewModel = viewModel(factory = factory),
                onNavigateToServiceType = { navController.navigate(Screen.ServiceType.route) },
                onNavigateToLocation = { navController.navigate(Screen.Location.route) },
                onNavigateToCart = { navController.navigate(Screen.Cart.route) },
                onNavigateToVoucher = { navController.navigate(Screen.Voucher.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToTopUp = { navController.navigate(Screen.TopUp.route) },
                onNavigateToHistory = { navController.navigate(Screen.History.route) },
                onNavigateToInProcess = { navController.navigate(Screen.InProcess.route) }
            )
        }

        // --- Pendaftaran Semua Rute Aplikasi Pengguna ---
        composable(route = Screen.InProcess.route) {
            val factory = remember { InProcessViewModel.provideFactory(serviceRepository) }
            InProcessScreen(
                viewModel = viewModel(factory = factory),
                onBackClick = { navController.navigateUp() }
            )
        }

        composable(route = Screen.Location.route) {
            LocationScreen(onBack = { navController.navigateUp() })
        }

        composable(route = Screen.TopUp.route) {
            val factory = remember { TopUpViewModel.provideFactory(authRepository) }
            TopUpScreen(
                viewModel = viewModel(factory = factory),
                onBackClick = { navController.navigateUp() }
            )
        }

        composable(route = Screen.Voucher.route) {
            val factory = remember { VoucherViewModel.provideFactory(authRepository) }
            VoucherScreen(
                onBackClick = { navController.popBackStack() },
                onVoucherSelected = { voucher ->
                    navController.previousBackStackEntry?.savedStateHandle?.set(
                        "selected_voucher",
                        voucher
                    )
                    navController.popBackStack()
                },
                voucherViewModel = viewModel(factory = factory)
            )
        }

        composable(route = Screen.History.route) {
            val factory = remember { HistoryViewModelFactory(serviceRepository) }
            HistoryScreen(
                viewModel = viewModel(factory = factory),
                onBack = { navController.navigateUp() },
                onCheckClick = { orderId ->
                    navController.navigate(Screen.HistoryDetail.createRoute(orderId))
                }
            )
        }

        composable(
            route = Screen.HistoryDetail.routeWithArgs,
            arguments = Screen.HistoryDetail.arguments
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString(Screen.HistoryDetail.ORDER_ID_ARG) ?: "" // Gunakan konstanta agar lebih aman
            HistoryDetailScreen(orderId = orderId, onBack = { navController.navigateUp() })
        }

        composable(route = Screen.Profile.route) {
            val factory = remember { ProfileViewModelFactory(authRepository) }
            ProfileScreen(
                viewModel = viewModel(factory = factory),
                onNavigateBack = { navController.navigateUp() },
                onLogout = {
                    navController.navigate(Graph.AUTHENTICATION) {
                        popUpTo(Graph.ROOT) { inclusive = true }
                    }
                }
            )
        }

        // --- Layar-layar Layanan ---
        composable(route = Screen.ServiceType.route) {
            ServiceTypeScreen(
                onBack = { navController.navigateUp() },
                onServiceSelected = { serviceName ->
                    val route = when (serviceName) {
                        "Pakaian Harian" -> Screen.ShirtPants.route
                        "Perawatan Khusus" -> Screen.SpecialTreatment.route
                        "Boneka" -> Screen.Doll.route
                        "Tas" -> Screen.Bag.route
                        "Sepatu" -> Screen.Shoes.route
                        else -> null
                    }
                    route?.let { navController.navigate(it) }
                })
        }

        val serviceScreens = listOf(
            Screen.Bag, Screen.Doll, Screen.ShirtPants, Screen.Shoes, Screen.SpecialTreatment
        )

        serviceScreens.forEach { screen ->
            composable(route = screen.route) { navBackStackEntry ->
                val cartViewModel = getSharedCartViewModel(navBackStackEntry)
                val context = LocalContext.current

                // Pola yang lebih sederhana untuk membuat ViewModel
                val factory = when (screen) {
                    Screen.Bag -> BagViewModel.provideFactory(serviceRepository)
                    Screen.Doll -> DollViewModel.provideFactory(serviceRepository)
                    Screen.ShirtPants -> ShirtPantsViewModel.provideFactory(serviceRepository)
                    Screen.Shoes -> ShoesViewModel.provideFactory(serviceRepository)
                    Screen.SpecialTreatment -> SpecialTreatmentViewModel.provideFactory(
                        serviceRepository
                    )

                    else -> throw IllegalArgumentException("Route tidak dikenal untuk service screen")
                }

                val localViewModel: ViewModel = viewModel(factory = factory)

                // Menampilkan layar yang sesuai berdasarkan rute
                when (screen) {
                    Screen.Bag -> BagScreen(
                        viewModel = localViewModel as BagViewModel,
                        onBack = { navController.navigateUp() },
                        onCartClick = { navController.navigate(Screen.Cart.route) },
                        onAddClick = { service ->
                            cartViewModel.addItem(service); Toast.makeText(
                            context,
                            "${service.title} ditambahkan",
                            Toast.LENGTH_SHORT
                        ).show()
                        })

                    Screen.Doll -> DollScreen(
                        viewModel = localViewModel as DollViewModel,
                        onBack = { navController.navigateUp() },
                        onCartClick = { navController.navigate(Screen.Cart.route) },
                        onAddClick = { service ->
                            cartViewModel.addItem(service); Toast.makeText(
                            context,
                            "${service.title} ditambahkan",
                            Toast.LENGTH_SHORT
                        ).show()
                        })

                    Screen.ShirtPants -> ShirtPantsScreen(
                        viewModel = localViewModel as ShirtPantsViewModel,
                        onBack = { navController.navigateUp() },
                        onCartClick = { navController.navigate(Screen.Cart.route) },
                        onAddClick = { service ->
                            cartViewModel.addItem(service); Toast.makeText(
                            context,
                            "${service.title} ditambahkan",
                            Toast.LENGTH_SHORT
                        ).show()
                        })

                    Screen.Shoes -> ShoesScreen(
                        viewModel = localViewModel as ShoesViewModel,
                        onBack = { navController.navigateUp() },
                        onCartClick = { navController.navigate(Screen.Cart.route) },
                        onAddClick = { service ->
                            cartViewModel.addItem(service); Toast.makeText(
                            context,
                            "${service.title} ditambahkan",
                            Toast.LENGTH_SHORT
                        ).show()
                        })

                    Screen.SpecialTreatment -> SpecialTreatmentScreen(
                        viewModel = localViewModel as SpecialTreatmentViewModel,
                        onBack = { navController.navigateUp() },
                        onCartClick = { navController.navigate(Screen.Cart.route) },
                        onAddClick = { service ->
                            cartViewModel.addItem(service); Toast.makeText(
                            context,
                            "${service.title} ditambahkan",
                            Toast.LENGTH_SHORT
                        ).show()
                        })

                    else -> {}
                }
            }
        }

        // --- Alur Checkout ---
        composable(route = Screen.Cart.route) { navBackStackEntry ->
            val cartViewModel = getSharedCartViewModel(navBackStackEntry)
            CartScreen(
                viewModel = cartViewModel,
                onBack = { navController.navigateUp() },
                onCheckoutClick = { totalPrice -> // totalPrice di sini adalah Double
                    // KOREKSI: Cukup konversi ke Float sebelum dimasukkan ke dalam fungsi
                    navController.navigate(Screen.Transaction.createRoute(totalPrice.toFloat()))
                }
            )
        }

        composable(
            route = Screen.Transaction.routeWithArgs,
            arguments = Screen.Transaction.arguments
        ) { backStackEntry ->
            val price = backStackEntry.arguments?.getFloat(Screen.Transaction.totalPriceArg) ?: 0f
            val factory = remember(price) { TransactionViewModel.provideFactory(serviceRepository, price.toDouble()) }
            TransactionScreen(
                viewModel = viewModel(factory = factory),
                onBack = { navController.navigateUp() },
                onCheckoutSuccess = { transactionId ->
                    navController.navigate(Screen.Payment.createRoute(transactionId))
                }
            )
        }

        composable(
            route = Screen.Payment.routeWithArgs,
            arguments = Screen.Payment.arguments
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getString(Screen.Payment.transactionIdArg) ?: ""
            val factory = remember(transactionId) {
                PaymentViewModel.PaymentViewModelFactory(
                    serviceRepository,
                    authRepository,
                    transactionId,
                    backStackEntry.savedStateHandle
                )
            }
            PaymentScreen(
                onBackClicked = { navController.navigateUp() },
                onPaymentSuccess = {
                    // KOREKSI KRITIS: Membersihkan backstack hingga ke Dashboard
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Dashboard.route) {
                            inclusive = true
                        }
                    }
                },
                onNavigateToTopUp = { navController.navigate(Screen.TopUp.route) },
                onNavigateToVoucher = { navController.navigate(Screen.Voucher.route) },
                paymentViewModel = viewModel(factory = factory)
            )
        }
    }
}

private fun NavGraphBuilder.adminGraph(navController: NavHostController) {
    navigation(
        // The start destination of this graph is the screen that contains the Scaffold and the nested NavHost.
        startDestination = Screen.AdminMain.route,
        route = Graph.ADMIN
    ) {
        composable(route = Screen.AdminMain.route) {
            // AdminMainScreen now correctly receives the root NavController
            AdminMainScreen(rootNavController = navController)
        }
        // The other admin destinations (Users, Vouchers, Orders) are now handled
        // INSIDE AdminMainScreen.kt, so they are removed from here.
    }
}