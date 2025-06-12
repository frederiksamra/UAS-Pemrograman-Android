package com.android.laundrygo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.laundrygo.ui.theme.Cream
import com.android.laundrygo.ui.theme.DarkBlue
import com.android.laundrygo.ui.theme.LaundryGoTheme

data class BagService(
    val title: String,
    val price: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BagScreen(
    onBack: () -> Unit,
    onAddClick: (String) -> Unit,
    onCartClick: () -> Unit
) {
    val services = listOf(
        BagService("Deep Cleaning Bag", "IDR 70.000/Pcs"),
        BagService("Premium Cleaning Bag", "IDR 90.000/Pcs"),
        BagService("Fast Cleaning Bag", "IDR 40.000/Pcs")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tas") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onCartClick) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Keranjang"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 5. Menggunakan itemsIndexed untuk membuat warna selang-seling
            itemsIndexed(services) { index, service ->
                val cardColor = if (index % 2 == 0) DarkBlue else Cream
                BagServiceCard(
                    service = service,
                    containerColor = cardColor,
                    onAddClick = { onAddClick(service.title) }
                )
            }
        }
    }
}

@Composable
private fun BagServiceCard(
    service: BagService,
    containerColor: Color,
    onAddClick: () -> Unit
) {
    // Logika pewarnaan dinamis yang sama seperti di ShirtPantsScreen
    val isCreamCard = containerColor == Cream
    val textColor = if (isCreamCard) MaterialTheme.colorScheme.primary else Color.White
    val priceColor = if (isCreamCard) MaterialTheme.colorScheme.primary else Color.White

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = service.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    text = service.price,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = priceColor
                )
            }
            Spacer(modifier = Modifier.width(16.dp))

            val buttonColors = if(isCreamCard) {
                ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            } else {
                ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            }

            FilledTonalButton(
                onClick = onAddClick,
                colors = buttonColors,
                contentPadding = PaddingValues(12.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tambah ${service.title}"
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "BagScreen")
@Composable
fun BagScreenPreview() {
    LaundryGoTheme {
        BagScreen({}, {}, {})
    }
}