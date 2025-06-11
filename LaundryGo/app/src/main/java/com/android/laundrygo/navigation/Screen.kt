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
}