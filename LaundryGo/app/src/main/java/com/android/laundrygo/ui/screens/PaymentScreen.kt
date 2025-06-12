package com.android.laundrygo.ui.screens.payment

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.laundrygo.data.model.OrderItem
import com.android.laundrygo.viewmodel.PaymentStatus
import com.android.laundrygo.viewmodel.PaymentUiState
import com.android.laundrygo.viewmodel.PaymentViewModel
import com.android.laundrygo.viewmodel.PaymentViewModelFactory
import java.text.NumberFormat
import java.util.*

// --- Composable Stateful (Entry Point untuk Navigasi) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    onBackClicked: () -> Unit,
    onPaymentSuccess: () -> Unit,
    paymentViewModel: PaymentViewModel = viewModel(factory = PaymentViewModelFactory())
) {
    val uiState by paymentViewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.paymentStatus) {
        if (uiState.paymentStatus == PaymentStatus.SUCCESS) {
            Toast.makeText(context, "Payment Successful!", Toast.LENGTH_SHORT).show()
            onPaymentSuccess()
        } else if (uiState.paymentStatus == PaymentStatus.ERROR) {
            Toast.makeText(context, "Payment Failed!", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invoice Payment", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                // ✅ PERBAIKAN: Tambahkan warna untuk ikon dan judul
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    // Warna untuk judul "Invoice Payment"
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    // Warna untuk ikon panah kembali
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
        PaymentScreenContent(
            uiState = uiState,
            onMethodSelected = { paymentViewModel.onPaymentMethodSelected(it) },
            paddingValues = paddingValues
        )
    }
}

// --- Composable Stateless (Hanya untuk Menampilkan UI) ---
@Composable
private fun PaymentScreenContent(
    uiState: PaymentUiState,
    onMethodSelected: (String) -> Unit,
    paddingValues: PaddingValues
) {
    val darkBlue = Color(0xFF3A4A7A)
    val lightGray = Color(0xFFF0F0F0)

    Column(
        modifier = Modifier
            .padding(paddingValues)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        TotalPriceCard(totalPrice = uiState.totalPrice, backgroundColor = darkBlue)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = uiState.transactionDate,
            color = Color.Gray,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        OrderDetails(
            items = uiState.orderItems,
            subtotal = uiState.subtotal,
            discount = uiState.discount,
            voucher = uiState.voucher,
            headerColor = lightGray
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Select Your Payment Method",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        PaymentMethodSelection(
            methods = uiState.availablePaymentMethods,
            selectedMethod = uiState.selectedPaymentMethod,
            onMethodSelected = onMethodSelected,
            selectedColor = darkBlue.copy(alpha = 0.1f),
            selectedBorderColor = darkBlue
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}


// --- Komponen-komponen UI Kecil ---

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
fun TotalPriceCard(totalPrice: Int, backgroundColor: Color) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth() // <-- Tambahkan ini
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Total Price",
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

@Composable
fun OrderDetails(items: List<OrderItem>, subtotal: Int, discount: Int, voucher: Int, headerColor: Color) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFFAFAFA))
            .padding(1.dp)
    ) {
        Row(
            modifier = Modifier
                .background(headerColor)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text("Product", Modifier.weight(2f), fontWeight = FontWeight.Bold)
            Text("Price", Modifier.weight(1f), textAlign = TextAlign.End, fontWeight = FontWeight.Bold)
            Text("Qty", Modifier.weight(1f), textAlign = TextAlign.End, fontWeight = FontWeight.Bold)
            Text("Subtotal", Modifier.weight(1f), textAlign = TextAlign.End, fontWeight = FontWeight.Bold)
        }
        items.forEach { item ->
            Row(
                modifier = Modifier
                    .background(Color.White)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(item.name, Modifier.weight(2f), fontSize = 14.sp)
                Text(formatRupiah(item.price, false), Modifier.weight(1f), textAlign = TextAlign.End, fontSize = 14.sp)
                Text(item.qty, Modifier.weight(1f), textAlign = TextAlign.End, fontSize = 14.sp)
                Text(formatRupiah(item.subtotal, false), Modifier.weight(1f), textAlign = TextAlign.End, fontSize = 14.sp)
            }
        }
        HorizontalDivider(color = headerColor)
        Column(modifier = Modifier
            .background(Color.White)
            .padding(12.dp)) {
            SummaryRow("Subtotal", formatRupiah(subtotal))
            Spacer(modifier = Modifier.height(4.dp))
            SummaryRow("Discount", formatRupiah(discount))
            Spacer(modifier = Modifier.height(4.dp))
            SummaryRow("Voucher", formatRupiah(voucher))
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray, fontSize = 14.sp)
        Text(value, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
}

@Composable
fun PaymentMethodSelection(
    methods: List<String>,
    selectedMethod: String?,
    onMethodSelected: (String) -> Unit,
    selectedColor: Color,
    selectedBorderColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        methods.forEach { method ->
            val isSelected = method == selectedMethod
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) selectedColor else Color(0xFFF5F5F5))
                    .border(
                        width = if (isSelected) 1.5.dp else 0.dp,
                        color = if (isSelected) selectedBorderColor else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onMethodSelected(method) },
                contentAlignment = Alignment.Center
            ) {
                Text(method, fontWeight = FontWeight.SemiBold, color = if(isSelected) selectedBorderColor else Color.Black)
            }
        }
    }
}

// --- Fungsi Helper ---
fun formatRupiah(amount: Int, withPrefix: Boolean = true): String {
    val localeID = Locale("in", "ID")
    val format = NumberFormat.getCurrencyInstance(localeID)
    format.maximumFractionDigits = 0
    val result = format.format(amount)
    return if (withPrefix) result else result.replace("Rp", "").trim()
}


// --- Preview ---
private fun createDummyUiState(paymentStatus: PaymentStatus = PaymentStatus.IDLE): PaymentUiState {
    val items = listOf(
        OrderItem("Fast Cleaning Shoes", 20000, "1", 20000),
        OrderItem("Iron only", 9000, "1,2Kg", 10000)
    )
    return PaymentUiState(
        orderItems = items,
        subtotal = 30000,
        totalPrice = 30000,
        transactionDate = "Kamis, 12 Juni 2025",
        availablePaymentMethods = listOf("Balance", "E-Wallet", "Bank Transfer"),
        selectedPaymentMethod = "E-Wallet",
        paymentStatus = paymentStatus
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Default State")
@Composable
fun PaymentScreenPreview() {
    MaterialTheme {
        val dummyState = createDummyUiState()
        Scaffold(
            topBar = { TopAppBar(title = { Text("Invoice Payment") }, navigationIcon = { Icon(Icons.AutoMirrored.Filled.ArrowBack, "") }) },
            bottomBar = { PayButton(isEnabled = true, isLoading = false, onClick = {}) }
        ) { paddingValues ->
            PaymentScreenContent(
                uiState = dummyState,
                onMethodSelected = {},
                paddingValues = paddingValues
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Loading State")
@Composable
fun PaymentScreenLoadingPreview() {
    MaterialTheme {
        val dummyState = createDummyUiState(paymentStatus = PaymentStatus.LOADING)
        Scaffold(
            topBar = { TopAppBar(title = { Text("Invoice Payment") }, navigationIcon = { Icon(Icons.AutoMirrored.Filled.ArrowBack, "") }) },
            bottomBar = { PayButton(isEnabled = false, isLoading = true, onClick = {}) }
        ) { paddingValues ->
            PaymentScreenContent(
                uiState = dummyState,
                onMethodSelected = {},
                paddingValues = paddingValues
            )
        }
    }
}