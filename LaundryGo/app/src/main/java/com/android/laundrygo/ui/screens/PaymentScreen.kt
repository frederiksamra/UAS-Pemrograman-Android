package com.android.laundrygo.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.laundrygo.model.CartItem
import com.android.laundrygo.ui.theme.Cream
import com.android.laundrygo.ui.theme.DarkBlue
import com.android.laundrygo.viewmodel.PaymentStatus
import com.android.laundrygo.viewmodel.PaymentUiState
import com.android.laundrygo.viewmodel.PaymentViewModel
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    onBackClicked: () -> Unit,
    onPaymentSuccess: () -> Unit,
    onNavigateToTopUp: () -> Unit, // <-- TAMBAHKAN PARAMETER NAVIGASI BARU
    paymentViewModel: PaymentViewModel
) {
    val uiState by paymentViewModel.uiState.collectAsState()
    val context = LocalContext.current

    // LaunchedEffect untuk menangani Toast (sudah benar)
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

    // --- TAMBAHKAN DIALOG DI SINI ---
    if (uiState.showInsufficientBalanceDialog) {
        AlertDialog(
            onDismissRequest = { paymentViewModel.dismissInsufficientBalanceDialog() },
            title = { Text("Saldo Tidak Cukup") },
            text = { Text("Saldo Anda tidak cukup untuk melakukan transaksi ini. Apakah Anda ingin melakukan Top Up sekarang?") },
            confirmButton = {
                Button(
                    onClick = {
                        paymentViewModel.dismissInsufficientBalanceDialog()
                        onNavigateToTopUp() // Panggil navigasi ke TopUp
                    }
                ) {
                    Text("Top Up")
                }
            },
            dismissButton = {
                TextButton(onClick = { paymentViewModel.dismissInsufficientBalanceDialog() }) {
                    Text("Batal")
                }
            },
            containerColor = Cream
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
                onClick = { paymentViewModel.onPayClicked() }
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        // Menangani state Loading dan Error
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.error != null) {
                Text(
                    text = "Error: ${uiState.error}",
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                    textAlign = TextAlign.Center
                )
            } else {
                PaymentScreenContent(
                    uiState = uiState,
                    onMethodSelected = { paymentViewModel.onPaymentMethodSelected(it) }
                )
            }
        }
    }
}

@Composable
private fun PaymentScreenContent(
    uiState: PaymentUiState,
    onMethodSelected: (String) -> Unit
) {
    val darkBlue = Color(0xFF3A4A7A)
    val lightGray = Color(0xFFF0F0F0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Tampilkan saldo jika tersedia
        uiState.userBalance?.let { balance ->
            BalanceInfoCard(balance = balance)
            Spacer(modifier = Modifier.height(24.dp))
        }

        TotalPriceCard(totalPrice = uiState.totalPrice, backgroundColor = darkBlue)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "ID Transaksi: ${uiState.transactionId}",
            color = Color.Gray,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelMedium
        )
        Spacer(modifier = Modifier.height(24.dp))
        OrderDetails(
            items = uiState.orderItems,
            headerColor = lightGray
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Pilih Metode Pembayaran",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        PaymentMethodSelection(
            methods = uiState.availablePaymentMethods,
            selectedMethod = uiState.selectedPaymentMethod,
            userBalance = uiState.userBalance,
            totalPrice = uiState.totalPrice,
            onMethodSelected = onMethodSelected,
            selectedColor = darkBlue.copy(alpha = 0.1f),
            selectedBorderColor = darkBlue
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun BalanceInfoCard(balance: Double) {
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
fun TotalPriceCard(totalPrice: Double, backgroundColor: Color) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Total Pembayaran",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 16.sp
            )
            Text(
                formatRupiah(totalPrice),
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// PERBAIKAN: Menggunakan List<CartItem>
@Composable
fun OrderDetails(items: List<CartItem>, headerColor: Color) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFFAFAFA))
            .border(1.dp, headerColor, RoundedCornerShape(8.dp))
    ) {
        Row(
            modifier = Modifier
                .background(headerColor)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text("Produk", Modifier.weight(2f), fontWeight = FontWeight.Bold)
            Text("Harga", Modifier.weight(1.5f), textAlign = TextAlign.End, fontWeight = FontWeight.Bold)
            Text("Qty", Modifier.weight(0.5f), textAlign = TextAlign.End, fontWeight = FontWeight.Bold)
            Text("Subtotal", Modifier.weight(1.5f), textAlign = TextAlign.End, fontWeight = FontWeight.Bold)
        }
        items.forEach { item ->
            Row(
                modifier = Modifier
                    .background(Color.White)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(item.name, Modifier.weight(2f), fontSize = 14.sp)
                Text(formatRupiah(item.price, false), Modifier.weight(1.5f), textAlign = TextAlign.End, fontSize = 14.sp)
                Text(item.quantity.toString(), Modifier.weight(0.5f), textAlign = TextAlign.End, fontSize = 14.sp)
                Text(formatRupiah(item.price * item.quantity, false), Modifier.weight(1.5f), textAlign = TextAlign.End, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun PayButton(isEnabled: Boolean, isLoading: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = isEnabled,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(50.dp),
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
            Text(
                "Pay",
                color = MaterialTheme.colorScheme.background,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PaymentMethodSelection(
    methods: List<String>, // <-- Kembali menerima List<String>
    selectedMethod: String?,
    userBalance: Double?, // <-- Terima parameter saldo
    totalPrice: Double,   // <-- Terima parameter total harga
    onMethodSelected: (String) -> Unit,
    selectedColor: Color,
    selectedBorderColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        methods.forEach { methodName ->
            val isSelected = methodName == selectedMethod

            // Logika untuk menentukan status aktif/nonaktif
            var isEnabled = true // Secara default, semua metode aktif
            if (methodName == "Balance") {
                // Khusus untuk "Balance", cek apakah saldo mencukupi
                isEnabled = userBalance != null && userBalance >= totalPrice
            }

            val backgroundColor = when {
                isSelected -> selectedColor
                !isEnabled -> Color.LightGray.copy(alpha = 0.5f)
                else -> Color(0xFFF5F5F5)
            }
            val contentColor = when {
                isSelected -> selectedBorderColor
                !isEnabled -> Color.Gray
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
                    // Gunakan variabel isEnabled yang sudah kita tentukan
                    .clickable(enabled = isEnabled) { onMethodSelected(methodName) },
                contentAlignment = Alignment.Center
            ) {
                Text(methodName, fontWeight = FontWeight.SemiBold, color = contentColor)
            }
        }
    }
}

fun formatRupiah(amount: Double, withPrefix: Boolean = true): String {
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