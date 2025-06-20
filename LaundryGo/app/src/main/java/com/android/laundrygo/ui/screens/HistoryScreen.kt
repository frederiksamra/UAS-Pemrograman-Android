package com.android.laundrygo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.android.laundrygo.ui.theme.DarkBlue
import com.android.laundrygo.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    // 1. Terima ViewModel dari NavGraph, jangan buat di sini
    viewModel: HistoryViewModel,
    onBack: () -> Unit,
    onCheckClick: (String) -> Unit
) {
    // 2. Ambil seluruh UiState dari ViewModel
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Riwayat Transaksi", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBlue)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF0F2F5)), // Warna latar yang lebih lembut
            contentAlignment = Alignment.Center
        ) {
            // 3. Tampilkan UI berdasarkan kondisi dari UiState
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else if (uiState.error != null) {
                Text(
                    text = "Error: ${uiState.error}",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            } else if (uiState.transactions.isEmpty()) {
                Text("Belum ada riwayat transaksi.", modifier = Modifier.padding(16.dp))
            } else {
                // 4. Gunakan LazyColumn untuk performa yang lebih baik
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.transactions, key = { it.id }) { transaction ->
                        // 5. Kirim seluruh objek Transaction ke item
                        HistoryItem(
                            transaction = transaction,
                            onCheckClick = onCheckClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItem(
    transaction: Transaction, // Terima objek Transaction lengkap
    onCheckClick: (String) -> Unit
) {
    // 6. Logika untuk menentukan warna sekarang ada di dalam Composable (Lapisan UI)
    val statusColor = when (transaction.status) {
        "Lunas" -> Color(0xFF1F9E56) // Hijau
        "Menunggu Pembayaran" -> Color(0xFFF2994A) // Oranye
        "Dicuci", "Selesai Dicuci", "Dalam Pengantaran" -> DarkBlue
        else -> Color.Gray
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gunakan data langsung dari objek transaction
                Text(
                    text = "ID: ${transaction.id.take(8).uppercase()}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = DarkBlue
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
                // Gunakan fungsi helper untuk memformat tanggal
                text = transaction.createdAt?.toFormattedString() ?: "Tanggal tidak tersedia",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = { onCheckClick(transaction.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBlue)
                ) {
                    Text(text = "Check Detail")
                }
            }
        }
    }
}

// Fungsi helper untuk format tanggal, bisa dipindahkan ke file utilitas
private fun Date.toFormattedString(): String {
    val format = SimpleDateFormat("EEEE, d MMMM yyyy, HH:mm", Locale("id", "ID"))
    return format.format(this)
}