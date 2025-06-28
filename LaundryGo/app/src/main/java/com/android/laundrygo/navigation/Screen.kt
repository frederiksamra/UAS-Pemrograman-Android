package com.android.laundrygo.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class Screen(val route: String) {

    // --- Rute untuk Auth Graph ---
    data object Start : Screen("start_screen")
    data object Login : Screen("login_screen")
    data object Register : Screen("register_screen")
    data object AdminMain : Screen("admin_main_screen")

    // --- Rute untuk Main Graph (tanpa argumen) ---
    data object Dashboard : Screen("dashboard_screen")
    data object ServiceType : Screen("service_type_screen")
    data object Location : Screen("location_screen")
    data object Cart : Screen("cart_screen")
    data object Voucher : Screen("voucher_screen")
    data object Profile : Screen("profile_screen")
    data object TopUp : Screen("top_up_screen")
    data object History : Screen("history_screen")
    data object InProcess : Screen("in_process_screen")

    // --- Rute untuk Layanan Spesifik (tanpa argumen) ---
    data object ShirtPants : Screen("shirt_pants_screen")
    data object SpecialTreatment : Screen("special_treatment_screen")
    data object Doll : Screen("doll_screen")
    data object Bag : Screen("bag_screen")
    data object Shoes : Screen("shoes_screen")

    // --- Rute dengan Argumen ---

    data object HistoryDetail : Screen("history_detail_screen") {
        // 1. Definisikan nama argumen
        const val ORDER_ID_ARG = "orderId"
        // 2. Definisikan rute lengkap dengan placeholder
        val routeWithArgs = "$route/{$ORDER_ID_ARG}"
        // 3. Definisikan tipe argumennya
        val arguments = listOf(
            navArgument(ORDER_ID_ARG) { type = NavType.StringType }
        )
        // 4. Fungsi untuk membuat rute dengan nilai nyata
        fun createRoute(orderId: String) = "$route/$orderId"
    }

    data object Transaction : Screen("transaction_screen") {
        // Properti & fungsi yang dibutuhkan oleh AppNavigationGraph.kt
        const val totalPriceArg = "totalPrice"
        val routeWithArgs = "$route/{$totalPriceArg}"
        val arguments = listOf(
            navArgument(totalPriceArg) { type = NavType.FloatType }
        )
        fun createRoute(totalPrice: Float) = "$route/$totalPrice"
    }

    data object Payment : Screen("payment_screen") {
        // Properti & fungsi yang dibutuhkan oleh AppNavigationGraph.kt
        const val transactionIdArg = "transactionId"
        val routeWithArgs = "$route/{$transactionIdArg}"
        val arguments = listOf(
            navArgument(transactionIdArg) { type = NavType.StringType }
        )
        fun createRoute(transactionId: String) = "$route/$transactionId"
    }
}

sealed class AdminScreen(val route: String, val label: String, val icon: ImageVector) {
    object Users : AdminScreen("admin_users_screen", "Pengguna", Icons.Default.Group)
    object Vouchers : AdminScreen("admin_vouchers_screen", "Voucher", Icons.Default.Sell)
    object Orders : AdminScreen("admin_orders_screen", "Pesanan", Icons.Default.ReceiptLong)
}