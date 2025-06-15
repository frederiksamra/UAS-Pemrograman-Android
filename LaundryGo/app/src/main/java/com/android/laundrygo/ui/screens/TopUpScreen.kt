package com.android.laundrygo.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.laundrygo.repository.AuthRepositoryImpl
import com.android.laundrygo.ui.theme.LaundryGoTheme
import com.android.laundrygo.viewmodel.TopUpState
import com.android.laundrygo.viewmodel.TopUpUiState
import com.android.laundrygo.viewmodel.TopUpViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun TopUpScreen(
    onBackClick: () -> Unit,
    topUpViewModel: TopUpViewModel = viewModel(
        factory = TopUpViewModel.provideFactory(AuthRepositoryImpl())
    )
) {
    val state by topUpViewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Menampilkan pesan error dari ViewModel menggunakan Toast
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            topUpViewModel.clearErrorMessage()
        }
    }

    // Menampilkan Snackbar saat top up berhasil (menggantikan SuccessScreen)
    LaunchedEffect(state.uiState) {
        if (state.uiState is TopUpUiState.Success) {
            snackbarHostState.showSnackbar(
                message = "Top Up Berhasil",
                duration = SnackbarDuration.Short
            )
            // Setelah snackbar selesai, kembali ke halaman sebelumnya
            topUpViewModel.finishTopUp()
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
            // Selalu tampilkan konten utama, visibility diatur oleh dialog di atasnya
            TopUpContent(
                state = state,
                onBackClick = onBackClick,
                onAmountSelected = { topUpViewModel.selectAmount(it) },
                onCustomAmountChanged = { topUpViewModel.onCustomAmountChanged(it) },
                onPaymentMethodSelected = { topUpViewModel.selectPaymentMethod(it) },
                onPayClick = { topUpViewModel.processPayment() }
            )

            // Menampilkan dialog loading saat state processing (sesuai guideline Material)
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
    onAmountSelected: (Long) -> Unit,
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
            title = { Text("Top Up") },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                Text("Pay", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}


@Composable
private fun BalanceCard(balance: Long) {
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
                text = "Current Balance",
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
    amounts: List<Long>,
    selectedAmount: Long?,
    customAmount: String,
    isCustomAmountSelected: Boolean,
    onAmountSelected: (Long) -> Unit,
    onCustomAmountChanged: (String) -> Unit
) {
    Column {
        Text(
            text = "Select Top Up Amount",
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
                    }
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
            label = { Text("Or enter other amount") },
            prefix = { Text("IDR ", color = MaterialTheme.colorScheme.onPrimary) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            shape = MaterialTheme.shapes.large,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                unfocusedLabelColor = MaterialTheme.colorScheme.onPrimary,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface
            )
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
            text = "Select Payment Method",
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
                            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.background)
                        )
                        Text(
                            text = method,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp),
                            color = MaterialTheme.colorScheme.onSurface
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
    Dialog(onDismissRequest = { /* Do nothing, user can't dismiss */ }) {
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
                CircularProgressIndicator(
                    modifier = Modifier.size(50.dp),
                    color = Color.White,
                    strokeWidth = 5.dp
                )
                Spacer(modifier = Modifier.width(20.dp))
                Text(
                    "Processing Payment...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

private fun formatCurrency(amount: Long, short: Boolean = false): String {
    val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
    format.maximumFractionDigits = 0
    val formatted = format.format(amount).replace("Rp", "IDR ")
    return if (short) {
        formatted.replace(",000", "K").replace("IDR ", "")
    } else {
        formatted
    }
}

@Preview(showBackground = true, name = "TopUp Screen")
@Composable
private fun TopUpScreenPreview() {
    LaundryGoTheme(darkTheme = false) {
        TopUpContent(
            state = TopUpState(selectedAmount = 50000),
            onBackClick = {},
            onAmountSelected = {},
            onCustomAmountChanged = {},
            onPaymentMethodSelected = {},
            onPayClick = {}
        )
    }
}