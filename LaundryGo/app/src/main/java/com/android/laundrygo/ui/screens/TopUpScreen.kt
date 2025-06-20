package com.android.laundrygo.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.android.laundrygo.repository.AuthRepository
import com.android.laundrygo.repository.AuthRepositoryImpl
import com.android.laundrygo.ui.theme.DarkBlue
import com.android.laundrygo.ui.theme.LaundryGoTheme
import com.android.laundrygo.ui.theme.White
import com.android.laundrygo.viewmodel.TopUpState
import com.android.laundrygo.viewmodel.TopUpUiState
import com.android.laundrygo.viewmodel.TopUpViewModel
import java.text.NumberFormat
import java.util.*

@Composable
fun TopUpScreen(
    // PERBAIKAN: Terima ViewModel sebagai parameter, hapus nilai default
    viewModel: TopUpViewModel,
    onBackClick: () -> Unit
) {
    // Hapus pembuatan ViewModel dari sini
    // val topUpViewModel: TopUpViewModel = viewModel(...) <-- HAPUS

    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.clearErrorMessage()
        }
    }

    LaunchedEffect(state.uiState) {
        if (state.uiState is TopUpUiState.Success) {
            snackbarHostState.showSnackbar(
                message = "Top Up Berhasil",
                duration = SnackbarDuration.Short
            )
            viewModel.finishTopUp()
            // Menunggu snackbar selesai sebelum navigasi bisa jadi UX yang lebih baik,
            // tapi untuk sekarang ini sudah cukup.
            onBackClick()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TopUpContent(
                state = state,
                onBackClick = onBackClick,
                onAmountSelected = { viewModel.selectAmount(it) },
                onCustomAmountChanged = { viewModel.onCustomAmountChanged(it) },
                onPaymentMethodSelected = { viewModel.selectPaymentMethod(it) },
                onPayClick = { viewModel.processPayment() }
            )

            if (state.uiState is TopUpUiState.Processing) {
                ProcessingDialog()
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopUpContent(
    state: TopUpState,
    onBackClick: () -> Unit,
    onAmountSelected: (Double) -> Unit,
    onCustomAmountChanged: (String) -> Unit,
    onPaymentMethodSelected: (String) -> Unit,
    onPayClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        TopAppBar(
            title = { Text("Top Up Saldo") },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            BalanceCard(balance = state.currentBalance)
            Spacer(modifier = Modifier.height(24.dp))
            NominalSelection(
                amounts = state.topUpAmounts,
                selectedAmount = state.selectedAmount,
                customAmount = state.customAmount,
                isCustomAmountSelected = state.isCustomAmountSelected,
                onAmountSelected = onAmountSelected,
                onCustomAmountChanged = onCustomAmountChanged
            )
            Spacer(modifier = Modifier.height(24.dp))
            PaymentMethodSelection(
                methods = state.paymentMethods,
                selectedMethod = state.selectedPaymentMethod,
                onPaymentMethodSelected = onPaymentMethodSelected
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Surface(
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            FilledTonalButton(
                onClick = onPayClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(52.dp),
                enabled = (state.selectedAmount != null || state.isCustomAmountSelected) && state.selectedPaymentMethod != null,
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Bayar Top Up", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}


@Composable
private fun BalanceCard(balance: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Saldo Anda Saat Ini",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatCurrency(balance),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun NominalSelection(
    amounts: List<Double>,
    selectedAmount: Double?,
    customAmount: String,
    isCustomAmountSelected: Boolean,
    onAmountSelected: (Double) -> Unit,
    onCustomAmountChanged: (String) -> Unit
) {
    Column {
        Text(
            text = "Pilih Nominal Top Up",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(112.dp)
        ) {
            items(amounts) { amount ->
                val isSelected = (amount == selectedAmount) && !isCustomAmountSelected
                OutlinedButton(
                    onClick = { onAmountSelected(amount) },
                    shape = MaterialTheme.shapes.medium,
                    colors = if (isSelected) {
                        ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    } else {
                        ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    },
                    border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Text(text = formatCurrency(amount, true))
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = customAmount,
            onValueChange = onCustomAmountChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Atau masukkan nominal lain", color = DarkBlue) },
            prefix = { Text("Rp ") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            shape = MaterialTheme.shapes.large,
            textStyle = TextStyle(color = DarkBlue) // Try setting the color here
        )
    }
}

@Composable
private fun PaymentMethodSelection(
    methods: List<String>,
    selectedMethod: String?,
    onPaymentMethodSelected: (String) -> Unit
) {
    Column {
        Text(
            text = "Pilih Metode Pembayaran",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(Modifier.selectableGroup()) {
                methods.forEachIndexed { index, method ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .selectable(
                                selected = (method == selectedMethod),
                                onClick = { onPaymentMethodSelected(method) },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (method == selectedMethod),
                            onClick = null,
                            colors = RadioButtonDefaults.colors(selectedColor = White) // Changed selected radio button color
                        )
                        Text(
                            text = method,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                    if (index < methods.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 56.dp, end = 16.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProcessingDialog() {
    Dialog(onDismissRequest = { /* Pengguna tidak bisa menutup dialog ini */ }) {
        Card(
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.width(20.dp))
                Text(
                    "Memproses Pembayaran...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

private fun formatCurrency(amount: Double, short: Boolean = false): String {
    val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
    format.maximumFractionDigits = 0
    val formatted = format.format(amount).replace("Rp", "Rp ").trim()
    return if (short) {
        formatted.replace(",000", "K").replace("Rp ", "")
    } else {
        formatted
    }
}

@Preview(showBackground = true, name = "TopUp Screen")
@Composable
private fun TopUpScreenPreview() {
    LaundryGoTheme(darkTheme = false) {
        TopUpContent(
            state = TopUpState(selectedAmount = 50000.0),
            onBackClick = {},
            onAmountSelected = {},
            onCustomAmountChanged = {},
            onPaymentMethodSelected = {},
            onPayClick = {}
        )
    }
}