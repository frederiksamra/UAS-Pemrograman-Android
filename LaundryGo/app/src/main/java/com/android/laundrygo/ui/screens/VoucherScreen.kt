package com.android.laundrygo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.laundrygo.ui.theme.LaundryGoTheme

// Warna kustom berdasarkan gambar yang Anda berikan
val VOUCHER_PROMO_BG = Color(0xFF515886)
val VOUCHER_PROMO_TEXT = Color(0xFFE8E4D9)
val VOUCHER_CLAIM_BUTTON_BG = Color(0xFF6F65A8)
val SCREEN_BACKGROUND = Color(0xFFF8F7FC) 

// Data class (tidak berubah)
data class VoucherInfo(
    val id: Int,
    val title: String,
    val expiryDate: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoucherScreen(
    onBackClick: () -> Unit
) {
    val vouchers = remember {
        listOf(
            VoucherInfo(1, "10% off entire order", "Monday, 9 March 2024"),
            VoucherInfo(2, "10% off entire order", "Monday, 9 March 2024"),
            VoucherInfo(3, "10% off entire order", "Monday, 9 March 2024")
        )
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
        // WARNA KUSTOM: Latar belakang utama layar
        containerColor = SCREEN_BACKGROUND
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(vouchers, key = { it.id }) { voucher ->
                VoucherCard(
                    voucher = voucher,
                    onClaimClick = { /* Handle claim logic */ }
                )
            }
        }
    }
}

@Composable
fun VoucherCard(
    voucher: VoucherInfo,
    onClaimClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            // Warna latar kartu, bisa juga diubah jika mau
            containerColor = Color.White
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Bagian atas untuk promosi
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    // WARNA KUSTOM: Latar belakang area promo
                    .background(VOUCHER_PROMO_BG)
                    .padding(horizontal = 20.dp, vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = voucher.title,
                    // WARNA KUSTOM: Teks di area promo
                    color = VOUCHER_PROMO_TEXT,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
            }

            // Bagian bawah untuk tanggal dan tombol
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
                    // Anda juga bisa mengubah warna ini jika perlu
                    color = Color.Gray
                )
                Button(
                    onClick = onClaimClick,
                    shape = MaterialTheme.shapes.small,
                    // WARNA KUSTOM: Warna tombol "Claim"
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VOUCHER_CLAIM_BUTTON_BG,
                        contentColor = Color.White // Teks tombol dibuat putih agar kontras
                    )
                ) {
                    Text(text = "Claim", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun VoucherScreenPreview() {
    LaundryGoTheme {
        VoucherScreen(onBackClick = {})
    }
}