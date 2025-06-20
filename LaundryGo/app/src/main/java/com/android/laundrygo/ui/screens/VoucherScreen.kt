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
    voucherViewModel: VoucherViewModel // Terima ViewModel dari NavGraph
) {
    // PERBAIKAN: Gunakan satu UiState object
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
            // PERBAIKAN: Gunakan when dengan state dari uiState
            when {
                uiState.isLoading -> CircularProgressIndicator(color = lightNavy)
                uiState.error != null -> Text(text = uiState.error!!, color = RedError, textAlign = TextAlign.Center)
                uiState.vouchers.isEmpty() -> Text("Anda belum memiliki voucher.", color = DarkBlueText.copy(alpha = 0.7f))
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.vouchers, key = { it.documentId }) { voucher ->
                        NewVoucherCard(
                            voucher = voucher,
                            expiry = voucherViewModel.formatDate(voucher.valid_until),
                            // PERBAIKAN: Panggil fungsi onVoucherUsed
                            onUseClick = { voucherViewModel.onVoucherUsed(voucher.documentId) }
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = White) // Kartu tetap putih agar menonjol
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
                    text = voucher.discount_value.toString(),
                    color = White, // Teks diskon utama yang kuat
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = if (voucher.discount_type == "percent") "% OFF" else "K OFF",
                    color = White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Bagian Kanan (Detail dan Tombol Aksi)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Voucher Diskon",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = BlackText // Teks judul yang paling jelas
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = voucher.voucher_code,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = lightNavy // Kode voucher dengan warna aksen
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Berlaku hingga $expiry",
                    style = MaterialTheme.typography.bodySmall,
                    color = BlackText.copy(alpha = 0.6f) // Teks sekunder yang lebih lembut
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onUseClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkBlueText, // Tombol aksi utama
                        contentColor = White
                    )
                ) {
                    Text(text = "Use", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}


