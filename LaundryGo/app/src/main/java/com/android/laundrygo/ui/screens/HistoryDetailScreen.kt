package com.android.laundrygo.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.laundrygo.R
import com.android.laundrygo.viewmodel.HistoryDetailViewModel
import com.android.laundrygo.viewmodel.ProductDetail
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryDetailScreen(
    viewModel: HistoryDetailViewModel = viewModel(),
    orderId: String,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.orderId,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF26326A)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Order Info
            DetailCard(
                "No. Order" to state.orderId,
                "Name" to state.name,
                "Phone Number" to state.phone,
                "Total payment" to state.totalPayment,
                "Payment method" to state.paymentMethod,
                "Payment status" to state.paymentStatus,
                "Address" to state.address
            )

            // Product List
            SectionTitle("Product Details")
            ProductHeaderRow()
            state.productList.forEach {
                ProductRow(it)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Payment Summary
            SectionTitle("Payment Summary")
            SummaryRow("Subtotal :", state.subtotal)
            SummaryRow("Discount :", state.discount)
            SummaryRow("Voucher :", state.voucher)
            SummaryRow("Total :", state.total)
        }
    }
}


@Composable
fun DetailCard(vararg rows: Pair<String, String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            rows.forEach { (label, value) ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = label)
                    Text(text = value, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun ProductHeaderRow() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Product", fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f))
        Text("Price", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Text("Qty", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Text("Subtotal", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
    }
}

@Composable
fun ProductRow(product: ProductDetail) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(product.name, modifier = Modifier.weight(2f))
        Text(product.price, modifier = Modifier.weight(1f))
        Text(product.qty, modifier = Modifier.weight(1f))
        Text(product.subtotal, modifier = Modifier.weight(1f))
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
fun SummaryRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label)
        Text(value, fontWeight = FontWeight.Bold)
    }
    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}
