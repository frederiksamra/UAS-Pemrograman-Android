package com.android.laundrygo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.laundrygo.model.Transaction
import com.android.laundrygo.repository.ServiceRepositoryImpl
import com.android.laundrygo.ui.theme.Cream
import com.android.laundrygo.ui.theme.DarkBlue
import com.android.laundrygo.util.formatRupiah
import com.android.laundrygo.viewmodel.OrderDetailState
import com.android.laundrygo.viewmodel.OrderManagementViewModel
import com.android.laundrygo.viewmodel.OrderManagementViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun OrderManagementScreen(viewModel: OrderManagementViewModel = viewModel(factory = OrderManagementViewModelFactory(ServiceRepositoryImpl()))) {
    val orders by viewModel.orders.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val feedbackMessage by viewModel.feedbackMessage.collectAsState()
    val statusOptions = viewModel.statusOptions
    val snackbarHostState = remember { SnackbarHostState() }

    var showDetailDialog by remember { mutableStateOf(false) }
    var orderIdToShow by remember { mutableStateOf<String?>(null) }
    val selectedOrderState by viewModel.selectedOrderState.collectAsState()

    LaunchedEffect(orderIdToShow) {
        orderIdToShow?.let {
            viewModel.loadOrderDetails(it)
            showDetailDialog = true
        }
    }

    LaunchedEffect(feedbackMessage) {
        feedbackMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearFeedbackMessage() }
    }

    if (showDetailDialog) {
        AdminOrderDetailDialog(
            detailState = selectedOrderState,
            onDismiss = {
                showDetailDialog = false
                orderIdToShow = null
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        content = { paddingValues ->
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues)
            ) {
                if (orders.isEmpty() && !isLoading) {
                    Text(
                        text = "Tidak ada pesanan aktif saat ini.",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(orders, key = { it.id }) { order ->
                            OrderItemCard(
                                order = order,
                                statusOptions = statusOptions,
                                onStatusChange = viewModel::updateOrderStatus,
                                onCheckDetailClick = { orderIdToShow = it }
                            )
                        }
                    }
                }
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    )
}

@Composable
private fun AdminOrderDetailDialog(
    detailState: OrderDetailState,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = Cream)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Rincian Pesanan", style = MaterialTheme.typography.headlineSmall, color = DarkBlue)
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        detailState.isLoading -> CircularProgressIndicator()
                        detailState.error != null -> Text("Error: ${detailState.error}")
                        detailState.order != null -> OrderDetailsDialogContent(detailState = detailState)
                        else -> Text("Memuat data...")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("Tutup")
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderDetailsDialogContent(detailState: OrderDetailState) {
    val order = detailState.order!!
    val fullStatusList = listOf("Menunggu Pembayaran", "Lunas", "Pick Up", "Washing", "Washed", "Delivery", "Completed")
    val currentStatusText = fullStatusList.getOrElse(order.status) { "Status Tidak Valid" }

    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Info Pesanan ---
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            DialogDetailRow(label = "No. Order", value = order.id.take(8).uppercase())
            // Gunakan customerName dan tambahkan alamat
            DialogDetailRow(label = "Nama Pemesan", value = order.customerName)
            DialogDetailRow(label = "Alamat", value = order.customerAddress)
            DialogDetailRow(label = "No. Telepon", value = order.customerPhone)
            DialogDetailRow(label = "Status", value = currentStatusText, isHighlight = true)
        }

        // --- Rincian Item ---
        if (order.items.isNotEmpty()) {
            Divider()
            DialogSectionTitle("Rincian Item")
            DialogProductHeaderRow()
            order.items.forEach { item ->
                DialogProductRow(
                    name = item.name,
                    price = formatRupiah(item.price),
                    qty = item.quantity.toString(),
                    subtotal = formatRupiah(item.price * item.quantity)
                )
            }
            Divider()
        }

        // --- Ringkasan Pembayaran ---
        DialogSectionTitle("Ringkasan Pembayaran")
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            DialogSummaryRow("Subtotal :", detailState.subtotalFormatted)
            DialogSummaryRow("Diskon :", detailState.discountFormatted)
            DialogSummaryRow("Total :", detailState.finalPriceFormatted, isTotal = true)
        }
    }
}

// --- Helper Composables untuk Dialog Detail ---

@Composable
private fun DialogDetailRow(label: String, value: String, isHighlight: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Text(text = "$label:", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold, color = DarkBlue.copy(alpha = 0.8f))
        Text(text = value, modifier = Modifier.weight(1.5f), fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Normal, color = DarkBlue, textAlign = TextAlign.End)
    }
}

@Composable
private fun DialogSectionTitle(text: String) {
    Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = DarkBlue, modifier = Modifier.padding(top = 8.dp))
}

@Composable
private fun DialogProductHeaderRow() {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Produk", fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f), color = DarkBlue)
        Text("Harga", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), color = DarkBlue)
        Text("Jml", fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.5f), color = DarkBlue, textAlign = TextAlign.Center)
        Box(Modifier.weight(1.2f), contentAlignment = Alignment.CenterEnd) {
            Text("Subtotal", fontWeight = FontWeight.Bold, color = DarkBlue)
        }
    }
}

@Composable
private fun DialogProductRow(name: String, price: String, qty: String, subtotal: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(name, modifier = Modifier.weight(2f), fontSize = 14.sp, color = DarkBlue)
        Text(price, modifier = Modifier.weight(1f), fontSize = 14.sp, color = DarkBlue)
        Text(qty, modifier = Modifier.weight(0.5f), fontSize = 14.sp, color = DarkBlue, textAlign = TextAlign.Center)
        Box(Modifier.weight(1.2f), contentAlignment = Alignment.CenterEnd) {
            Text(subtotal, maxLines = 1, fontSize = 14.sp, color = DarkBlue)
        }
    }
}

@Composable
private fun DialogSummaryRow(label: String, value: String, isTotal: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, color = DarkBlue.copy(alpha = 0.8f))
        Text(text = value, fontWeight = if(isTotal) FontWeight.Bold else FontWeight.Normal, color = DarkBlue)
    }
}


// --- Card untuk Daftar Pesanan (Tidak Berubah) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrderItemCard(
    order: Transaction,
    statusOptions: List<String>,
    onStatusChange: (transactionId: String, newStatusIndex: Int) -> Unit,
    onCheckDetailClick: (orderId: String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val fullStatusList = listOf("Menunggu Pembayaran", "Lunas", "Pick Up", "Washing", "Washed", "Delivery", "Completed")
    val currentStatusText = fullStatusList.getOrElse(order.status - 1) { "Status Tidak Valid" }
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = DarkBlue, unfocusedTextColor = DarkBlue, focusedBorderColor = DarkBlue,
        unfocusedBorderColor = DarkBlue.copy(alpha = 0.5f), focusedLabelColor = DarkBlue,
        unfocusedLabelColor = DarkBlue.copy(alpha = 0.7f), focusedTrailingIconColor = DarkBlue,
        unfocusedTrailingIconColor = DarkBlue.copy(alpha = 0.7f), cursorColor = DarkBlue,
        focusedPlaceholderColor = DarkBlue.copy(alpha = 0.5f), unfocusedPlaceholderColor = DarkBlue.copy(alpha = 0.5f)
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Cream)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val orderDate = order.createdAt?.let { SimpleDateFormat("EEEE, d MMMM yyyy, HH:mm", Locale("id", "ID")).format(it) } ?: "Tanggal tidak tersedia"
            Text(text = "Order ID: ${order.id.take(8).uppercase()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DarkBlue)
            Text(text = "Pemesan: ${order.customerName}", style = MaterialTheme.typography.bodySmall, color = DarkBlue.copy(alpha = 0.8f))
            Text(text = "Tanggal: $orderDate", style = MaterialTheme.typography.bodySmall, color = DarkBlue.copy(alpha = 0.8f))
            Divider(modifier = Modifier.padding(vertical = 4.dp))
            Text(text = "Total: ${formatRupiah(order.totalPrice)}", style = MaterialTheme.typography.bodyMedium, color = DarkBlue)
            Text(text = "Status Saat Ini: $currentStatusText", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = DarkBlue)
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth().menuAnchor(), value = "", onValueChange = {}, readOnly = true,
                    placeholder = { Text("Update Status Pesanan...") }, label = { Text("Pilih Status Berikutnya") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, colors = textFieldColors
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(Cream)) {
                    statusOptions.forEachIndexed { index, status ->
                        val newStatusValue = index + 3
                        val isPastOrCurrentStatus = newStatusValue <= order.status
                        DropdownMenuItem(
                            text = { Text(text = status, color = if (isPastOrCurrentStatus) DarkBlue.copy(alpha = 0.5f) else DarkBlue) },
                            onClick = { onStatusChange(order.id, index); expanded = false },
                            enabled = !isPastOrCurrentStatus
                        )
                    }
                }
            }
            Button(
                onClick = { onCheckDetailClick(order.id) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = DarkBlue, contentColor = Cream)
            ) {
                Text("Lihat Detail Pesanan")
            }
        }
    }
}
