package com.android.laundrygo

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.android.laundrygo.navigation.AppNavigationGraph
import com.android.laundrygo.repository.AuthRepositoryImpl
import com.android.laundrygo.repository.ServiceRepositoryImpl
import com.android.laundrygo.ui.theme.LaundryGoTheme
import com.android.laundrygo.util.BiometricAuthManager
import com.android.laundrygo.viewmodel.MainViewModel
import com.google.firebase.auth.FirebaseAuth

class MainActivity : FragmentActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var biometricAuthManager: BiometricAuthManager

    // PERBAIKAN: Gunakan flag yang lebih sederhana
    private var isReturningFromBackground = false
    private lateinit var navController: androidx.navigation.NavHostController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupBiometricManager()

        val serviceRepository = ServiceRepositoryImpl()
        val authRepository = AuthRepositoryImpl()

        setContent {
            navController = rememberNavController()
            val isLocked by mainViewModel.isLocked.collectAsState()

            LaunchedEffect(isLocked) {
                if (isLocked) {
                    biometricAuthManager.showBiometricPrompt(
                        title = "Aplikasi Terkunci",
                        subtitle = "Verifikasi untuk melanjutkan",
                        description = "Gunakan sidik jari Anda untuk membuka aplikasi."
                    )
                }
            }

            LaundryGoTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AppNavigationGraph(
                            mainViewModel = mainViewModel,
                            serviceRepository = serviceRepository,
                            authRepository = authRepository,
                            navController = navController
                        )

                        if (isLocked) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.8f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = Color.White)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Menunggu verifikasi...", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Restore navigation state after setContent
        if (savedInstanceState != null) {
            val navState = savedInstanceState.getBundle("nav_state")
            navController.restoreState(navState)
        }
        mainViewModel.decideStartDestination()
    }

    override fun onPause() {
        super.onPause()
        isReturningFromBackground = true
    }

    override fun onResume() {
        super.onResume()
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null && biometricAuthManager.canAuthenticate()) {
            mainViewModel.lock()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save the state of the NavController
        navController.saveState()?.let {
            outState.putBundle("nav_state", it)
        }
    }

    private fun setupBiometricManager() {
        biometricAuthManager = BiometricAuthManager(
            activity = this,
            onAuthSuccess = { mainViewModel.unlock() },
            onAuthError = { _, _ -> finish() },
            onAuthFailed = {}
        )
    }
}