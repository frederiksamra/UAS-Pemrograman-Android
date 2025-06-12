package com.android.laundrygo.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.laundrygo.R
// Import warna DarkBlue dari tema Anda
import com.android.laundrygo.ui.theme.DarkBlue
import com.android.laundrygo.ui.theme.LaundryGoTheme

data class ServiceTypeItem(val label: String, @DrawableRes val iconResId: Int)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceTypeScreen(
    onBack: () -> Unit,
    onServiceSelected: (String) -> Unit
) {
    val services = listOf(
        ServiceTypeItem("Pakaian Harian", R.drawable.shirt_and_pant),
        ServiceTypeItem("Perawatan Khusus", R.drawable.special_treatment),
        ServiceTypeItem("Boneka", R.drawable.doll),
        ServiceTypeItem("Tas", R.drawable.bag),
        ServiceTypeItem("Sepatu", R.drawable.shoes)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pilih Jenis Layanan") },
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
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(services) { service ->
                ServiceCard(
                    service = service,
                    onClick = { onServiceSelected(service.label) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ServiceCard(
    service: ServiceTypeItem,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.aspectRatio(1f),
        // 1. Warna container diubah menjadi DarkBlue
        colors = CardDefaults.cardColors(
            containerColor = DarkBlue,
        ),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = service.iconResId),
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                // 2. Warna ikon diubah menjadi putih agar kontras
                tint = Color.White
            )
            // Spacer dihilangkan dan diganti dengan verticalGap pada Column
            Text(
                text = service.label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                // 3. Warna teks diubah menjadi putih agar kontras
                color = Color.White
            )
        }
    }
}

@Preview(showBackground = true, name = "ServiceType Screen")
@Composable
fun ServiceTypeScreenPreview() {
    LaundryGoTheme {
        ServiceTypeScreen(onBack = {}, onServiceSelected = {})
    }
}