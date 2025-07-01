package com.android.laundrygo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.laundrygo.model.Transaction
import com.android.laundrygo.ui.theme.DarkBlue
import com.android.laundrygo.viewmodel.HistoryDetailState
import com.android.laundrygo.viewmodel.HistoryDetailViewModel
import com.android.laundrygo.viewmodel.HistoryDetailViewModelFactory
import com.android.laundrygo.viewmodel.ProductDetail
import androidx.compose.ui.text.style.TextAlign
import com.android.laundrygo.util.formatRupiah

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryDetailScreen(
    orderId: String,
    onBack: () -> Unit,
    viewModel: HistoryDetailViewModel = viewModel(factory = HistoryDetailViewModelFactory(orderId = orderId))
) {
    val state by viewModel.state.collectAsState()
    val statusList = remember {
        listOf(
            "Menunggu Pembayaran",
            "Lunas",
            "Pick Up",
            "Washing",
            "Washed",
            "Delivery",
            "Completed"
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Detail Transaksi",
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (state.isLoading) {
                CircularProgressIndicator()
            } else if (state.error != null) {
                Text(
                    text = "Error: ${state.error}",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                // Order Info
                DetailCard(
                    "No. Order" to state.transaction?.id.orEmpty(),
                    "Name" to state.transaction?.customerName.orEmpty(),
                    "Phone Number" to state.transaction?.customerPhone.orEmpty(),
                    "Total payment" to state.totalPaymentFormatted,
                    "Payment method" to state.transaction?.paymentMethod.orEmpty(),
                    "Payment status" to state.transaction?.status?.let { statusIndex ->
                        statusList.getOrNull(statusIndex) ?: "Status Tidak Diketahui"
                    }.orEmpty(),
                    "Address" to state.transaction?.customerAddress.orEmpty()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Product List
                SectionTitle("Product Details")
                ProductHeaderRow()
                state.transaction?.items?.forEach {
                    ProductRow(
                        ProductDetail(
                            name = it.name,
                            price = formatRupiah(it.price),
                            qty = it.quantity.toString(),
                            subtotal = formatRupiah(it.price * it.quantity) // Diubah
                        )
                    )
                } ?: Text("No items in this transaction")


                Spacer(modifier = Modifier.height(16.dp))

                // Payment Summary
                SectionTitle("Payment Summary")
                SummaryRow("Subtotal :", state.subtotalFormatted)
                SummaryRow("Discount :", state.discountFormatted)
                SummaryRow("Total :", state.finalAmountFormatted)
            }
        }
    }
}

@Composable
fun DetailCard(vararg rows: Pair<String, String>) {
    val DarkBlue = Color(0xFF344970)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            rows.forEach { (label, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = label,
                        fontSize = 14.sp,
                        color = DarkBlue,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = value,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkBlue,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

@Composable
fun ProductHeaderRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Product", fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f))
        Text("Price", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Text("Qty", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
            Text("Subtotal", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ProductRow(product: ProductDetail) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(product.name, modifier = Modifier.weight(2f))
        Text(product.price, modifier = Modifier.weight(1f))
        Text(product.qty, modifier = Modifier.weight(1f))
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
            Text(
                text = product.subtotal,
                maxLines = 1
            )
        }
    }
}


@Composable
fun SummaryRow(label: String, value: String) {
    val DarkBlue = Color(0xFF344970)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = DarkBlue)
        Text(text = value, fontWeight = FontWeight.Bold, color = DarkBlue)
    }
    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
fun SectionTitle(text: String) {
    val DarkBlue = Color(0xFF344970)

    Text(
        text = text,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        color = DarkBlue,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}