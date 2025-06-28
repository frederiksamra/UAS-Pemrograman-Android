package com.android.laundrygo.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.android.laundrygo.navigation.AdminScreen
import com.android.laundrygo.navigation.Graph
import com.android.laundrygo.ui.theme.Cream
import com.android.laundrygo.ui.theme.DarkBlue
import com.android.laundrygo.viewmodel.AdminMainViewModel
import com.android.laundrygo.viewmodel.AdminMainViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMainScreen(
    rootNavController: NavController,
    viewModel: AdminMainViewModel = viewModel(factory = AdminMainViewModelFactory())
) {
    val adminNavController = rememberNavController()
    val navigationItems = viewModel.navigationItems
    val currentScreenTitle by viewModel.title.collectAsState()
    val navBackStackEntry by adminNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    LaunchedEffect(currentDestination) {
        viewModel.onRouteChanged(currentDestination?.route)
    }

    // -- Konfirmasi Dialog untuk Logout --
    var showLogoutDialog by remember { mutableStateOf(false) }
    if (showLogoutDialog) {
        AlertDialog(
            iconContentColor = MaterialTheme.colorScheme.secondary,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurface,
            containerColor = MaterialTheme.colorScheme.surface,

            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Konfirmasi Logout") },
            text = { Text("Apakah Anda yakin ingin keluar?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout() // Panggil fungsi logout di ViewModel
                        // Navigasi kembali ke halaman awal/login
                        rootNavController.navigate(Graph.AUTHENTICATION) {
                            popUpTo(Graph.ROOT) {
                                inclusive = true
                            }
                        }
                    }
                ) {
                    Text("Logout", color = Color(0xFFFF9800))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Batal", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentScreenTitle) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.onPrimary // Warna ikon disamakan dengan judul
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = DarkBlue
            ) {
                navigationItems.forEach { screen ->
                    val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = isSelected,
                        onClick = {
                            adminNavController.navigate(screen.route) {
                                popUpTo(adminNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Cream,
                            selectedTextColor = Cream,
                            unselectedIconColor = Cream.copy(alpha = 0.7f),
                            unselectedTextColor = Cream.copy(alpha = 0.7f),
                            indicatorColor = Color.White.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = adminNavController,
            startDestination = AdminScreen.Users.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = AdminScreen.Users.route) {
                UserManagementScreen()
            }
            composable(route = AdminScreen.Vouchers.route) {
                VoucherManagementScreen()
            }
            composable(route = AdminScreen.Orders.route) {
                OrderManagementScreen()
            }
        }
    }
}
