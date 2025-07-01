package com.android.laundrygo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.laundrygo.model.Voucher
import com.android.laundrygo.ui.theme.*
import com.android.laundrygo.viewmodel.VoucherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoucherScreen(
    onBackClick: () -> Unit,
    onVoucherSelected: (Voucher) -> Unit, // Callback untuk mengirim voucher
    voucherViewModel: VoucherViewModel
) {
    val uiState by voucherViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Voucher Saya", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = DarkBlueText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = White,
                    titleContentColor = DarkBlueText
                )
            )
        },
        containerColor = White
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> CircularProgressIndicator(color = lightNavy)
                uiState.error != null -> Text(text = uiState.error!!, color = RedError, textAlign = TextAlign.Center)
                uiState.vouchers.isEmpty() -> Text("Anda belum memiliki voucher.", color = DarkBlueText.copy(alpha = 0.7f))
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.vouchers, key = { it.voucherDocumentId }) { voucher ->
                        NewVoucherCard(
                            voucher = voucher,
                            expiry = voucherViewModel.formatDate(voucher.valid_until),
                            // Saat tombol diklik, panggil callback untuk mengirim data voucher & kembali
                            onUseClick = { onVoucherSelected(voucher) }
                        )
                    }
                }
            }
        }
    }
}

// --- DESAIN KARTU VOUCHER BARU DENGAN PALET ANDA ---
@Composable
fun NewVoucherCard(
    voucher: Voucher,
    expiry: String,
    onUseClick: () -> Unit
) {
    // Siapkan teks diskon berdasarkan tipenya
    val (discountText, unitText) = remember(voucher) {
        when (voucher.discount_type) {
            "percent" -> {
                // Untuk persen, tampilkan angka bulatnya
                val value = voucher.discount_value.toInt().toString()
                val unit = "% OFF"
                value to unit
            }
            "fixed" -> {
                // Untuk fixed (dalam ribuan), bagi dengan 1000
                val value = (voucher.discount_value / 1000).toInt().toString()
                val unit = "RB OFF" // RB = Ribu, lebih umum di Indonesia
                value to unit
            }
            else -> {
                // Fallback jika ada tipe lain
                val value = voucher.discount_value.toInt().toString()
                val unit = "OFF"
                value to unit
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = White)
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bagian Kiri (Informasi Diskon Utama)
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .background(DarkBlue)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .width(100.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = discountText, // Teks diskon yang sudah diformat
                    color = White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = unitText, // Teks unit yang sudah diformat
                    color = White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Voucher Diskon",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = BlackText
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = voucher.voucher_code,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = lightNavy
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Berlaku hingga $expiry",
                    style = MaterialTheme.typography.bodySmall,
                    color = BlackText.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onUseClick,
                    enabled = !voucher.is_used,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkBlueText,
                        contentColor = White
                    )
                ) {
                    Text(text = if (voucher.is_used) "Telah Digunakan" else "Gunakan")
                }
            }
        }
    }
}


