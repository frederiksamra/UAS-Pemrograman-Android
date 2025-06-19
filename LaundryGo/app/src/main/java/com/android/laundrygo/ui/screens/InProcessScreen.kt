package com.android.laundrygo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.laundrygo.R
import com.android.laundrygo.ui.theme.AppTypography
import com.android.laundrygo.viewmodel.InProcessViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InProcessScreen(
    // Terima ViewModel dari NavGraph
    viewModel: InProcessViewModel,
    onBackClick: () -> Unit
) {
    val Grey = Color(0xFFE1E1E1)
    val NavGrey = Color(0xFFB0B0B0)
    val TextBlue = Color(0xFF435585)

    // Ambil semua state dari ViewModel
    val selectedIndex by viewModel.selectedIndex.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val activeTransaction by viewModel.activeTransaction.collectAsState()

    val navItems = listOf("Pick Up", "Washing", "Washed", "Delivery")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lacak Pesanan", fontSize = 20.sp, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, "Kembali", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF26326A))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Grey)
                .padding(padding)
                .padding(16.dp)
        ) {
            // Tampilkan Navigasi Tab
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().background(NavGrey).padding(8.dp)
            ) {
                navItems.forEachIndexed { index, item ->
                    val isSelected = index == selectedIndex
                    Box(modifier = Modifier
                        .weight(1f)
                        .selectable(
                            selected = isSelected,
                            onClick = { /* Tab tidak bisa diubah manual oleh user */ }
                        )
                        .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = item,
                            modifier = Modifier.align(Alignment.Center),
                            color = if (isSelected) Grey else TextBlue,
                            fontWeight = FontWeight.Bold,
                            style = AppTypography.titleLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Tampilkan Konten berdasarkan state
            Box(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator()
                } else if (errorMessage != null) {
                    Text(text = errorMessage!!, color = Color.Red, textAlign = TextAlign.Center)
                } else if (activeTransaction == null) {
                    Text("Tidak ada pesanan yang sedang diproses saat ini.", textAlign = TextAlign.Center)
                } else {
                    // Tampilkan konten sesuai tab yang aktif
                    ProcessContentSwitch(selectedIndex = selectedIndex, textColor = TextBlue)
                }
            }
        }
    }
}

// Composable baru untuk mengatur konten mana yang tampil
@Composable
private fun ProcessContentSwitch(selectedIndex: Int, textColor: Color) {
    when (selectedIndex) {
        0 -> ProcessContent(
            iconRes = R.drawable.on_the_way,
            text = "Kurir kami sedang dalam perjalanan untuk mengambil laundry Anda! Mohon ditunggu.",
            textColor = textColor
        )
        1 -> ProcessContent(
            iconRes = R.drawable.in_process,
            text = "Laundry Anda sedang kami cuci dengan bersih. Kami pastikan hasilnya memuaskan!",
            textColor = textColor
        )
        2 -> ProcessContent(
            iconRes = R.drawable.process_done,
            text = "Pesanan Anda telah selesai dicuci dan siap untuk diantar kembali.",
            textColor = textColor
        )
        3 -> ProcessContent(
            iconRes = R.drawable.delivery,
            text = "Kabar baik! Pakaian bersih Anda sedang dalam perjalanan menuju lokasi Anda.",
            textColor = textColor
        )
    }
}

@Composable
fun ProcessContent(iconRes: Int, text: String, textColor: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier
                .fillMaxWidth(0.5f) // Ikut skala device, setengah lebar layar
                .aspectRatio(1f)
                .padding(16.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = text,
            color = textColor,
            style = AppTypography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}
