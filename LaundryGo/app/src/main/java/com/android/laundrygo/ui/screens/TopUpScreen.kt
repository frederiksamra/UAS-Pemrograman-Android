package com.android.laundrygo.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.laundrygo.viewmodel.TopUpState
import com.android.laundrygo.viewmodel.TopUpUiState
import com.android.laundrygo.viewmodel.TopUpViewModel
import java.text.NumberFormat
import java.util.Locale
import com.android.laundrygo.ui.theme.AppPrimaryColor
import com.android.laundrygo.ui.theme.AppScreenBackground
import com.android.laundrygo.ui.theme.AppSecondaryColor
import com.android.laundrygo.ui.theme.AppTextColorOnDark
import com.android.laundrygo.ui.theme.DarkBlue
import com.android.laundrygo.ui.theme.DarkBlueText


// Fungsi utama yang mengontrol tampilan berdasarkan state
@Composable
fun TopUpScreen(
    onBackClick: () -> Unit,
    topUpViewModel: TopUpViewModel = viewModel()
) {
    val state by topUpViewModel.state.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        // Tampilan akan berganti dengan animasi fade-in/out
        AnimatedVisibility(
            visible = state.uiState is TopUpUiState.Idle,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            TopUpContent(
                state = state,
                onBackClick = onBackClick,
                onAmountSelected = { topUpViewModel.selectAmount(it) },
                onPaymentMethodSelected = { topUpViewModel.selectPaymentMethod(it) },
                onPayClick = { topUpViewModel.processPayment() }
            )
        }

        AnimatedVisibility(
            visible = state.uiState is TopUpUiState.Processing,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            ProcessingScreen()
        }

        AnimatedVisibility(
            visible = state.uiState is TopUpUiState.Success,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            SuccessScreen(onDoneClick = {
                topUpViewModel.finishTopUp()
                onBackClick() // Kembali ke halaman sebelumnya setelah selesai
            })
        }
    }
}


// Composable untuk UI utama pengisian Top Up
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopUpContent(
    state: TopUpState,
    onBackClick: () -> Unit,
    onAmountSelected: (Long) -> Unit,
    onPaymentMethodSelected: (String) -> Unit,
    onPayClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Top Up", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        // WARNA DISESUAIKAN: Gunakan background yang serasi dengan layar lain
        containerColor = AppScreenBackground,
        bottomBar = {
            Button(
                onClick = onPayClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(50.dp),

                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkBlue,
                    contentColor = Color.White
                )
            ){

                Text("Pay", fontSize = 18.sp)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            BalanceCard(balance = state.currentBalance)
            Spacer(modifier = Modifier.height(24.dp))
            NominalSelection(
                amounts = state.topUpAmounts,
                selectedAmount = state.selectedAmount,
                onAmountSelected = onAmountSelected
            )
            Spacer(modifier = Modifier.height(24.dp))
            PaymentMethodSelection(
                methods = state.paymentMethods,
                selectedMethod = state.selectedPaymentMethod,
                onPaymentMethodSelected = onPaymentMethodSelected
            )
        }
    }
}


@Composable
private fun BalanceCard(balance: Long) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        // WARNA DISESUAIKAN: Latar belakang kartu menggunakan surface (biasanya putih/putih gading)
        colors = CardDefaults.cardColors(containerColor = AppTextColorOnDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Current Balance",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatCurrency(balance),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                // WARNA DISESUAIKAN: Teks saldo menonjol dengan warna primer
                color = AppPrimaryColor
            )
        }
    }
}

@Composable
private fun NominalSelection(
    amounts: List<Long>,
    selectedAmount: Long?,
    onAmountSelected: (Long) -> Unit
) {
    Column {
        Text(
            text = "Select Top Up Amount",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(amounts) { amount ->
                val isSelected = amount == selectedAmount
                OutlinedButton(
                    onClick = { onAmountSelected(amount) },
                    shape = MaterialTheme.shapes.medium,
                    colors = if (isSelected) {
                        ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    } else {
                        ButtonDefaults.outlinedButtonColors()
                    }
                ) {
                    Text(text = formatCurrency(amount, true), color = AppSecondaryColor)
                }
            }
        }
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
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(Modifier.selectableGroup()) {
                methods.forEachIndexed { index, method ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .background(AppTextColorOnDark)
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
                            onClick = null // null recommended for accessibility with selectable
                        )
                        Text(
                            text = method,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                    if (index < methods.lastIndex) {
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }
    }
}


// --- Layar Tambahan (Processing & Success) ---

@Composable
private fun ProcessingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            // WARNA DISESUAIKAN: Gunakan warna sekunder yang lebih gelap
            .background(AppSecondaryColor),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = Color.White)
            Spacer(modifier = Modifier.height(24.dp))
            Text("Processing Payment", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Please wait a moment...", fontSize = 16.sp, color = Color.White.copy(alpha = 0.8f))
        }
    }
}

@Composable
private fun SuccessScreen(onDoneClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            // WARNA DISESUAIKAN: Gunakan warna primer untuk nuansa yang positif
            .background(AppPrimaryColor),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
            Text("✅", fontSize = 80.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Top Up Successful",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onDoneClick,
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(52.dp),
                shape = RoundedCornerShape(50),
                // WARNA DISESUAIKAN: Tombol dengan warna kontras
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppScreenBackground,
                    contentColor = AppPrimaryColor
                )
            ) {
                Text("Done", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
// --- Fungsi utilitas ---
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

@Preview(showBackground = true)
@Composable
private fun TopUpScreenPreview() {
    TopUpContent(
        state = TopUpState(),
        onBackClick = {},
        onAmountSelected = {},
        onPaymentMethodSelected = {},
        onPayClick = {}
    )
}