package com.android.laundrygo.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.livedata.observeAsState
import com.android.laundrygo.viewmodel.DashboardViewModel
import com.android.laundrygo.ui.theme.LaundryGoTheme
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.History

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel()
) {
    val userName by viewModel.userName.observeAsState("User")
    val userBalance by viewModel.userBalance.observeAsState("0")
    val error by viewModel.error.observeAsState()

    DashboardScreenContent(
        userName = userName,
        userBalance = userBalance,
        error = error,
        onTopUpClick = { viewModel.openTopUpScreen() },
        onSearchClick = { /* Handle search */ },
        onFeatureClick = { feature -> /* Handle feature clicks */ },
        onClaimVoucherClick = { voucherId -> viewModel.claimVoucher(voucherId) },
        onRefreshClick = { viewModel.refreshDashboardData() }
    )
}

@Composable
private fun DashboardScreenContent(
    userName: String,
    userBalance: String,
    error: String?,
    onTopUpClick: () -> Unit,
    onSearchClick: () -> Unit,
    onFeatureClick: (String) -> Unit,
    onClaimVoucherClick: (String) -> Unit,
    onRefreshClick: () -> Unit
) {
    LaundryGoTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Section
            HeaderSection(
                userName = userName,
                userBalance = userBalance,
                onSearchClick = onSearchClick,
                onTopUpClick = onTopUpClick
            )

            // Show error if exists
            error?.let {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // Features Section
            FeaturesSection(onFeatureClick = onFeatureClick)

            // Promo Section
            PromoSection(onClaimVoucherClick = onClaimVoucherClick)
        }
    }
}

@Composable
private fun HeaderSection(
    userName: String,
    userBalance: String,
    onSearchClick: () -> Unit,
    onTopUpClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        // Header Background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(165.dp)
                .background(
                    color = Color(0xFF435585),
                    shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Search Bar and Profile
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Search Button
                Button(
                    onClick = onSearchClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Search",
                        color = Color(0xFF435585),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Profile Image
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = Color.White,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Balance Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Hello, $userName",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF344767)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Wallet Icon
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = "Wallet",
                            tint = Color(0xFF344767),
                            modifier = Modifier.size(40.dp)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        // Balance Text
                        Text(
                            text = "Rp $userBalance",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF344767)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        // Divider
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(40.dp)
                                .background(Color(0xFF344767))
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        // Top Up Button
                        Row(
                            modifier = Modifier.clickable { onTopUpClick() },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Top Up",
                                tint = Color(0xFF344767),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Top Up",
                                fontSize = 14.sp,
                                color = Color(0xFF344767)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeaturesSection(onFeatureClick: (String) -> Unit) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "Features",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF435585),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        val features = listOf(
            FeatureItem("Service Type", Icons.Default.Build, "service_type"),
            FeatureItem("Cart", Icons.Default.ShoppingCart, "cart"),
            FeatureItem("Nearest LaundryGo", Icons.Default.LocationOn, "nearest_location"),
            FeatureItem("In Process", Icons.Default.Schedule, "in_process"),  // Use Schedule icon
            FeatureItem("Your Voucher", Icons.Default.CardGiftcard, "voucher"),  // Use CardGiftcard icon
            FeatureItem("History", Icons.Default.History, "history")  // Use History icon
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            items(features) { feature ->
                FeatureCard(
                    feature = feature,
                    onClick = { onFeatureClick(feature.id) }
                )
            }
        }
    }
}

@Composable
private fun FeatureCard(
    feature: FeatureItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(106.dp)
            .height(172.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE1E1E1)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = feature.icon,
                contentDescription = feature.name,
                modifier = Modifier.size(64.dp),
                tint = Color(0xFF435585)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = feature.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun PromoSection(onClaimVoucherClick: (String) -> Unit) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "Promo",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF435585),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        repeat(3) { index ->
            VoucherCard(
                voucherId = "voucher_${index + 1}",
                onClaimClick = onClaimVoucherClick
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun VoucherCard(
    voucherId: String,
    onClaimClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(206.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Voucher Background (placeholder)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = Color(0xFF435585),
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Special Offer!",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Get 20% discount on your next laundry service",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }

            // Claim Button
            Button(
                onClick = { onClaimClick(voucherId) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B2D30)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Claim",
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        }
    }
}

data class FeatureItem(
    val name: String,
    val icon: ImageVector,
    val id: String
)

// Preview Functions
@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    DashboardScreenContent(
        userName = "Saoirse",
        userBalance = "1.500.000",
        error = null,
        onTopUpClick = {},
        onSearchClick = {},
        onFeatureClick = {},
        onClaimVoucherClick = {},
        onRefreshClick = {}
    )
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenErrorPreview() {
    DashboardScreenContent(
        userName = "User",
        userBalance = "0",
        error = "Failed to load user data",
        onTopUpClick = {},
        onSearchClick = {},
        onFeatureClick = {},
        onClaimVoucherClick = {},
        onRefreshClick = {}
    )
}