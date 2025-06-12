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

data class DollService(
    val title: String,
    val price: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DollScreen(
    onBack: () -> Unit,
    onAddClick: (String) -> Unit,
    onCartClick: () -> Unit
) {
    val services = listOf(
        DollService("Boneka 20 - 40cm", "IDR 43.000/Pcs"),
        DollService("Boneka 41 - 60cm", "IDR 45.000/Pcs"),
        DollService("Boneka 61 - 100cm", "IDR 70.000/Pcs"),
        DollService("Boneka > 100cm", "IDR 90.000/Pcs"),
        DollService("Boneka Ukuran Jumbo", "IDR 150.000/Pcs")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Boneka") },
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
            itemsIndexed(services) { index, service ->
                val cardColor = if (index % 2 == 0) DarkBlue else Cream
                DollServiceCard(
                    service = service,
                    containerColor = cardColor,
                    onAddClick = { onAddClick(service.title) }
                )
            }
        }
    }
}

@Composable
private fun DollServiceCard(
    service: DollService,
    containerColor: Color,
    onAddClick: () -> Unit
) {
    // Logika pewarnaan dinamis yang sama seperti sebelumnya
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

@Preview(showBackground = true, name = "DollScreen")
@Composable
fun DollScreenPreview() {
    LaundryGoTheme {
        DollScreen({}, {}, {})
    }
}