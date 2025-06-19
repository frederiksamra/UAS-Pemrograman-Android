package com.android.laundrygo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.laundrygo.R
import com.android.laundrygo.ui.theme.AppTypography
import com.android.laundrygo.viewmodel.InProcessViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InProcessScreen(
    viewModel: InProcessViewModel = viewModel(),
    onBackClick: () -> Unit = {}
) {
    val Grey = Color(0xFFE1E1E1)
    val NavGrey = Color(0xFFB0B0B0)
    val TextBlue = Color(0xFF435585)

    val selectedIndex by viewModel.selectedIndex.collectAsState()

    val navItems = listOf("Pick Up", "Washing", "Washed", "Delivery")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "In Process",
                        fontSize = 20.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF26326A)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Grey)
                .padding(padding)
                .padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NavGrey)
                    .padding(8.dp)
            ) {
                navItems.forEachIndexed { index, item ->
                    val isSelected = index == selectedIndex
                    Text(
                        text = item,
                        modifier = Modifier
                            .weight(1f)
                            .selectable(
                                selected = isSelected,
                                onClick = { viewModel.selectTab(index) }
                            )
                            .padding(vertical = 8.dp),
                        color = if (isSelected) Grey else TextBlue,
                        fontWeight = FontWeight.Bold,
                        style = AppTypography.titleLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                when (selectedIndex) {
                    0 -> ProcessContent(
                        iconRes = R.drawable.on_the_way,
                        text = "Excited to announce that our courier is on the way to pick up your laundry from your location!\n\nYour convenience is our priority. Sit tight, your clothes are in good hands!",
                        textColor = TextBlue
                    )
                    1 -> ProcessContent(
                        iconRes = R.drawable.in_process,
                        text = "Your laundry are currently undergoing the rejuvenating process at our expert hands.\n\nWe're committed to delivering them back to you fresh and clean, just the way you like it!",
                        textColor = TextBlue
                    )
                    2 -> ProcessContent(
                        iconRes = R.drawable.process_done,
                        text = "Your order has been impeccably laundered and is ready for delivery!\n\nWe've tallied up the total bill for your convenience. Please review it at your earliest convenience here.",
                        textColor = TextBlue
                    )
                    3 -> ProcessContent(
                        iconRes = R.drawable.delivery,
                        text = "Great news! Your freshly cleaned clothes are en route to your doorstep with our dedicated courier.\n\nWe hope you're thrilled with the results. Enjoy your clean and crisp laundry!",
                        textColor = TextBlue
                    )
                }
            }
        }
    }
}

@Composable
fun ProcessContent(iconRes: Int, text: String, textColor: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier
                .fillMaxWidth(0.5f) // Ikut skala device, setengah lebar layar
                .aspectRatio(1f)
                .padding(16.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = text,
            color = textColor,
            style = AppTypography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}
