package com.android.laundrygo.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.laundrygo.model.Transaction
import com.android.laundrygo.viewmodel.InProcessViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InProcessScreen(
    viewModel: InProcessViewModel,
    onBackClick: () -> Unit
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val inProcessTransactions by viewModel.inProcessTransactions.collectAsState()
    val selectedTransaction by viewModel.selectedTransaction.collectAsState()

    val statusList = remember {
        listOf(
            "Menunggu Pembayaran", "Lunas", "Pick Up", "Washing", "Washed", "Delivery", "Completed"
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lacak Pesanan", fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {

            // STEP 1: Stepper dan deskripsi
            AnimatedVisibility(
                visible = selectedTransaction != null,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    selectedTransaction?.let { transaction ->
                        Text(
                            text = getStatusDescription(transaction.status),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        HorizontalStepper(
                            currentStep = transaction.status,
                            steps = statusList.drop(1)
                        )
                    }
                }
            }


            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                errorMessage != null -> {
                    Text(
                        errorMessage ?: "Terjadi kesalahan",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))


            Text(
                "Daftar Pesanan Anda",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(inProcessTransactions, key = { it.id }) { transaction ->
                    OrderItem(
                        transaction = transaction,
                        isSelected = selectedTransaction?.id == transaction.id,
                        onItemClick = { viewModel.selectTransaction(transaction) },
                        statusList = statusList
                    )
                    Divider()
                }
            }
        }
    }
}

@Composable
fun OrderItem(
    transaction: Transaction,
    isSelected: Boolean,
    onItemClick: (Transaction) -> Unit,
    statusList: List<String>
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable { onItemClick(transaction) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "ID: ${transaction.id.take(8).uppercase()}",
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Text(
                text = transaction.createdAt?.toFormattedString() ?: "Tanggal tidak tersedia",
                color = textColor.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
        }

        val currentStatus = statusList.getOrNull(transaction.status - 1) ?: "Status Tidak Diketahui"
        Text(
            text = currentStatus,
            color = textColor,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
    }
}

@Composable
fun HorizontalStepper(
    currentStep: Int,
    steps: List<String>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        steps.forEachIndexed { index, step ->
            val stepNumber = index + 2
            val isCurrent = currentStep == stepNumber
            val isCompleted = currentStep > stepNumber

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = when {
                                isCurrent -> MaterialTheme.colorScheme.primary
                                isCompleted -> Color.Gray
                                else -> Color.LightGray
                            },
                            shape = CircleShape
                        )
                ) {
                    if (isCompleted) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text(
                            text = "$stepNumber",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = step,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )
            }

            if (index != steps.lastIndex) {
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .height(2.dp)
                        .weight(1f)
                        .background(
                            if (currentStep > stepNumber)
                                MaterialTheme.colorScheme.primary
                            else
                                Color.LightGray
                        )
                        .align(Alignment.CenterVertically)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
        }
    }
}

fun getStatusDescription(status: Int): String {
    return when (status) {
        1 -> "Sedang menunggu pembayaran, segera lakukan pembayaran."
        2 -> "Pembayaran sudah berhasil, pesanan akan segera dijemput oleh kurir."
        3 -> "Pesanan Anda sedang dijemput oleh kurir."
        4 -> "Pesanan Anda sedang dalam proses pencucian."
        5 -> "Pencucian telah selesai, menunggu pengantaran."
        6 -> "Kurir sedang mengantarkan pesanan Anda."
        7 -> "Pesanan telah selesai. Terima kasih telah menggunakan layanan kami!"
        else -> "Status tidak diketahui."
    }
}

private fun Date.toFormattedString(): String {
    val format = SimpleDateFormat("EEEE, d MMMM yyyy, HH:mm", Locale("id", "ID"))
    return format.format(this)
}
