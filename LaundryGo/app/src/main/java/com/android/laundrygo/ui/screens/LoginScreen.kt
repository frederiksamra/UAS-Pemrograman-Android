package com.android.laundrygo.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.laundrygo.viewmodel.LoginEvent
import com.android.laundrygo.viewmodel.LoginViewModel

// --- Tema Aplikasi (Warna & Tipografi) ---
// Untuk menjaga file ini mandiri, tema didefinisikan di sini.
// Dalam proyek nyata, ini biasanya ada di dalam package ui/theme.

private val DarkBlue = Color(0xFF344970)
private val DarkBlueText = Color(0xFF435585)
private val Cream = Color(0xFFF5E8C7)
private val White = Color(0xFFFFFFFF)

private val AppColorScheme = lightColorScheme(
    primary = DarkBlue,
    onPrimary = White,
    primaryContainer = Cream,
    onPrimaryContainer = DarkBlue,
    background = White,
    onBackground = DarkBlueText,
    surface = DarkBlue,
    onSurface = White,
    surfaceVariant = DarkBlueText, // Untuk outline & text kurang penting di atas surface
    onSurfaceVariant = White,
    error = Color(0xFFB00020),
    onError = White
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = MaterialTheme.typography, // Menggunakan tipografi default (Roboto) untuk readability
        content = content
    )
}


// --- Composable Utama untuk Layar Login ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onBackClicked: () -> Unit,
    onLoginSuccess: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    // Mengambil state dari ViewModel
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // LaunchedEffect untuk menangani event sekali jalan dari ViewModel
    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is LoginEvent.NavigateToHome -> {
                    Toast.makeText(context, "Login Berhasil!", Toast.LENGTH_SHORT).show()
                    onLoginSuccess()
                }
                is LoginEvent.NavigateToForgotPassword -> {
                    // Di sini Anda akan memanggil fungsi navigasi ke halaman lupa password
                    Toast.makeText(context, "Navigasi ke Lupa Password", Toast.LENGTH_SHORT).show()
                    onNavigateToForgotPassword()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Log in",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Welcome Back!",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )

                    // Error message, hanya tampil jika ada
                    if (uiState.errorMessage != null) {
                        Text(
                            text = uiState.errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    // Input Fields
                    OutlinedTextField(
                        value = uiState.username,
                        onValueChange = viewModel::onUsernameChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        label = { Text("Username") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Person, contentDescription = "Username Icon")
                        },
                        singleLine = true,
                        readOnly = uiState.isLoading,
                        isError = uiState.errorMessage != null,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedIndicatorColor = MaterialTheme.colorScheme.primaryContainer, unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            focusedLabelColor = MaterialTheme.colorScheme.primaryContainer, unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            cursorColor = MaterialTheme.colorScheme.primaryContainer,
                            focusedLeadingIconColor = MaterialTheme.colorScheme.primaryContainer, unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            errorTextColor = MaterialTheme.colorScheme.error, errorCursorColor = MaterialTheme.colorScheme.error,
                            errorIndicatorColor = MaterialTheme.colorScheme.error, errorLabelColor = MaterialTheme.colorScheme.error,
                            errorLeadingIconColor = MaterialTheme.colorScheme.error
                        )
                    )

                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = viewModel::onPasswordChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 32.dp),
                        label = { Text("Password") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = "Password Icon")
                        },
                        singleLine = true,
                        readOnly = uiState.isLoading,
                        isError = uiState.errorMessage != null,
                        visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = viewModel::onTogglePasswordVisibility) {
                                Icon(
                                    imageVector = if (uiState.isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password visibility"
                                )
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedIndicatorColor = MaterialTheme.colorScheme.primaryContainer, unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            focusedLabelColor = MaterialTheme.colorScheme.primaryContainer, unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            cursorColor = MaterialTheme.colorScheme.primaryContainer,
                            focusedLeadingIconColor = MaterialTheme.colorScheme.primaryContainer, unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            focusedTrailingIconColor = MaterialTheme.colorScheme.primaryContainer, unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            errorTextColor = MaterialTheme.colorScheme.error, errorCursorColor = MaterialTheme.colorScheme.error,
                            errorIndicatorColor = MaterialTheme.colorScheme.error, errorLabelColor = MaterialTheme.colorScheme.error,
                            errorLeadingIconColor = MaterialTheme.colorScheme.error, errorTrailingIconColor = MaterialTheme.colorScheme.error
                        )
                    )

                    // Tombol Login
                    Button(
                        onClick = viewModel::onLoginClicked,
                        enabled = !uiState.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Log in",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    TextButton(
                        onClick = viewModel::onForgotPasswordClicked,
                        enabled = !uiState.isLoading,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(
                            text = "Forgot password",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Divider "OR"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Divider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "OR", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Divider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    // Tombol Google Login
                    OutlinedButton(
                        onClick = { /* TODO: Implement Google Login */ },
                        enabled = !uiState.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Text(
                            text = "Login with Google",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

// --- Preview untuk melihat tampilan di Android Studio ---
@Preview(showBackground = true, device = "id:pixel_4")
@Composable
fun LoginScreenPreview() {
    AppTheme {
        LoginScreen(
            onBackClicked = {},
            onLoginSuccess = {},
            onNavigateToForgotPassword = {}
        )
    }
}