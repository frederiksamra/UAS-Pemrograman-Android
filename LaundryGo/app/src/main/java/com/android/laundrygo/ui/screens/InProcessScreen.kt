package com.android.laundrygo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.laundrygo.model.Transaction
import com.android.laundrygo.viewmodel.InProcessViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InProcessScreen(
    viewModel: InProcessViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val inProcessTransactions by viewModel.inProcessTransactions.collectAsState()
    val selectedTransaction by viewModel.selectedTransaction.collectAsState()
    val statusList = remember {
        listOf(
            "Menunggu Pembayaran", // Status 1
            "Lunas",             // Status 2
            "Pick Up",           // Status 3
            "Washing",           // Status 4
            "Washed",            // Status 5
            "Delivery",          // Status 6
            "Completed"          // Status 7
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lacak Pesanan", fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // List of Orders
            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.4f)
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

            Spacer(modifier = Modifier.width(16.dp))

            // Stepper for Selected Order
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.6f)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(16.dp),
                contentAlignment = Alignment.TopStart
            ) {
                if (uiState) {
                    CircularProgressIndicator()
                } else if (errorMessage != null) {
                    Text(errorMessage!!)
                } else if (selectedTransaction != null) {
                    OrderStepper(transaction = selectedTransaction!!, statusList = statusList)
                } else {
                    Text("Pilih pesanan untuk melihat statusnya.")
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
fun OrderStepper(transaction: Transaction, statusList: List<String>) {
    val currentStep = transaction.status
    Stepper(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        // Kita mulai dari status kedua ("Lunas") sebagai langkah pertama di stepper
        statusList.drop(1).forEachIndexed { index, title ->
            // `index` berbasis 0 (0 -> Lunas, 1 -> Pick Up, dst.)
            // `currentStep` berbasis 1 (1 -> Menunggu Pembayaran, 2 -> Lunas, dst.)
            val stepIndexInStatusList = index + 2 // Menyamakan basis (2 -> Lunas, 3 -> Pick Up, dst.)

            val stepState = when {
                stepIndexInStatusList < currentStep -> StepState.Completed
                stepIndexInStatusList == currentStep -> StepState.Current
                else -> StepState.Idle
            }

            Step(
                state = stepState,
                title = { Text(title, fontWeight = FontWeight.Bold) }
            ) {
                // Tampilkan konten tambahan jika ini adalah langkah saat ini
                if (stepState == StepState.Current) {
                    Text(
                        "Pesanan Anda saat ini dalam tahap ini.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

// Buat stepper manual (soalnya gabisa di import)
enum class StepState {
    Idle,      // Langkah yang belum tercapai
    Current,   // Langkah saat ini
    Completed  // Langkah yang sudah selesai
}

interface StepperScope {
    fun Step(
        state: StepState,
        title: @Composable () -> Unit,
        content: @Composable () -> Unit
    )
}

private class StepperScopeImpl : StepperScope {
    val steps = mutableListOf<StepInfo>()

    override fun Step(
        state: StepState,
        title: @Composable () -> Unit,
        content: @Composable () -> Unit
    ) {
        steps.add(StepInfo(state, title, content))
    }
}

private data class StepInfo(
    val state: StepState,
    val title: @Composable () -> Unit,
    val content: @Composable () -> Unit
)

@Composable
fun Stepper(
    modifier: Modifier = Modifier,
    content: @Composable StepperScope.() -> Unit
) {
    val scope = StepperScopeImpl()
    scope.content()
    Column(modifier = modifier) {
        scope.steps.forEachIndexed { index, stepInfo ->
            val isLastStep = index == scope.steps.size - 1
            Row {
                // Kolom untuk Ikon dan Garis
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.wrapContentHeight()
                ) {
                    val primaryColor = MaterialTheme.colorScheme.primary
                    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
                    val inactiveColor = Color.Gray

                    val circleColor = when (stepInfo.state) {
                        StepState.Completed, StepState.Current -> primaryColor
                        StepState.Idle -> inactiveColor
                    }

                    val lineColor = if (stepInfo.state == StepState.Completed) primaryColor else inactiveColor

                    // Lingkaran Indikator
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .drawBehind {
                                val style = if (stepInfo.state == StepState.Current) Stroke(width = 6f) else Fill
                                drawCircle(
                                    color = circleColor,
                                    radius = size.minDimension / 2.0f,
                                    style = style
                                )
                            }
                    ) {
                        if (stepInfo.state == StepState.Completed) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Selesai",
                                tint = onPrimaryColor,
                                modifier = Modifier
                                    .size(16.dp)
                                    .align(Alignment.Center)
                            )
                        }
                    }

                    // Garis Vertikal
                    if (!isLastStep) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .weight(1f)
                                .background(lineColor)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }

                // Kolom untuk Judul dan Konten
                Column(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .padding(bottom = if (!isLastStep) 24.dp else 0.dp) // Beri jarak antar step
                ) {
                    stepInfo.title()
                    stepInfo.content()
                }
            }
        }
    }
}

private fun Date.toFormattedString(): String {
    val format = SimpleDateFormat("EEEE, d MMMM yyyy, HH:mm", Locale("id", "ID"))
    return format.format(this)
}