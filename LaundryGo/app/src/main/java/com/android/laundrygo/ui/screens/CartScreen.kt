package com.android.laundrygo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.laundrygo.ui.theme.Cream
import com.android.laundrygo.ui.theme.LaundryGoTheme
import com.android.laundrygo.ui.theme.White
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

// --- ViewModel untuk Mengelola State CartScreen ---
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
            CartItem(2, "special Long Dress", "", 27000.0, 1),
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
        // Logika untuk proses checkout
        // Misalnya, membersihkan keranjang
        _cartItems.value = emptyList()
    }
}


// --- Composable Utama untuk CartScreen ---
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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                CheckoutBottomBar(totalPrice = totalPrice) {
                    viewModel.checkout()
                    onCheckoutClick()
                }
            }
        }
    ) { paddingValues ->
        if (cartItems.isEmpty()) {
            EmptyCartView(modifier = Modifier.padding(paddingValues))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cartItems, key = { it.id }) { item ->
                    CartItemCard(
                        item = item,
                        onRemoveClick = { viewModel.removeItem(item.id) },
                        onQuantityChange = { change -> viewModel.updateQuantity(item.id, change) }
                    )
                }
            }
        }
    }
}

// --- Composable untuk Menampilkan Satu Item di Keranjang ---
@Composable
private fun CartItemCard(
    item: CartItem,
    onRemoveClick: () -> Unit,
    onQuantityChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Cream)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = item.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = formatCurrency(item.price), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onRemoveClick, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Hapus Item", tint = MaterialTheme.colorScheme.error)
                }
                Spacer(modifier = Modifier.height(8.dp))
                QuantitySelector(
                    quantity = item.quantity,
                    onQuantityChange = onQuantityChange
                )
            }
        }
    }
}

// --- Composable untuk Pilihan Kuantitas ---
@Composable
private fun QuantitySelector(quantity: Int, onQuantityChange: (Int) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 4.dp)
    ) {
        IconButton(onClick = { onQuantityChange(-1) }, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Remove, contentDescription = "Kurangi Kuantitas")
        }
        Text(
            text = quantity.toString(),
            modifier = Modifier.width(30.dp),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = { onQuantityChange(1) }, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Add, contentDescription = "Tambah Kuantitas")
        }
    }
}

// --- Composable untuk Tampilan Keranjang Kosong ---
@Composable
private fun EmptyCartView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Keranjang Anda Kosong",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Ayo mulai tambahkan layanan laundry!",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// --- Composable untuk Bottom Bar Checkout ---
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
                    color = White
                )
            }
            Button(
                onClick = onCheckoutClick,
                modifier = Modifier
                    .padding(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Cream, // DIUBAH
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer // DIUBAH
                ),
                shape = MaterialTheme.shapes.small // DIUBAH
            ) {
                Text("Checkout", style = MaterialTheme.typography.titleMedium)
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
@Preview(showBackground = true)
@Composable
fun CartScreenPreview() {
    LaundryGoTheme {
        CartScreen(onBack = {}, onCheckoutClick = {})
    }
}