package com.android.laundrygo.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.android.laundrygo.ui.theme.Cream
import com.android.laundrygo.ui.theme.DarkBlue
import com.android.laundrygo.ui.theme.lightNavy
import com.android.laundrygo.viewmodel.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionScreen(
    viewModel: TransactionViewModel,
    onBack: () -> Unit,
    onCheckoutSuccess: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showDatePicker by remember { mutableStateOf(false) }

    // Efek untuk menampilkan pesan error dari ViewModel
    LaunchedEffect(uiState.error) {
        uiState.error?.let { errorMessage ->
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = DarkBlue,
        unfocusedTextColor = DarkBlue,
        focusedLeadingIconColor = DarkBlue,
        unfocusedLeadingIconColor = DarkBlue.copy(alpha = 0.7f),
        focusedLabelColor = DarkBlue,
        focusedBorderColor = DarkBlue,
        unfocusedBorderColor = DarkBlue.copy(alpha = 0.5f)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Detail Transaksi",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Informasi Pelanggan", style = MaterialTheme.typography.titleMedium)

                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = viewModel::onNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nama Lengkap") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    colors = textFieldColors
                )
                OutlinedTextField(
                    value = uiState.phone,
                    onValueChange = viewModel::onPhoneChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nomor Telepon") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    colors = textFieldColors
                )
                OutlinedTextField(
                    value = uiState.address,
                    onValueChange = viewModel::onAddressChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Alamat Penjemputan") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    colors = textFieldColors
                )

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Text("Jadwal Penjemputan", style = MaterialTheme.typography.titleMedium)

                OutlinedTextField(
                    value = formatDate(uiState.pickupDateMillis),
                    onValueChange = { },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Tanggal Penjemputan") },
                    leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(
                                Icons.Default.EditCalendar,
                                contentDescription = "Pilih Tanggal",
                                tint = DarkBlue
                            )
                        }
                    },
                    colors = textFieldColors
                )

                var expandedTime by remember { mutableStateOf(false) }
                val timeSlots = listOf(
                    "08:00", "09:00", "10:00", "11:00", "12:00",
                    "13:00", "14:00", "15:00", "16:00", "17:00"
                )
                ExposedDropdownMenuBox(
                    expanded = expandedTime,
                    onExpandedChange = { expandedTime = !expandedTime }
                ) {
                    OutlinedTextField(
                        value = uiState.selectedTime ?: "Pilih waktu",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        label = { Text("Waktu Penjemputan") },
                        leadingIcon = { Icon(Icons.Default.AccessTime, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTime) },
                        colors = textFieldColors
                    )
                    ExposedDropdownMenu(
                        expanded = expandedTime,
                        onDismissRequest = { expandedTime = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        timeSlots.forEach { time ->
                            DropdownMenuItem(
                                text = { Text(time, color = DarkBlue) },
                                onClick = {
                                    viewModel.onTimeSelected(time)
                                    expandedTime = false
                                }
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    viewModel.onCheckout { transactionId ->
                        onCheckoutSuccess(transactionId)
                    }
                },
                enabled = uiState.isFormValid && !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(48.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkBlue,
                    contentColor = Color.White,
                    disabledContainerColor = Color.Gray
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 3.dp
                    )
                } else {
                    Text(
                        "Lanjutkan ke Pembayaran",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (showDatePicker) {
        val selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val today = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }
                return utcTimeMillis >= today.timeInMillis
            }
        }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = System.currentTimeMillis(),
            selectableDates = selectableDates
        )

        Dialog(
            onDismissRequest = { showDatePicker = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = DarkBlue
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    DatePicker(
                        state = datePickerState,
                        showModeToggle = false,
                        colors = DatePickerDefaults.colors(
                            containerColor = DarkBlue,
                            titleContentColor = Cream,
                            headlineContentColor = Cream, // For "<", ">", and Month (Let's keep this)
                            navigationContentColor = Cream, // Try this for Month and Year
                            weekdayContentColor = Cream.copy(alpha = 0.8f),
                            yearContentColor = Cream,
                            selectedDayContainerColor = lightNavy,
                            selectedDayContentColor = Cream,
                            todayDateBorderColor = lightNavy,
                            todayContentColor = Cream,
                            dayContentColor = Cream.copy(alpha = 0.8f),
                            selectedYearContainerColor = lightNavy,
                            selectedYearContentColor = Cream
                        )
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Batal", color = Cream, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Button(
                            onClick = {
                                viewModel.onDateSelected(datePickerState.selectedDateMillis)
                                showDatePicker = false
                            },
                            enabled = datePickerState.selectedDateMillis != null,
                            colors = ButtonDefaults.buttonColors(containerColor = lightNavy)
                        ) {
                            Text("Pilih", color = Cream, fontWeight = FontWeight.Bold) // This is already Cream
                        }
                    }
                }
            }
        }
    }
}

private fun formatDate(millis: Long?): String {
    if (millis == null) return "Belum dipilih"
    val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID"))
    return dateFormat.format(Date(millis))
}
//
//@Preview(showBackground = true)
//@Compoosable
//private fun TransactionScreenPreview() {
//    // 1. Buat Repository Palsu khusus untuk kebutuhan preview ini
//    class FakeTransactionRepository : ServiceRepository {
//        override suspend fun getServices(category: String): Result<List<LaundryService>> = Result.success(emptyList())
//        override fun getCartItems(userId: String): Flow<Result<List<CartItem>>> = flowOf(Result.success(emptyList()))
//        override fun addItemToCart(userId: String, service: LaundryService): Flow<Result<Unit>> = flowOf(Result.success(Unit))
//        override fun removeItemFromCart(userId: String, itemId: String): Flow<Result<Unit>> = flowOf(Result.success(Unit))
//        override fun updateItemQuantity(userId: String, itemId: String, change: Int): Flow<Result<Unit>> = flowOf(Result.success(Unit))
//        override suspend fun createTransaction(transaction: com.android.laundrygo.model.Transaction): Result<String> = Result.success("tx-123")
//        override suspend fun clearCart(userId: String): Result<Unit> = Result.success(Unit)
//        override suspend fun getTransactionById(transactionId: String): Result<Transaction?> {
//            return Result.success(null)
//        }
//    }
//
//    // 2. Buat Factory yang menggunakan Repository Palsu tersebut
//    val mockFactory = object : ViewModelProvider.Factory {
//        @Suppress("UNCHECKED_CAST")
//        override fun <T : ViewModel> create(modelClass: Class<T>): T {
//            return TransactionViewModel(
//                repository = FakeTransactionRepository(),
//                totalPrice = 50000.0
//            ) as T
//        }
//    }
//
//    LaundryGoTheme {
//        TransactionScreen(
//            viewModel = viewModel(factory = mockFactory),
//            onBack = {},
//            onCheckoutSuccess = {}
//        )
//    }
//}