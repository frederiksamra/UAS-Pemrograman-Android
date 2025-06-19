package com.android.laundrygo.navigation

sealed class Screen(val route: String) {
    data object Start : Screen("start_screen")
    data object Login : Screen("login_screen")
    data object Register : Screen("register_screen")
    data object Dashboard : Screen("dashboard_screen")
    data object Bag : Screen("bag_screen")
    data object Doll : Screen("doll_screen")
    data object Location : Screen("location_screen")
    data object ServiceType : Screen("service_type_screen")
    data object ShirtPants : Screen("shirt_pants_screen")
    data object Shoes : Screen("shoes_screen")
    data object SpecialTreatment : Screen("special_treatment_screen")
    data object Cart : Screen("cart_screen")
    data object Voucher : Screen("voucher_screen")
    data object TopUp : Screen("top_up_screen")
    data object Profile : Screen("profile_screen")
    data object History : Screen("history_screen")
    object HistoryDetail : Screen("history_detail/{orderId}") {
        fun createRoute(orderId: String) = "history_detail/$orderId"
    }
    data object Transaction : Screen("transaction_screen")
    data object Payment : Screen ("payment_screen")
    data object InProcess : Screen("in_process_screen")
}