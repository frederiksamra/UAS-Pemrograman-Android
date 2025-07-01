package com.android.laundrygo.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.android.laundrygo.navigation.AdminScreen
import com.android.laundrygo.navigation.Graph
import com.android.laundrygo.ui.theme.Cream
import com.android.laundrygo.ui.theme.DarkBlue
import com.android.laundrygo.util.BiometricAuthManager
import com.android.laundrygo.viewmodel.AdminMainViewModel
import com.android.laundrygo.viewmodel.AdminMainViewModelFactory
import com.android.laundrygo.viewmodel.BiometricAuthState

@SuppressLint("ContextCastToActivity")
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
                        viewModel.logout()
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

    // UI utama akan terkunci sampai otentikasi berhasil
    Box(modifier = Modifier.fillMaxSize()) {
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
                                tint = MaterialTheme.colorScheme.onPrimary
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
}