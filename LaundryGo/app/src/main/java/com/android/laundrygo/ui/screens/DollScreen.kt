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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.laundrygo.model.LaundryService
import com.android.laundrygo.repository.ServiceRepositoryImpl
import com.android.laundrygo.ui.theme.Cream
import com.android.laundrygo.ui.theme.DarkBlue
import com.android.laundrygo.viewmodel.DollViewModel
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DollScreen(
    onBack: () -> Unit,
    onAddClick: (LaundryService) -> Unit,
    onCartClick: () -> Unit,
    viewModel: DollViewModel = viewModel(
        factory = DollViewModel.provideFactory(ServiceRepositoryImpl())
    )
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Boneka") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = onCartClick) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Keranjang")
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.error != null -> {
                    Text(
                        text = "Gagal memuat data: ${uiState.error}",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(uiState.services) { index, service ->
                            val cardColor = if (index % 2 == 0) DarkBlue else Cream
                            DollServiceCard(
                                service = service,
                                containerColor = cardColor,
                                onAddClick = { onAddClick(service) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DollServiceCard(
    service: LaundryService,
    containerColor: Color,
    onAddClick: () -> Unit
) {
    val isCreamCard = containerColor == Cream
    val titleColor = if (isCreamCard) DarkBlue else Color.White
    val descriptionColor = if (isCreamCard) DarkBlue.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.8f)
    val priceColor = if (isCreamCard) DarkBlue else Color.White
    val formattedPrice = formatRupiah(service.price)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(service.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = titleColor)
                Text(service.description, style = MaterialTheme.typography.bodyMedium, color = descriptionColor)
                Text("${formattedPrice}${service.unit}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = priceColor)
            }
            Spacer(modifier = Modifier.width(16.dp))
            val buttonColors = if (isCreamCard) {
                ButtonDefaults.filledTonalButtonColors(containerColor = DarkBlue, contentColor = Color.White)
            } else {
                ButtonDefaults.filledTonalButtonColors(containerColor = Color.White, contentColor = DarkBlue)
            }
            FilledTonalButton(
                onClick = onAddClick,
                colors = buttonColors,
                contentPadding = PaddingValues(12.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah ${service.title}")
            }
        }
    }
}

private fun formatPrice(price: Long): String {
    val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
    format.maximumFractionDigits = 0
    return format.format(price)
}