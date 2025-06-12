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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.laundrygo.ui.theme.Cream
import com.android.laundrygo.ui.theme.DarkBlue
import com.android.laundrygo.ui.theme.LaundryGoTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.NumberFormat
import java.util.*

// --- Data Class untuk Item Keranjang ---
data class CartItem(
    val id: Int,
    val name: String,
    val description: String,
    val price: Double,
    var quantity: Int
)

class CartViewModel : ViewModel() {

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    val totalPrice: Double
        get() = _cartItems.value.sumOf { it.price * it.quantity }

    init {
        // Data dummy untuk contoh
        loadDummyItems()
    }

    private fun loadDummyItems() {
        _cartItems.value = listOf(
            CartItem(1, "Deep Cleaning", "Cuci Kering + Setrika", 12000.0, 2),
            CartItem(2, "Special Long Dress", "Perawatan khusus gaun", 27000.0, 1),
            CartItem(3, "King Package", "Cuci ekspres, setrika, antar jemput", 35000.0, 3)
        )
    }

    fun removeItem(itemId: Int) {
        _cartItems.update { currentList ->
            currentList.filterNot { it.id == itemId }
        }
    }

    fun updateQuantity(itemId: Int, change: Int) {
        _cartItems.update { currentList ->
            currentList.map { item ->
                if (item.id == itemId) {
                    val newQuantity = (item.quantity + change).coerceAtLeast(0)
                    if (newQuantity == 0) null else item.copy(quantity = newQuantity)
                } else {
                    item
                }
            }.filterNotNull()
        }
    }

    fun checkout() {
        _cartItems.value = emptyList()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    viewModel: CartViewModel = viewModel(),
    onBack: () -> Unit,
    onCheckoutClick: () -> Unit
) {
    val cartItems by viewModel.cartItems.collectAsState()
    val totalPrice = viewModel.totalPrice

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
            if (cartItems.isNotEmpty()) {
                CheckoutBottomBar(totalPrice = totalPrice) {
                    onCheckoutClick()
                }
            }
        },
        containerColor = Color.White
    ) { paddingValues ->
        if (cartItems.isEmpty()) {
            EmptyCartView(modifier = Modifier.padding(paddingValues))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(cartItems, key = { _, item -> item.id }) { index, item ->
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

@Composable
private fun CartListItem(
    item: CartItem,
    containerColor: Color,
    onRemoveClick: () -> Unit,
    onQuantityChange: (Int) -> Unit
) {
    val isCreamCard = containerColor == Cream
    val contentColor = if (isCreamCard) MaterialTheme.colorScheme.primary else Color.White

    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        ListItem(
            headlineContent = { Text(item.name, fontWeight = FontWeight.SemiBold, color = contentColor) },
            supportingContent = { Text(formatCurrency(item.price), color = contentColor.copy(alpha = 0.8f)) },
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
        IconButton(onClick = { onQuantityChange(-1) }, modifier = Modifier.size(28.dp)) {
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
                tint = MaterialTheme.colorScheme.surfaceVariant
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
                Text("Total Harga:", style = MaterialTheme.typography.labelLarge)
                Text(
                    text = formatCurrency(totalPrice),
                    style = MaterialTheme.typography.headlineSmall.copy(fontSize = 20.sp),
                    fontWeight = FontWeight.ExtraBold,
                    // Warna diperbaiki agar kontras di latar terang
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Button(
                onClick = onCheckoutClick,
                modifier = Modifier
                    .padding(start = 12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Cream,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Checkout", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// --- Fungsi utilitas untuk format mata uang ---
private fun formatCurrency(price: Double): String {
    val localeID = Locale("in", "ID")
    val formatRupiah = NumberFormat.getCurrencyInstance(localeID)
    formatRupiah.maximumFractionDigits = 0
    return formatRupiah.format(price)
}

// --- Preview ---
@Preview(showBackground = true, name = "Cart with Items")
@Composable
fun CartScreenPreview() {
    LaundryGoTheme {
        CartScreen(onBack = {}, onCheckoutClick = {})
    }
}