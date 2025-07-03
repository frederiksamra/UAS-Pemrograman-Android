package com.android.laundrygo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.laundrygo.model.Transaction
import com.android.laundrygo.viewmodel.InProcessViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InProcessScreen(
    viewModel: InProcessViewModel,
    onBackClick: () -> Unit
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val transactions by viewModel.inProcessTransactions.collectAsState()
    val selectedTransaction by viewModel.selectedTransaction.collectAsState()

    val statusDescriptions = mapOf(
        2 to "Pembayaran berhasil, pesanan akan segera dijemput oleh kurir.",
        3 to "Pesanan sedang dijemput oleh kurir.",
        4 to "Pesanan sedang dicuci oleh tim laundry.",
        5 to "Pencucian selesai, siap untuk dikirim.",
        6 to "Pesanan sedang diantarkan ke lokasi Anda."
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lacak Pesanan") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    scrolledContainerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        },
        containerColor = Color.White
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (errorMessage != null) {
                Text(text = errorMessage!!, color = Color.Red)
            } else {
                selectedTransaction?.let { transaction ->
                    Text(
                        text = statusDescriptions[transaction.status]
                            ?: "Status tidak diketahui.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        textAlign = TextAlign.Center
                    )

                    StepperHorizontal(currentStep = transaction.status)
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    "Daftar Pesanan Anda",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(transactions, key = { it.id }) { transaction ->
                        OrderChip(
                            transaction = transaction,
                            isSelected = selectedTransaction?.id == transaction.id,
                            onClick = { viewModel.selectTransaction(transaction) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OrderChip(
    transaction: Transaction,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        color = backgroundColor,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .width(140.dp)
            .clickable { onClick() }
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("ID: ${transaction.id.take(6)}", color = contentColor, fontSize = 12.sp)
            Text(transaction.createdAt?.toShortFormattedString() ?: "-", color = contentColor, fontSize = 10.sp)
        }
    }
}

@Composable
fun StepperHorizontal(currentStep: Int) {
    val steps = listOf("Lunas", "Pick Up", "Washing", "Washed", "Delivery")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        steps.forEachIndexed { index, label ->
            val stepNumber = index + 2
            val isCompleted = currentStep > stepNumber
            val isCurrent = currentStep == stepNumber
            val color = when {
                isCompleted -> MaterialTheme.colorScheme.primary
                isCurrent -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                else -> Color.LightGray
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(color, shape = MaterialTheme.shapes.small),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(label, fontSize = 12.sp, color = Color.Black)
            }
            if (index != steps.lastIndex) {
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}

fun Date.toShortFormattedString(): String {
    val format = SimpleDateFormat("d MMM yyyy, HH:mm", Locale("id", "ID"))
    return format.format(this)
}
