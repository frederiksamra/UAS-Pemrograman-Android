package com.android.laundrygo.navigation

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
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

        composable(route = Screen.InProcess.route) {
            // InProcessViewModel butuh ServiceRepository, jadi kita gunakan factory
            val factory = InProcessViewModel.provideFactory(serviceRepository)
            val inProcessViewModel: InProcessViewModel = viewModel(factory = factory)

            InProcessScreen(
                viewModel = inProcessViewModel,
                onBackClick = { navController.navigateUp() }
            )
        }

        composable(route = Screen.Location.route) {
            LocationScreen(onBack = { navController.navigateUp() })
        }

        composable(route = Screen.TopUp.route) {
            val factory = TopUpViewModel.provideFactory(authRepository)
            val topUpViewModel: TopUpViewModel = viewModel(factory = factory)
            TopUpScreen(
                viewModel = topUpViewModel,
                onBackClick = { navController.navigateUp() }
            )
        }

        composable(route = Screen.Voucher.route) {
            // Jika VoucherViewModel butuh repository, buat dengan factory
            // val factory = VoucherViewModelFactory(repository)
            val voucherViewModel: VoucherViewModel = viewModel()
            VoucherScreen(
                onBackClick = { navController.navigateUp() },
                voucherViewModel = voucherViewModel
            )
        }

        composable(route = Screen.History.route) {
            // Jika HistoryViewModel butuh repository, buat dengan factory
            val historyViewModel: HistoryViewModel = viewModel()
            HistoryScreen(
                viewModel = historyViewModel,
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

        composable(route = Screen.Profile.route) {
            // PERBAIKAN: Panggil factory tanpa argumen, karena konstruktornya kosong.
            val factory = ProfileViewModelFactory()
            val profileViewModel: ProfileViewModel = viewModel(factory = factory)

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
                }
            )
        }

        // 3. Pastikan semua layar yang butuh data menggunakan 'repository' yang sama
        composable(route = Screen.Bag.route) { navBackStackEntry ->
            val cartViewModel = getSharedCartViewModel(navBackStackEntry)
            val bagViewModel: BagViewModel = viewModel(factory = BagViewModel.provideFactory(serviceRepository))
            val context = LocalContext.current
            BagScreen(
                viewModel = bagViewModel,
                onBack = { navController.navigateUp() },
                onCartClick = { navController.navigate(Screen.Cart.route) },
                onAddClick = { service ->
                    cartViewModel.addItem(service)
                    Toast.makeText(context, "${service.title} ditambahkan", Toast.LENGTH_SHORT).show()
                }
            )
        }

        composable(route = Screen.Doll.route) { navBackStackEntry ->
            val cartViewModel = getSharedCartViewModel(navBackStackEntry)
            val dollViewModel: DollViewModel = viewModel(factory = DollViewModel.provideFactory(serviceRepository))
            val context = LocalContext.current
            DollScreen(
                viewModel = dollViewModel,
                onBack = { navController.navigateUp() },
                onCartClick = { navController.navigate(Screen.Cart.route) },
                onAddClick = { service ->
                    cartViewModel.addItem(service)
                    Toast.makeText(context, "${service.title} ditambahkan", Toast.LENGTH_SHORT).show()
                }
            )
        }

        composable(route = Screen.ShirtPants.route) { navBackStackEntry ->
            val cartViewModel = getSharedCartViewModel(navBackStackEntry)
            val shirtPantsViewModel: ShirtPantsViewModel = viewModel(factory = ShirtPantsViewModel.provideFactory(serviceRepository))
            val context = LocalContext.current
            ShirtPantsScreen(
                viewModel = shirtPantsViewModel,
                onBack = { navController.navigateUp() },
                onCartClick = { navController.navigate(Screen.Cart.route) },
                onAddClick = { service ->
                    cartViewModel.addItem(service)
                    Toast.makeText(context, "${service.title} ditambahkan", Toast.LENGTH_SHORT).show()
                }
            )
        }

        composable(route = Screen.Shoes.route) { navBackStackEntry ->
            val cartViewModel = getSharedCartViewModel(navBackStackEntry)
            val shoesViewModel: ShoesViewModel = viewModel(factory = ShoesViewModel.provideFactory(serviceRepository))
            val context = LocalContext.current
            ShoesScreen(
                viewModel = shoesViewModel,
                onBack = { navController.navigateUp() },
                onCartClick = { navController.navigate(Screen.Cart.route) },
                onAddClick = { service ->
                    cartViewModel.addItem(service)
                    Toast.makeText(context, "${service.title} ditambahkan", Toast.LENGTH_SHORT).show()
                }
            )
        }

        composable(route = Screen.SpecialTreatment.route) { navBackStackEntry ->
            val cartViewModel = getSharedCartViewModel(navBackStackEntry)
            val specialTreatmentViewModel: SpecialTreatmentViewModel = viewModel(factory = SpecialTreatmentViewModel.provideFactory(serviceRepository))
            val context = LocalContext.current
            SpecialTreatmentScreen(
                viewModel = specialTreatmentViewModel,
                onBack = { navController.navigateUp() },
                onCartClick = { navController.navigate(Screen.Cart.route) },
                onAddClick = { service ->
                    cartViewModel.addItem(service)
                    Toast.makeText(context, "${service.title} ditambahkan", Toast.LENGTH_SHORT).show()
                }
            )
        }

        composable(route = Screen.Cart.route) { navBackStackEntry ->
            val cartViewModel = getSharedCartViewModel(navBackStackEntry)
            CartScreen(
                viewModel = cartViewModel,
                onBack = { navController.navigateUp() },
                onCheckoutClick = { totalPrice ->
                    navController.navigate("${Screen.Transaction.route}/${totalPrice.toFloat()}")
                }
            )
        }

        val KEY_TOTAL_PRICE = "total_price"
        val KEY_TRANSACTION_ID = "transaction_id"

        composable(
            route = "${Screen.Transaction.route}/{$KEY_TOTAL_PRICE}",
            arguments = listOf(navArgument(KEY_TOTAL_PRICE) { type = NavType.FloatType })
        ) { backStackEntry ->
            val price = backStackEntry.arguments?.getFloat(KEY_TOTAL_PRICE) ?: 0f
            val transactionViewModel: TransactionViewModel = viewModel(
                factory = TransactionViewModel.provideFactory(serviceRepository, price.toDouble())
            )
            TransactionScreen(
                viewModel = transactionViewModel,
                onBack = { navController.navigateUp() },
                // Kirim transactionId ke rute PaymentScreen
                onCheckoutSuccess = { transactionId ->
                    navController.navigate("${Screen.Payment.route}/$transactionId")
                }
            )
        }

        // --- PERBAIKI RUTE DAN PEMBUATAN VIEWMODEL UNTUK PAYMENTSCREEN ---
        composable(
            route = "${Screen.Payment.route}/{$KEY_TRANSACTION_ID}",
            arguments = listOf(navArgument(KEY_TRANSACTION_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getString(KEY_TRANSACTION_ID) ?: ""

            val paymentViewModel: PaymentViewModel = viewModel(
                factory = PaymentViewModelFactory(serviceRepository, transactionId)
            )

            PaymentScreen(
                onBackClicked = { navController.navigateUp() },
                onPaymentSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Graph.MAIN) { inclusive = true }
                    }
                },
                // Hubungkan callback ke navigasi yang sebenarnya
                onNavigateToTopUp = {
                    navController.navigate(Screen.TopUp.route)
                },
                paymentViewModel = paymentViewModel
            )
        }

        composable(route = Screen.Profile.route) {
            val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory())
            val isEditMode by profileViewModel.isEditMode.collectAsState()
            BackHandler(enabled = isEditMode) { profileViewModel.onCancelEdit() }
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