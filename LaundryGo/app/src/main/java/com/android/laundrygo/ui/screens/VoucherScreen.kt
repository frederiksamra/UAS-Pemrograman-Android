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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.laundrygo.ui.theme.LaundryGoTheme
import com.android.laundrygo.viewmodel.VoucherViewModel

// Definisi warna dan data class tidak berubah
val VOUCHER_PROMO_BG = Color(0xFF515886)
val VOUCHER_PROMO_TEXT = Color(0xFFE8E4D9)
val VOUCHER_CLAIM_BUTTON_BG = Color(0xFF6F65A8)
val SCREEN_BACKGROUND = Color(0xFFF8F7FC)

data class VoucherInfo(
    val id: Int,
    val title: String,
    val expiryDate: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoucherScreen(
    onBackClick: () -> Unit,
    // ================== PERUBAHAN DI SINI ==================
    // Nama parameter diubah dari 'viewModel' menjadi 'voucherViewModel' untuk menghindari konflik.
    voucherViewModel: VoucherViewModel = viewModel()
    // =======================================================
) {
    // Mengumpulkan (collect) state dari ViewModel
    // ================== PERUBAHAN DI SINI ==================
    // Gunakan nama parameter yang baru.
    val vouchers by voucherViewModel.vouchers.collectAsState()
    val isLoading by voucherViewModel.isLoading.collectAsState()
    val error by voucherViewModel.error.collectAsState()
    // =======================================================

    // State untuk Snackbar (notifikasi)
    val snackbarHostState = remember { SnackbarHostState() }

    // Mendengarkan event dari ViewModel untuk menampilkan Snackbar
    LaunchedEffect(Unit) {
        // ================== PERUBAHAN DI SINI ==================
        voucherViewModel.claimStatus.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
        // =======================================================
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Voucher", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = SCREEN_BACKGROUND
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else if (error != null) {
                Text(text = error!!, color = MaterialTheme.colorScheme.error)
            } else if (vouchers.isEmpty()) {
                Text(text = "Tidak ada voucher yang tersedia saat ini.")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(vouchers, key = { it.id }) { voucher ->
                        VoucherCard(
                            voucher = voucher,
                            // ================== PERUBAHAN DI SINI ==================
                            onClaimClick = { voucherViewModel.claimVoucher(voucher.id) }
                            // =======================================================
                        )
                    }
                }
            }
        }
    }
}


// Composable VoucherCard tidak perlu diubah
@Composable
fun VoucherCard(
    voucher: VoucherInfo,
    onClaimClick: () -> Unit
) {
    // ... isi kode VoucherCard sama seperti sebelumnya ...
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .background(VOUCHER_PROMO_BG)
                    .padding(horizontal = 20.dp, vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = voucher.title,
                    color = VOUCHER_PROMO_TEXT,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = voucher.expiryDate,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Button(
                    onClick = onClaimClick,
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VOUCHER_CLAIM_BUTTON_BG,
                        contentColor = Color.White
                    )
                ) {
                    Text(text = "Claim", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun VoucherScreenPreview() {
    LaundryGoTheme {
        VoucherScreen(onBackClick = {})
    }
}