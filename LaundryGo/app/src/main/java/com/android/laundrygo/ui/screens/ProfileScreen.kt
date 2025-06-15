package com.android.laundrygo.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.laundrygo.ui.theme.LaundryGoTheme
import com.android.laundrygo.viewmodel.ProfileEvent
import com.android.laundrygo.viewmodel.ProfileViewModel
import com.android.laundrygo.viewmodel.ProfileViewModelFactory


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory())
) {
    val uiState by viewModel.uiState.collectAsState()
    val isEditMode by viewModel.isEditMode.collectAsState()
    val editState by viewModel.editState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                ProfileEvent.NavigateToLogin -> {
                    Toast.makeText(context, "Anda telah logout", Toast.LENGTH_SHORT).show()
                    onLogout()
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        topBar = {
            TopAppBar(
                title = { Text("My Profile", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isEditMode) {
                            viewModel.onCancelEdit()
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(
                            imageVector = if (isEditMode) Icons.Default.Close else Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if (isEditMode) "Cancel Edit" else "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading && !isEditMode) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                ProfileInitialCircle(name = uiState.name)
                Spacer(modifier = Modifier.height(8.dp))

                // ✅ PERBAIKAN: Salin errorMessage ke variabel lokal terlebih dahulu
                val errorMessage = uiState.errorMessage
                if (errorMessage != null) {
                    Text(
                        text = errorMessage, // Gunakan variabel lokal yang sudah pasti non-null di sini
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                if (isEditMode) {
                    OutlinedTextField(
                        value = editState.name,
                        onValueChange = { viewModel.onNameChange(it) },
                        label = { Text("Nama Lengkap") },
                        singleLine = true,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                } else {
                    Text(
                        text = uiState.name,
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "@${uiState.username}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (isEditMode) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { viewModel.onSaveEdit() }) {
                            Icon(Icons.Default.Done, contentDescription = "Simpan")
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text("Simpan")
                        }
                        OutlinedButton(onClick = { viewModel.onCancelEdit() }) {
                            Text("Batal")
                        }
                    }
                } else {
                    Button(onClick = { viewModel.onEnterEditMode() }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Profile", modifier = Modifier.size(ButtonDefaults.IconSize))
                        Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Edit Profile")
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    ProfileInfoRow(
                        isEditMode = isEditMode,
                        icon = Icons.Default.Email,
                        label = "Email",
                        value = if (isEditMode) editState.email else uiState.email,
                        onValueChange = { viewModel.onEmailChange(it) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal=16.dp))
                    ProfileInfoRow(
                        isEditMode = isEditMode,
                        icon = Icons.Default.Phone,
                        label = "Phone Number",
                        value = if (isEditMode) editState.phone else uiState.phone,
                        onValueChange = { viewModel.onPhoneChange(it) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal=16.dp))
                    ProfileInfoRow(
                        isEditMode = isEditMode,
                        icon = Icons.Default.Home,
                        label = "Address",
                        value = if (isEditMode) editState.address else uiState.address,
                        onValueChange = { viewModel.onAddressChange(it) },
                        singleLine = false
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                AnimatedVisibility(!isEditMode) {
                    OutlinedButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth()
                            .height(50.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                    ) {
                        Text(text = "Log out", color = MaterialTheme.colorScheme.error)
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(
    isEditMode: Boolean,
    icon: ImageVector,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    singleLine: Boolean = true
) {
    if (isEditMode) {
        // Tampilan saat mode edit: TextField di dalam padding
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            leadingIcon = { Icon(icon, contentDescription = label) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            singleLine = singleLine
        )
    } else {
        // Tampilan normal: ListItem
        ListItem(
            headlineContent = { Text(value) },
            supportingContent = { Text(label) },
            leadingContent = {
                Icon(
                    imageVector = icon,
                    contentDescription = label
                    // `tint` sudah dihapus, ListItem akan memberinya warna secara otomatis
                )
            },
            colors = themedListItemColors()
        )
    }
}

@Composable
private fun ProfileInitialCircle(name: String, modifier: Modifier = Modifier) {
    val initial = name.firstOrNull()?.uppercase() ?: ""
    Box(
        modifier = modifier
            .size(120.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun themedListItemColors() = ListItemDefaults.colors(
    containerColor = Color.Transparent,
    headlineColor = MaterialTheme.colorScheme.onSurface,
    supportingColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
    leadingIconColor = MaterialTheme.colorScheme.onSurface
)

@Preview(showBackground = true, name = "Display Mode")
@Composable
fun ProfileScreenPreview() {
    LaundryGoTheme {
        ProfileScreen(onNavigateBack = {}, onLogout = {})
    }
}
