package com.android.laundrygo.ui.screens.payment

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.laundrygo.model.CartItem
import com.android.laundrygo.model.LaundryService
import com.android.laundrygo.model.Transaction
import com.android.laundrygo.model.User
import com.android.laundrygo.model.Voucher
import com.android.laundrygo.repository.AuthRepository
import com.android.laundrygo.repository.ServiceRepository
import com.android.laundrygo.ui.theme.Cream
import com.android.laundrygo.ui.theme.DarkBlue
import com.android.laundrygo.ui.theme.LaundryGoTheme
import com.android.laundrygo.viewmodel.PaymentStatus
import com.android.laundrygo.viewmodel.PaymentUiState
import com.android.laundrygo.viewmodel.PaymentViewModel
import com.android.laundrygo.viewmodel.PaymentViewModelFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.text.NumberFormat
import java.util.*

// --- FUNGSI UTAMA (STATEFUL) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    onBackClicked: () -> Unit,
    onPaymentSuccess: () -> Unit,
    onNavigateToTopUp: () -> Unit,
    onNavigateToVoucher: () -> Unit,
    paymentViewModel: PaymentViewModel
) {
    val uiState by paymentViewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.paymentStatus) {
        if (uiState.paymentStatus == PaymentStatus.SUCCESS) {
            Toast.makeText(context, "Pembayaran Berhasil!", Toast.LENGTH_SHORT).show()
            onPaymentSuccess()
        } else if (uiState.paymentStatus == PaymentStatus.ERROR) {
            val errorMessage = uiState.error ?: "Pembayaran Gagal!"
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            paymentViewModel.clearErrorStatus()
        }
    }

    if (uiState.showInsufficientBalanceDialog) {
        AlertDialog(
            onDismissRequest = { paymentViewModel.dismissInsufficientBalanceDialog() },
            title = { Text("Saldo Tidak Cukup") },
            text = { Text("Saldo Anda tidak cukup untuk melakukan transaksi ini. Apakah Anda ingin melakukan Top Up sekarang?") },
            confirmButton = {
                Button(onClick = {
                    paymentViewModel.dismissInsufficientBalanceDialog()
                    onNavigateToTopUp()
                }) { Text("Top Up") }
            },
            dismissButton = {
                TextButton(onClick = { paymentViewModel.dismissInsufficientBalanceDialog() }) { Text("Batal") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invoice Pembayaran", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        },
        bottomBar = {
            PayButton(
                isEnabled = uiState.selectedPaymentMethod != null && uiState.paymentStatus != PaymentStatus.LOADING,
                isLoading = uiState.paymentStatus == PaymentStatus.LOADING,
                onClick = {
                    val currentBalance = uiState.userBalance // Create a local immutable copy
                    if (uiState.selectedPaymentMethod == "Balance" &&
                        (currentBalance == null || currentBalance < uiState.finalAmount)
                    ) {
                        onNavigateToTopUp() // Navigate to Top Up directly
                    } else {
                        paymentViewModel.onPayClicked() // Proceed with payment
                    }
                }
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.error != null && uiState.paymentStatus != PaymentStatus.ERROR) {
                Text(
                    text = "Error: ${uiState.error}",
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                    textAlign = TextAlign.Center
                )
            } else {
                PaymentScreenContent(
                    uiState = uiState,
                    onMethodSelected = { paymentViewModel.onPaymentMethodSelected(it) },
                    onNavigateToVoucher = onNavigateToVoucher,
                    onRemoveVoucher = { paymentViewModel.onVoucherSelected(null) }
                )
            }
        }
    }
}


// --- KUMPULAN COMPOSABLE UNTUK UI (STATELESS) ---

@Composable
private fun PaymentScreenContent(
    uiState: PaymentUiState,
    onMethodSelected: (String) -> Unit,
    onNavigateToVoucher: () -> Unit,
    onRemoveVoucher: () -> Unit
) {
    val darkBlue = Color(0xFF3A4A7A)
    val lightGray = Color(0xFFF0F0F0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        uiState.userBalance?.let { balance ->
            BalanceInfoCard(balance = balance)
            Spacer(modifier = Modifier.height(24.dp))
        }

        TotalPriceCard(totalPrice = uiState.finalAmount, backgroundColor = darkBlue)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "ID Transaksi: ${uiState.transactionId.take(8).uppercase()}",
            color = Color.Gray,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelMedium
        )
        Spacer(modifier = Modifier.height(24.dp))
        OrderDetails(
            items = uiState.orderItems,
            subtotal = uiState.subtotal,
            discount = uiState.discountAmount,
            headerColor = lightGray
        )
        Spacer(modifier = Modifier.height(24.dp))

        VoucherSection(
            appliedVoucher = uiState.appliedVoucher,
            onNavigateToVoucher = onNavigateToVoucher,
            onRemoveVoucher = onRemoveVoucher
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Pilih Metode Pembayaran",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        // --- PERBAIKAN DI SINI ---
        PaymentMethodSelection(
            methods = uiState.availablePaymentMethods, // <-- Hapus .map{ it.first }
            selectedMethod = uiState.selectedPaymentMethod,
            userBalance = uiState.userBalance,
            totalPrice = uiState.finalAmount,
            onMethodSelected = onMethodSelected,
            selectedColor = darkBlue.copy(alpha = 0.1f),
            selectedBorderColor = darkBlue
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}


@Composable
private fun VoucherSection(
    appliedVoucher: Voucher?,
    onNavigateToVoucher: () -> Unit,
    onRemoveVoucher: () -> Unit
) {
    Column {
        Text(
            "Voucher",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        OutlinedCard(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onNavigateToVoucher),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (appliedVoucher == null) {
                    Text("Gunakan Voucher", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Pilih Voucher")
                } else {
                    Column(Modifier.weight(1f)) {
                        Text("Voucher Digunakan:", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.8f))
                        Text(appliedVoucher.voucher_code, fontWeight = FontWeight.Bold, color = Cream)
                    }
                    IconButton(onClick = onRemoveVoucher, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Hapus Voucher")
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderDetails(items: List<CartItem>, subtotal: Double, discount: Double, headerColor: Color) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFFAFAFA))
            .border(1.dp, headerColor, RoundedCornerShape(8.dp))
    ) {
        Row(
            modifier = Modifier.background(headerColor).padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text("Produk", Modifier.weight(2f), fontWeight = FontWeight.Bold)
            Text("Qty", Modifier.weight(0.5f), textAlign = TextAlign.End, fontWeight = FontWeight.Bold)
            Text("Subtotal", Modifier.weight(1.5f), textAlign = TextAlign.End, fontWeight = FontWeight.Bold)
        }
        items.forEach { item ->
            Row(
                modifier = Modifier.background(Color.White).padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(item.name, Modifier.weight(2f), fontSize = 14.sp, lineHeight = 18.sp)
                Text(item.quantity.toString(), Modifier.weight(0.5f), textAlign = TextAlign.End, fontSize = 14.sp)
                Text(formatRupiah(item.price * item.quantity, false), Modifier.weight(1.5f), textAlign = TextAlign.End, fontSize = 14.sp)
            }
        }
        HorizontalDivider(color = headerColor)
        Column(
            modifier = Modifier.background(Color.White).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SummaryRow("Subtotal", formatRupiah(subtotal))
            if (discount > 0) {
                SummaryRow("Diskon Voucher", "- ${formatRupiah(discount)}", valueColor = Color(0xFF1F9E56))
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, valueColor: Color = LocalContentColor.current) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray, fontSize = 14.sp)
        Text(value, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = valueColor)
    }
}

@Composable
private fun PayButton(isEnabled: Boolean, isLoading: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = isEnabled,
        modifier = Modifier.fillMaxWidth().padding(16.dp).height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledContainerColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.background,
                strokeWidth = 2.dp
            )
        } else {
            Text("Pay", color = MaterialTheme.colorScheme.background, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun PaymentMethodSelection(
    methods: List<String>,
    selectedMethod: String?,
    userBalance: Double?,
    totalPrice: Double,
    onMethodSelected: (String) -> Unit,
    selectedColor: Color,
    selectedBorderColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        methods.forEach { methodName ->
            val isSelected = methodName == selectedMethod
            // --- MODIFICATION HERE ---
            val isBalanceOption = methodName == "Balance"
            val isEnabled = true // Always enable the options
            val insufficientBalance = isBalanceOption && userBalance != null && userBalance < totalPrice

            val backgroundColor = when {
                isSelected -> selectedColor
                !isEnabled && !isBalanceOption -> Color.LightGray.copy(alpha = 0.5f)
                isBalanceOption && insufficientBalance -> Color.LightGray.copy(alpha = 0.3f) // Visual cue for insufficient balance
                else -> Color(0xFFF5F5F5)
            }
            val contentColor = when {
                isSelected -> selectedBorderColor
                !isEnabled && !isBalanceOption -> Color.Gray
                isBalanceOption && insufficientBalance -> Color.Gray
                else -> Color.Black
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(backgroundColor)
                    .border(
                        width = if (isSelected) 1.5.dp else 0.dp,
                        color = if (isSelected) selectedBorderColor else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable(enabled = isEnabled) { onMethodSelected(methodName) },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) {
                    Text(methodName, fontWeight = FontWeight.SemiBold, color = contentColor)
                    if (isBalanceOption && userBalance != null) {
                        Text(formatRupiah(userBalance), color = contentColor)
                    }
                }
            }
        }
    }
}

@Composable
private fun BalanceInfoCard(balance: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Cream)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Saldo Anda", fontWeight = FontWeight.Medium)
        Text(formatRupiah(balance), fontWeight = FontWeight.Bold, color = DarkBlue)
    }
}

@Composable
private fun TotalPriceCard(totalPrice: Double, backgroundColor: Color) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Total Pembayaran", color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp)
            Text(formatRupiah(totalPrice), color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// --- FUNGSI HELPER HARUS DI LUAR COMPOSABLE LAIN ---
private fun formatRupiah(amount: Double, withPrefix: Boolean = true): String {
    val localeID = Locale("in", "ID")
    val format = NumberFormat.getCurrencyInstance(localeID)
    format.maximumFractionDigits = 0
    return format.format(amount)
}

// --- PERBAIKAN UTAMA DI PREVIEW ---
//@Preview(showBackground = true, name = "Default State")
//@Composable
//fun PaymentScreenPreview() {
//    // 1. Buat FakeRepository yang mengembalikan data Transaksi
//    class FakePaymentRepository : ServiceRepository {
//        override suspend fun getTransactionById(transactionId: String): Result<Transaction?> {
//            val dummyItems = listOf(
//                CartItem("1", "Deep Cleaning Shoes", "...", 25000.0, 2),
//                CartItem("2", "Special Long Dress", "...", 35000.0, 1)
//            )
//            val dummyTransaction = Transaction(
//                id = transactionId,
//                customerName = "John Doe",
//                pickupDate = "Jumat, 20 Juni 2025",
//                pickupTime = "10:00",
//                items = dummyItems,
//                totalPrice = 85000.0,
//                createdAt = Date()
//            )
//            return Result.success(dummyTransaction)
//        }
//        // Implementasi fungsi lain (bisa kosong)
//        override suspend fun getServices(category: String): Result<List<LaundryService>> = Result.success(emptyList())
//        override fun getCartItems(userId: String): Flow<Result<List<CartItem>>> = flowOf(Result.success(emptyList()))
//        override fun addItemToCart(userId: String, service: LaundryService): Flow<Result<Unit>> = flowOf(Result.success(Unit))
//        override fun removeItemFromCart(userId: String, itemId: String): Flow<Result<Unit>> = flowOf(Result.success(Unit))
//        override fun updateItemQuantity(userId: String, itemId: String, change: Int): Flow<Result<Unit>> = flowOf(Result.success(Unit))
//        override suspend fun createTransaction(transaction: Transaction): Result<String> = Result.success("tx-123")
//        override suspend fun clearCart(userId: String): Result<Unit> = Result.success(Unit)
//    }
//
//    // 2. Buat ViewModel dengan Factory dan FakeRepository
//    val factory = PaymentViewModelFactory(FakePaymentRepository(), "tx-preview-123")
//    val previewViewModel: PaymentViewModel = viewModel(factory = factory)
//
//    // 3. Gunakan di Composable
//    LaundryGoTheme {
//        PaymentScreen(
//            onBackClicked = { },
//            onPaymentSuccess = { },
//            paymentViewModel = previewViewModel
//        )
//    }
//}