package com.android.laundrygo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Toys
import androidx.compose.material.icons.filled.Backpack
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DryCleaning
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.android.laundrygo.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceTypeScreen(
    onBack: () -> Unit = {},
    onBag: () -> Unit = {},
    onDoll: () -> Unit = {},
    onShirtPants: () -> Unit = {},
    onShoes: () -> Unit = {},
    onSpecialTreatment: () -> Unit = {}
) {
    val services = listOf(
        ServiceTypeItem("Shirt and Pants", Icons.Default.Checkroom),
        ServiceTypeItem("Special Treatment", Icons.Default.DryCleaning),
        ServiceTypeItem("Doll", Icons.Default.EmojiEmotions),
        ServiceTypeItem("Bag", Icons.Default.ShoppingBag),
        ServiceTypeItem("Shoes", Icons.Default.DirectionsRun)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Service Type",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_black),
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1565C0)
                )
            )
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(services) { service ->
                    val onClick: () -> Unit = when (service.label) {
                        "Bag" -> onBag
                        "Doll" -> onDoll
                        "Shirt and Pants" -> onShirtPants
                        "Shoes" -> onShoes
                        "Special Treatment" -> onSpecialTreatment
                        else -> { {} }
                    }
                    Card(
                        modifier = Modifier
                            .height(180.dp)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFFDE7)
                        ),
                        elevation = CardDefaults.cardElevation(6.dp),
                        onClick = onClick
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = service.icon,
                                contentDescription = service.label,
                                modifier = Modifier.size(80.dp),
                                tint = Color(0xFF1565C0)
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = onClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = Color.Black
                                ),
                                elevation = null,
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                            ) {
                                Text(
                                    text = service.label,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

data class ServiceTypeItem(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@Preview(showBackground = true)
@Composable
fun ServiceTypeScreenPreview() {
    ServiceTypeScreen()
}

