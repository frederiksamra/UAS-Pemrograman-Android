package com.android.laundrygo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.laundrygo.model.Transaction
import com.android.laundrygo.ui.theme.Cream
import com.android.laundrygo.ui.theme.DarkBlue
import com.android.laundrygo.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onBack: () -> Unit,
    onCheckClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Riwayat Transaksi") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        },
        containerColor = Color.White
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else if (uiState.error != null) {
                Text(text = "Error: ${uiState.error}", textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp))
            } else if (uiState.transactions.isEmpty()) {
                Text("Belum ada riwayat transaksi.", modifier = Modifier.padding(16.dp))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(uiState.transactions, key = { _, item -> item.id }) { index, transaction ->
                        HistoryItem(
                            transaction = transaction,
                            onCheckClick = onCheckClick,
                            onDeleteClick = { transactionId -> // Handle delete click
                                viewModel.deleteTransaction(transactionId)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItem(
    transaction: Transaction,
    onCheckClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit // Add this callback
) {
    val (cardColor, textColor, buttonContainerColor) = when (transaction.status) {
        "Lunas" -> Triple(Cream, DarkBlue, DarkBlue)
        "Menunggu Pembayaran" -> Triple(DarkBlue, Cream, Cream)
        "Dicuci", "Selesai Dicuci", "Dalam Pengantaran" -> Triple(Color.White, DarkBlue, DarkBlue)
        else -> Triple(Color.White, Color.Gray, DarkBlue)
    }

    val statusColor = when (transaction.status) {
        "Lunas" -> Color(0xFF1F9E56)
        "Menunggu Pembayaran" -> Color(0xFFF2994A)
        "Dicuci", "Selesai Dicuci", "Dalam Pengantaran" -> DarkBlue
        else -> Color.Gray
    }

    val buttonTextColor = when (transaction.status) {
        "Lunas" -> Cream
        "Menunggu Pembayaran" -> DarkBlue
        else -> Color.White
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ID: ${transaction.id.take(8).uppercase()}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = textColor
                )
                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = transaction.status,
                        color = statusColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = transaction.createdAt?.toFormattedString() ?: "Tanggal tidak tersedia",
                fontSize = 14.sp,
                color = textColor
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween, // Changed to SpaceBetween
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onDeleteClick(transaction.id) }) { // Delete button on the left
                    Icon(Icons.Default.Delete, contentDescription = "Hapus Riwayat", tint = MaterialTheme.colorScheme.error)
                }
                Row( // Row for the Check Detail button to align it to the end
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.weight(1f, fill = false) // Use weight to push to the end
                ) {
                    Button(
                        onClick = { onCheckClick(transaction.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = buttonContainerColor, contentColor = buttonTextColor)
                    ) {
                        Text(text = "Check Detail")
                    }
                }
            }
        }
    }
}

private fun Date.toFormattedString(): String {
    val format = SimpleDateFormat("EEEE, d MMMM yyyy, HH:mm", Locale("id", "ID"))
    return format.format(this)
}