package com.android.laundrygo.navigation

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.SavedStateHandle
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
import com.android.laundrygo.ui.screens.payment.PaymentScreen
import com.android.laundrygo.viewmodel.*

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
        startDestination = Graph.AUTHENTICATION,
        route = Graph.ROOT
    ) {
        authGraph(navController)
        mainGraph(navController)
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
            LoginScreen(
                onBackClicked = { navController.navigateUp() },
                onLoginSuccess = {
                    navController.navigate(Graph.MAIN) {
                        popUpTo(Graph.AUTHENTICATION) { inclusive = true }
                    }
                },
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

private fun NavGraphBuilder.mainGraph(navController: NavHostController) {
    navigation(
        startDestination = Screen.Dashboard.route,
        route = Graph.MAIN
    ) {
        // Buat instance repository sekali untuk di-share
        val serviceRepository: ServiceRepository = ServiceRepositoryImpl()
        val authRepository: AuthRepository = AuthRepositoryImpl()

        // Helper untuk Shared CartViewModel
        val getSharedCartViewModel: @Composable (NavBackStackEntry) -> CartViewModel = { entry ->
            val parentEntry = remember(entry) { navController.getBackStackEntry(Graph.MAIN) }
            viewModel(parentEntry, factory = CartViewModelFactory(serviceRepository))
        }

        // --- Layar Utama ---
        composable(route = Screen.Dashboard.route) {
            val factory = DashboardViewModel.provideFactory(authRepository)
            val dashboardViewModel: DashboardViewModel = viewModel(factory = factory)
            DashboardScreen(
                viewModel = dashboardViewModel,
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

        // --- Pendaftaran Semua Rute Aplikasi ---
        composable(route = Screen.InProcess.route) {
            val factory = InProcessViewModel.provideFactory(serviceRepository)
            InProcessScreen(
                viewModel = viewModel(factory = factory),
                onBackClick = { navController.navigateUp() }
            )
        }

        composable(route = Screen.Location.route) {
            LocationScreen(onBack = { navController.navigateUp() })
        }

        composable(route = Screen.TopUp.route) {
            val factory = TopUpViewModel.provideFactory(authRepository)
            TopUpScreen(
                viewModel = viewModel(factory = factory),
                onBackClick = { navController.navigateUp() }
            )
        }

        composable(route = Screen.Voucher.route) {
            val factory = VoucherViewModel.provideFactory(authRepository)
            val voucherViewModel: VoucherViewModel = viewModel(factory = factory)
            VoucherScreen(
                onBackClick = { navController.popBackStack() },
                onVoucherSelected = { voucher ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("selected_voucher", voucher)
                    navController.popBackStack()
                },
                voucherViewModel = voucherViewModel
            )
        }

        composable(route = Screen.History.route) {
            val factory = HistoryViewModelFactory(serviceRepository)
            HistoryScreen(
                viewModel = viewModel(factory = factory),
                onBack = { navController.navigateUp() },
                onCheckClick = { orderId ->
                    navController.navigate(Screen.HistoryDetail.createRoute(orderId))
                }
            )
        }

        composable(route = Screen.HistoryDetail.route) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            HistoryDetailScreen(orderId = orderId, onBack = { navController.navigateUp() })
        }

        composable(route = Screen.Profile.route) {
            val factory = ProfileViewModelFactory(authRepository)
            val profileViewModel: ProfileViewModel = viewModel(factory = factory)
            ProfileScreen(
                viewModel = profileViewModel,
                onNavigateBack = { navController.navigateUp() },
                onLogout = {
                    navController.navigate(Graph.AUTHENTICATION) { popUpTo(Graph.MAIN) { inclusive = true } }
                }
            )
        }

        // --- Layar-layar Layanan ---
        composable(route = Screen.ServiceType.route) {
            ServiceTypeScreen(onBack = { navController.navigateUp() }, onServiceSelected = { serviceName ->
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
                    Screen.SpecialTreatment -> SpecialTreatmentViewModel.provideFactory(serviceRepository)
                    else -> throw IllegalArgumentException("Route tidak dikenal untuk service screen")
                }

                val localViewModel: ViewModel = viewModel(factory = factory)

                // Menampilkan layar yang sesuai berdasarkan rute
                when (screen) {
                    Screen.Bag -> BagScreen(viewModel = localViewModel as BagViewModel, onBack = { navController.navigateUp() }, onCartClick = { navController.navigate(Screen.Cart.route) }, onAddClick = { service -> cartViewModel.addItem(service); Toast.makeText(context, "${service.title} ditambahkan", Toast.LENGTH_SHORT).show() })
                    Screen.Doll -> DollScreen(viewModel = localViewModel as DollViewModel, onBack = { navController.navigateUp() }, onCartClick = { navController.navigate(Screen.Cart.route) }, onAddClick = { service -> cartViewModel.addItem(service); Toast.makeText(context, "${service.title} ditambahkan", Toast.LENGTH_SHORT).show() })
                    Screen.ShirtPants -> ShirtPantsScreen(viewModel = localViewModel as ShirtPantsViewModel, onBack = { navController.navigateUp() }, onCartClick = { navController.navigate(Screen.Cart.route) }, onAddClick = { service -> cartViewModel.addItem(service); Toast.makeText(context, "${service.title} ditambahkan", Toast.LENGTH_SHORT).show() })
                    Screen.Shoes -> ShoesScreen(viewModel = localViewModel as ShoesViewModel, onBack = { navController.navigateUp() }, onCartClick = { navController.navigate(Screen.Cart.route) }, onAddClick = { service -> cartViewModel.addItem(service); Toast.makeText(context, "${service.title} ditambahkan", Toast.LENGTH_SHORT).show() })
                    Screen.SpecialTreatment -> SpecialTreatmentScreen(viewModel = localViewModel as SpecialTreatmentViewModel, onBack = { navController.navigateUp() }, onCartClick = { navController.navigate(Screen.Cart.route) }, onAddClick = { service -> cartViewModel.addItem(service); Toast.makeText(context, "${service.title} ditambahkan", Toast.LENGTH_SHORT).show() })
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
                onCheckoutClick = { totalPrice ->
                    navController.navigate("${Screen.Transaction.route}/$totalPrice")
                }
            )
        }

        val KEY_TRANSACTION_ID = "transaction_id"

        composable(
            route = "${Screen.Transaction.route}/{totalPrice}",
            arguments = listOf(navArgument("totalPrice") { type = NavType.FloatType })
        ) { backStackEntry ->
            val price = backStackEntry.arguments?.getFloat("totalPrice") ?: 0f
            val transactionViewModel: TransactionViewModel = viewModel(
                factory = TransactionViewModel.provideFactory(serviceRepository, price.toDouble())
            )
            TransactionScreen(
                viewModel = transactionViewModel,
                onBack = { navController.navigateUp() },
                onCheckoutSuccess = { transactionId ->
                    navController.navigate("${Screen.Payment.route}/$transactionId")
                }
            )
        }

        composable(
            route = "${Screen.Payment.route}/{$KEY_TRANSACTION_ID}",
            arguments = listOf(navArgument(KEY_TRANSACTION_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getString(KEY_TRANSACTION_ID) ?: ""
            val paymentViewModel: PaymentViewModel = viewModel(
                factory = PaymentViewModelFactory(serviceRepository, authRepository, transactionId, backStackEntry.savedStateHandle)
            )
            PaymentScreen(
                onBackClicked = { navController.navigateUp() },
                onPaymentSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Graph.MAIN) { inclusive = true }
                    }
                },
                onNavigateToTopUp = { navController.navigate(Screen.TopUp.route) },
                onNavigateToVoucher = { navController.navigate(Screen.Voucher.route) },
                paymentViewModel = paymentViewModel
            )
        }
    }
}