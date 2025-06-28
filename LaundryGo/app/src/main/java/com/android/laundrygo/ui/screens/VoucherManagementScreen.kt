package com.android.laundrygo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.laundrygo.model.Voucher
import com.android.laundrygo.repository.AuthRepositoryImpl
import com.android.laundrygo.ui.theme.Cream
import com.android.laundrygo.ui.theme.DarkBlue
import com.android.laundrygo.ui.theme.lightNavy
import com.android.laundrygo.util.formatRupiah
import com.android.laundrygo.viewmodel.VoucherManagementViewModel
import com.android.laundrygo.viewmodel.VoucherManagementViewModelFactory
import java.text.SimpleDateFormat
import java.util.*


@Composable
fun VoucherManagementScreen(viewModel: VoucherManagementViewModel = viewModel(factory = VoucherManagementViewModelFactory(
    AuthRepositoryImpl()
))) {
    val vouchers by viewModel.vouchers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val feedbackMessage by viewModel.feedbackMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(feedbackMessage) {
        feedbackMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearFeedbackMessage()
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = DarkBlue,
                contentColor = Cream
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Voucher")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        content = { paddingValues ->
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items = vouchers, key = { it.voucher_code }) { voucher ->
                        VoucherItemCard(voucher = voucher)
                    }
                }

                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                if (showAddDialog) {
                    AddVoucherDialog(
                        onDismissRequest = { showAddDialog = false },
                        onConfirm = { code, desc, discount, type, date ->
                            viewModel.addVoucher(code, desc, discount, type, date)
                            showAddDialog = false
                        }
                    )
                }
            }
        }
    )
}

@Composable
private fun VoucherItemCard(voucher: Voucher) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Cream)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = voucher.voucher_code,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            val discountText = if (voucher.discount_type == "percent") {
                "Diskon ${voucher.discount_value.toInt()}%"
            } else {
                formatRupiah(voucher.discount_value)
            }
            Text(
                text = discountText,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )

            val expiryText = voucher.valid_until?.toDate()?.let {
                "Berlaku hingga: ${SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(it)}"
            } ?: "Berlaku selamanya"
            Text(text = expiryText, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddVoucherDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (code: String, description: String, discount: Double, type: String, expiryDate: Date?) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var discount by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf<Date?>(null) }
    var discountType by remember { mutableStateOf("percent") }

    var showDatePicker by remember { mutableStateOf(false) }
    val selectableDates = object : SelectableDates {
        override fun isSelectableDate(utcTimeMillis: Long): Boolean {
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            return utcTimeMillis >= today.timeInMillis
        }
    }
    val datePickerState = rememberDatePickerState(selectableDates = selectableDates)

    if (showDatePicker) {
        Dialog(
            onDismissRequest = { showDatePicker = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(shape = RoundedCornerShape(28.dp), color = DarkBlue) {
                Column(
                    modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    DatePicker(
                        state = datePickerState,
                        showModeToggle = false,
                        colors = DatePickerDefaults.colors(
                            containerColor = DarkBlue, titleContentColor = Cream, headlineContentColor = Cream,
                            navigationContentColor = Cream, weekdayContentColor = Cream.copy(alpha = 0.8f),
                            yearContentColor = Cream, selectedDayContainerColor = lightNavy,
                            selectedDayContentColor = Cream, todayDateBorderColor = lightNavy,
                            todayContentColor = Cream, dayContentColor = Cream.copy(alpha = 0.8f),
                            selectedYearContainerColor = lightNavy, selectedYearContentColor = Cream
                        )
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Batal", color = Cream, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Button(
                            onClick = {
                                datePickerState.selectedDateMillis?.let { utcMillis ->
                                    // 1. Dapatkan offset timezone lokal dari UTC
                                    val tz = TimeZone.getDefault()
                                    val offset = tz.getOffset(utcMillis)

                                    // 2. Sesuaikan milidetik UTC dengan offset lokal
                                    // Ini memastikan tanggal yang dipilih adalah tanggal lokal yang benar.
                                    val localMillis = utcMillis + offset

                                    // 3. Buat objek Date dari milidetik yang sudah disesuaikan
                                    val localDate = Date(localMillis)

                                    // Set waktu ke akhir hari agar berlaku seharian penuh
                                    val calendar = Calendar.getInstance()
                                    calendar.time = localDate
                                    calendar.set(Calendar.HOUR_OF_DAY, 23)
                                    calendar.set(Calendar.MINUTE, 59)
                                    calendar.set(Calendar.SECOND, 59)

                                    expiryDate = calendar.time
                                }
                                showDatePicker = false
                            },
                            enabled = datePickerState.selectedDateMillis != null,
                            colors = ButtonDefaults.buttonColors(containerColor = lightNavy)
                        ) { Text("Pilih", color = Cream, fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = DarkBlue, unfocusedTextColor = DarkBlue,
        focusedBorderColor = DarkBlue, unfocusedBorderColor = DarkBlue.copy(alpha = 0.5f),
        focusedLabelColor = DarkBlue, unfocusedLabelColor = DarkBlue.copy(alpha = 0.7f),
        focusedTrailingIconColor = DarkBlue, unfocusedTrailingIconColor = DarkBlue.copy(alpha = 0.7f),
        cursorColor = DarkBlue
    )

    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = Cream, titleContentColor = DarkBlue, textContentColor = DarkBlue,
        title = { Text("Tambah Voucher Baru") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Kode Voucher") }, colors = textFieldColors)
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Deskripsi") }, colors = textFieldColors)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = discountType == "percent", onClick = { discountType = "percent" },
                        colors = RadioButtonDefaults.colors(selectedColor = DarkBlue, unselectedColor = DarkBlue)
                    )
                    Text("Persen (%)", modifier = Modifier.padding(start = 2.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(
                        selected = discountType == "fixed", onClick = { discountType = "fixed" },
                        colors = RadioButtonDefaults.colors(selectedColor = DarkBlue, unselectedColor = DarkBlue)
                    )
                    Text("Rupiah (Rp)", modifier = Modifier.padding(start = 2.dp))
                }
                OutlinedTextField(
                    value = discount, onValueChange = { discount = it.filter { char -> char.isDigit() } },
                    label = { Text(if (discountType == "percent") "Diskon (%)" else "Diskon (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = textFieldColors
                )
                OutlinedTextField(
                    value = expiryDate?.let { SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(it) } ?: "",
                    onValueChange = {}, label = { Text("Tanggal Berakhir (Opsional)") }, readOnly = true,
                    trailingIcon = { IconButton(onClick = { showDatePicker = true }) { Icon(Icons.Default.DateRange, contentDescription = "Pilih Tanggal") } },
                    colors = textFieldColors
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(code, description, discount.toDoubleOrNull() ?: 0.0, discountType, expiryDate) }) { Text("Simpan") }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text("Batal") }
        }
    )
}
