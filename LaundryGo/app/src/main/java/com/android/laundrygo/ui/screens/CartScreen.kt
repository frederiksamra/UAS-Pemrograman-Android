package com.android.laundrygo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.ShoppingCart
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
import com.android.laundrygo.model.CartItem
import com.android.laundrygo.ui.theme.Cream
import com.android.laundrygo.ui.theme.DarkBlue
import com.android.laundrygo.viewmodel.CartViewModel
import java.text.NumberFormat
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    viewModel: CartViewModel,
    onBack: () -> Unit,
    onCheckoutClick: (Double) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val totalPrice = uiState.cartItems.sumOf { it.price * it.quantity }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Keranjang Saya") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        },
        bottomBar = {
            if (uiState.cartItems.isNotEmpty() && !uiState.isLoading) {
                CheckoutBottomBar(totalPrice = totalPrice) { onCheckoutClick(totalPrice) }
            }
        },
        containerColor = Color.White
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.error != null) {
                Text(
                    text = "Error: ${uiState.error}",
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                    textAlign = TextAlign.Center
                )
            } else if (uiState.cartItems.isEmpty()) {
                EmptyCartView(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(uiState.cartItems, key = { _, item -> item.id }) { index, item ->
                        val cardColor = if (index % 2 == 0) DarkBlue else Cream
                        CartListItem(
                            item = item,
                            containerColor = cardColor,
                            onRemoveClick = { viewModel.removeItem(item.id) },
                            onQuantityChange = { change -> viewModel.updateQuantity(item.id, change) }
                        )
                    }
                }
            }
        }
    }
}

// Tidak ada perubahan di CartListItem, sudah benar
@Composable
private fun CartListItem(
    item: CartItem,
    containerColor: Color,
    onRemoveClick: () -> Unit,
    onQuantityChange: (Int) -> Unit
) {
    val isCreamCard = containerColor == Cream
    val contentColor = if (isCreamCard) DarkBlue else Color.White

    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        ListItem(
            headlineContent = {
                // Baris Judul dengan Kategori
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(item.name, fontWeight = FontWeight.SemiBold, color = contentColor)
                    Spacer(Modifier.width(8.dp))
                    // Menampilkan kategori dengan style yang berbeda
                    Card(
                        shape = RoundedCornerShape(4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = contentColor.copy(alpha = 0.15f),
                            contentColor = contentColor
                        )
                    ) {
                        Text(
                            text = item.category,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            },
            supportingContent = {
                // Menampilkan deskripsi dan harga/unit
                Column {
                    Text(
                        // Menggabungkan harga dengan unit
                        text = "${formatCurrency(item.price)} ${item.unit}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                }
            },
            trailingContent = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    QuantitySelector(
                        quantity = item.quantity,
                        contentColor = contentColor,
                        onQuantityChange = onQuantityChange
                    )
                    IconButton(onClick = onRemoveClick) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus Item", tint = MaterialTheme.colorScheme.error)
                    }
                }
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )
    }
}

@Composable
private fun QuantitySelector(
    quantity: Int,
    contentColor: Color,
    onQuantityChange: (Int) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { onQuantityChange(-1) }, modifier = Modifier.size(28.dp), enabled = quantity > 1) {
            Icon(Icons.Default.Remove, "Kurangi Kuantitas", modifier = Modifier.size(20.dp), tint = contentColor)
        }
        Text(
            text = quantity.toString(),
            modifier = Modifier.width(28.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
        IconButton(onClick = { onQuantityChange(1) }, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Add, "Tambah Kuantitas", modifier = Modifier.size(20.dp), tint = contentColor)
        }
    }
}

@Composable
private fun EmptyCartView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.ShoppingCart,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Color.Gray.copy(alpha = 0.5f)
            )
            Text(
                text = "Keranjang Anda Kosong",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Ayo mulai tambahkan layanan laundry!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CheckoutBottomBar(totalPrice: Double, onCheckoutClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Total Harga:", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = formatCurrency(totalPrice),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            // PERBAIKAN DI SINI: Mengembalikan kustomisasi tombol Checkout
            Button(
                onClick = onCheckoutClick,
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Cream,
                    contentColor = DarkBlue // Warna ikon/teks di dalam tombol
                )
            ) {
                Text("Checkout", fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun formatCurrency(price: Double): String {
    val localeID = Locale("in", "ID")
    val formatRupiah = NumberFormat.getCurrencyInstance(localeID)
    formatRupiah.maximumFractionDigits = 0
    return formatRupiah.format(price)
}
