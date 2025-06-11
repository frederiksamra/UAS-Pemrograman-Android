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
fun ShirtPantsScreen(
    onBack: () -> Unit = {},
    onAddClick: (String) -> Unit = {},
    onCartClick: () -> Unit = {}
) {
    val blue = Color(0xFF1565C0)
    val cream = Color(0xFFFFFDE7)
    val grey = Color(0xFFF5F5F5)
    val cards = listOf(
        ShirtPantsServiceCardData(
            title = "King Package",
            desc = "Wash express, ironing, pick up and drop off",
            price = "IDR 35.000/Kg",
            backgroundColor = grey
        ),
        ShirtPantsServiceCardData(
            title = "General Package",
            desc = "Express washing and ironing",
            price = "IDR 30.000/Kg",
            backgroundColor = cream
        ),
        ShirtPantsServiceCardData(
            title = "Extra Regular Package",
            desc = "Regular washing, ironing, pick up and delivery",
            price = "IDR 20.000/Kg",
            backgroundColor = grey
        ),
        ShirtPantsServiceCardData(
            title = "Regular Package",
            desc = "Regular washing and ironing",
            price = "IDR 14.000/Kg",
            backgroundColor = cream
        )
    )
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Shirt & Pants",
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
            cards.forEach { card ->
                ShirtPantsServiceCard(
                    title = card.title,
                    desc = card.desc,
                    price = card.price,
                    backgroundColor = card.backgroundColor,
                    textColor = blue,
                    onAddClick = { onAddClick(card.title) }
                )
            }
        }
    }
}

@Composable
fun ShirtPantsServiceCard(
    title: String,
    desc: String,
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
                Text(
                    text = desc,
                    color = textColor,
                    fontSize = 14.sp
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

data class ShirtPantsServiceCardData(val title: String, val desc: String, val price: String, val backgroundColor: Color)

@Preview(showBackground = true)
@Composable
fun ShirtPantsScreenPreview() {
    ShirtPantsScreen()
}

