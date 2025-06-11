package com.android.laundrygo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.android.laundrygo.R
import com.android.laundrygo.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BagScreen(
    onBack: () -> Unit = {},
    onAddClick: (String) -> Unit = {},
    onCartClick: () -> Unit = {}
) {
    val blue = Color(0xFF1565C0)
    val cream = Color(0xFFFFFDE7)
    val grey = Color(0xFFF5F5F5)
    val black = Color(0x000000)
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Bag",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = White
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
                actions = {
                    IconButton(onClick = onCartClick) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Cart",
                            tint = White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1565C0)
                )
            )
        },
        containerColor = grey
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BagServiceCard(
                title = "Deep Cleaning Bag",
                price = "IDR 70.000/Pcs",
                backgroundColor = grey,
                textColor = blue,
                onAddClick = { onAddClick("Deep Cleaning Bag") }
            )
            BagServiceCard(
                title = "Premium Cleaning Bag",
                price = "IDR 90.000/Pcs",
                backgroundColor = cream,
                textColor = blue,
                onAddClick = { onAddClick("Premium Cleaning Bag") }
            )
            BagServiceCard(
                title = "Fast Cleaning Bag",
                price = "IDR 40.000/Pcs",
                backgroundColor = grey,
                textColor = blue,
                onAddClick = { onAddClick("Fast Cleaning Bag") }
            )
        }
    }
}

@Composable
fun BagServiceCard(
    title: String,
    price: String,
    backgroundColor: Color,
    textColor: Color,
    onAddClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp, vertical = 8.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = textColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = price,
                    color = textColor,
                    fontSize = 16.sp
                )
            }
            IconButton(
                onClick = onAddClick,
                modifier = Modifier
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = textColor,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BagScreenPreview() {
    BagScreen()
}

