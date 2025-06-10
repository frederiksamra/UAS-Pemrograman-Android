package com.android.laundrygo.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import com.android.laundrygo.viewmodel.RegisterEvent
import com.android.laundrygo.viewmodel.RegisterUserEvent
import com.android.laundrygo.viewmodel.RegisterViewModel

// --- Tema Aplikasi (Warna & Tipografi) ---
// Tema ini bisa digunakan bersama dengan LoginScreen untuk konsistensi.

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
    surfaceVariant = DarkBlueText,
    onSurfaceVariant = White,
    error = Color(0xFFB00020),
    onError = White
)

// --- Composable Utama untuk Layar Registrasi ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onBackClicked: () -> Unit,
    onRegistrationSuccess: () -> Unit,
    viewModel: RegisterViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is RegisterEvent.RegistrationSuccess -> {
                    Toast.makeText(context, "Registrasi Berhasil!", Toast.LENGTH_SHORT).show()
                    onRegistrationSuccess()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Register",
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()), // Penting agar form bisa di-scroll
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp) // Memberi jarak antar field
                ) {
                    Text(
                        text = "New Here?",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )

                    if (uiState.errorMessage != null) {
                        Text(
                            text = uiState.errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    // Menggunakan fungsi bantuan untuk membuat TextField agar tidak repetitif
                    RegisterTextField(
                        value = uiState.name,
                        onValueChange = { viewModel.onEvent(RegisterUserEvent.NameChanged(it)) },
                        label = "Name",
                        icon = Icons.Default.Badge,
                        isError = uiState.errorMessage != null,
                        isLoading = uiState.isLoading
                    )

                    RegisterTextField(
                        value = uiState.email,
                        onValueChange = { viewModel.onEvent(RegisterUserEvent.EmailChanged(it)) },
                        label = "Email",
                        icon = Icons.Default.Email,
                        keyboardType = KeyboardType.Email,
                        isError = uiState.errorMessage != null,
                        isLoading = uiState.isLoading
                    )

                    RegisterTextField(
                        value = uiState.phone,
                        onValueChange = { viewModel.onEvent(RegisterUserEvent.PhoneChanged(it)) },
                        label = "Phone Number",
                        icon = Icons.Default.Phone,
                        keyboardType = KeyboardType.Phone,
                        isError = uiState.errorMessage != null,
                        isLoading = uiState.isLoading
                    )

                    RegisterTextField(
                        value = uiState.address,
                        onValueChange = { viewModel.onEvent(RegisterUserEvent.AddressChanged(it)) },
                        label = "Address",
                        icon = Icons.Default.Home,
                        isError = uiState.errorMessage != null,
                        isLoading = uiState.isLoading
                    )

                    RegisterTextField(
                        value = uiState.username,
                        onValueChange = { viewModel.onEvent(RegisterUserEvent.UsernameChanged(it)) },
                        label = "Username",
                        icon = Icons.Default.Person,
                        isError = uiState.errorMessage != null,
                        isLoading = uiState.isLoading
                    )

                    // TextField khusus untuk Password
                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = { viewModel.onEvent(RegisterUserEvent.PasswordChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, "Password Icon") },
                        readOnly = uiState.isLoading,
                        isError = uiState.errorMessage != null,
                        visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { viewModel.onEvent(RegisterUserEvent.TogglePasswordVisibility) }) {
                                Icon(
                                    imageVector = if (uiState.isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password visibility"
                                )
                            }
                        },
                        colors = registerTextFieldColors()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Tombol "Get Started"
                    Button(
                        onClick = { viewModel.onEvent(RegisterUserEvent.RegisterClicked) },
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
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            } else {
                                Text(
                                    "Get Started",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Fungsi bantuan untuk mengurangi duplikasi kode TextField
@Composable
private fun RegisterTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean,
    isLoading: Boolean
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        leadingIcon = { Icon(icon, "$label Icon") },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        readOnly = isLoading,
        isError = isError,
        colors = registerTextFieldColors()
    )
}

// Fungsi bantuan untuk warna TextField agar konsisten
@Composable
private fun registerTextFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
    focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    focusedIndicatorColor = MaterialTheme.colorScheme.primaryContainer, unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant,
    focusedLabelColor = MaterialTheme.colorScheme.primaryContainer, unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    cursorColor = MaterialTheme.colorScheme.primaryContainer,
    focusedLeadingIconColor = MaterialTheme.colorScheme.primaryContainer, unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    errorCursorColor = MaterialTheme.colorScheme.error, errorIndicatorColor = MaterialTheme.colorScheme.error,
    errorLabelColor = MaterialTheme.colorScheme.error, errorLeadingIconColor = MaterialTheme.colorScheme.error
)


@Preview(showBackground = true, device = "id:pixel_4")
@Composable
fun RegisterScreenPreview() {
    AppTheme {
        RegisterScreen(onBackClicked = {}, onRegistrationSuccess = {})
    }
}