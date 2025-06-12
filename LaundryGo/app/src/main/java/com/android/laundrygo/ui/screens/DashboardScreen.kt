package com.android.laundrygo.ui.screens

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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.laundrygo.R
import com.android.laundrygo.ui.theme.LaundryGoTheme
import com.android.laundrygo.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel(),
    onNavigateToServiceType: () -> Unit = {},
    onNavigateToLocation: () -> Unit = {},
    onNavigateToCart: () -> Unit = {},
    onNavigateToVoucher: () -> Unit = {},
    onNavigateToTopUp: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    val userName by viewModel.userName.observeAsState("User")
    val userBalance by viewModel.userBalance.observeAsState("0")
    val error by viewModel.error.observeAsState()

    DashboardScreenContent(
        userName = userName,
        userBalance = userBalance,
        error = error,
        onTopUpClick = onNavigateToTopUp,
        onSearchClick = { /* Handle search */ },
        onFeatureClick = { feature -> /* Handle feature clicks, ini akan ditangani di bawah */ },
        onClaimVoucherClick = { voucherId -> viewModel.claimVoucher(voucherId) },
        onNavigateToServiceType = onNavigateToServiceType,
        onNavigateToLocation = onNavigateToLocation,
        onNavigateToCart = onNavigateToCart,
        onNavigateToVoucher = onNavigateToVoucher,
        onNavigateToTopUp = onNavigateToTopUp,
        onNavigateToProfile = onNavigateToProfile
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
    onNavigateToServiceType: () -> Unit,
    onNavigateToLocation: () -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToVoucher: () -> Unit,
    onNavigateToTopUp: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        HeaderSection(
            userName = userName,
            userBalance = userBalance,
            onSearchClick = onSearchClick,
            onTopUpClick = onTopUpClick,
            onNavigateToProfile = onNavigateToProfile
        )

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

        FeaturesSection(onFeatureClick = { featureId ->
            when (featureId) {
                "service_type" -> onNavigateToServiceType()
                "nearest_location" -> onNavigateToLocation()
                "cart" -> onNavigateToCart()
                "voucher" -> onNavigateToVoucher()
                // Logika when ini sudah benar, masalahnya ada di daftar fitur
                "top_up" -> onNavigateToTopUp()
                else -> onFeatureClick(featureId)
            }
        })
        PromoSection(onClaimVoucherClick = onClaimVoucherClick)
    }
}

@Composable
private fun HeaderSection(
    userName: String,
    userBalance: String,
    onSearchClick: () -> Unit,
    onTopUpClick: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(165.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary, // DIUBAH
                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onSearchClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background), // DIUBAH
                    shape = MaterialTheme.shapes.small // DIUBAH
                ) {
                    Text(
                        text = "Search",
                        color = MaterialTheme.colorScheme.onBackground, // DIUBAH
                        style = MaterialTheme.typography.labelLarge // DIUBAH
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .clickable { onNavigateToProfile() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = MaterialTheme.colorScheme.onPrimary, // DIUBAH
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium, // DIUBAH
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), // DIUBAH
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Hello, $userName",
                        style = MaterialTheme.typography.titleLarge, // DIUBAH
                        color = MaterialTheme.colorScheme.onBackground // DIUBAH
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.AccountBalanceWallet, "Wallet",
                            tint = MaterialTheme.colorScheme.onBackground, // DIUBAH
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Rp $userBalance",
                            style = MaterialTheme.typography.titleLarge, // DIUBAH
                            color = MaterialTheme.colorScheme.onBackground // DIUBAH
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(40.dp)
                                .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)) // DIUBAH
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Row(
                            modifier = Modifier.clickable { onTopUpClick() },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Add, "Top Up",
                                tint = MaterialTheme.colorScheme.onBackground, // DIUBAH
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Top Up",
                                style = MaterialTheme.typography.labelLarge, // DIUBAH
                                color = MaterialTheme.colorScheme.onBackground // DIUBAH
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
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Features",
            style = MaterialTheme.typography.headlineLarge, // DIUBAH
            color = MaterialTheme.colorScheme.primary, // DIUBAH
            modifier = Modifier.padding(bottom = 8.dp)
        )

        val features = listOf(
            FeatureItem("Service Type", IconType.DrawableResource(R.drawable.service_type), "service_type"),
            FeatureItem("Cart", IconType.DrawableResource(R.drawable.cart), "cart"),
            FeatureItem("Nearest LaundryGo", IconType.DrawableResource(R.drawable.location), "nearest_location"),
            FeatureItem("In Process", IconType.DrawableResource(R.drawable.in_process), "in_process"),
            FeatureItem("Your Voucher", IconType.DrawableResource(R.drawable.voucher), "voucher"),
            FeatureItem("History", IconType.ImageVectorIcon(Icons.Default.History), "history"),
            FeatureItem("Top Up", IconType.ImageVectorIcon(Icons.Default.AccountBalanceWallet), "top_up")
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(features) { feature ->
                FeatureCard(feature = feature, onClick = { onFeatureClick(feature.id) })
            }
        }
    }
}

@Composable
private fun FeatureCard(feature: FeatureItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(106.dp)
            .height(172.dp)
            .clickable { onClick() },
        shape = MaterialTheme.shapes.medium, // DIUBAH
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), // DIUBAH
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (val icon = feature.icon) {
                is IconType.DrawableResource -> Image(
                    painterResource(id = icon.id), feature.name,
                    modifier = Modifier.size(64.dp), contentScale = ContentScale.Fit
                )
                is IconType.ImageVectorIcon -> Icon(
                    icon.imageVector, feature.name,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant // DIUBAH
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = feature.name,
                style = MaterialTheme.typography.labelLarge, // DIUBAH
                color = MaterialTheme.colorScheme.onSurfaceVariant, // DIUBAH
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PromoSection(onClaimVoucherClick: (String) -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Promo",
            style = MaterialTheme.typography.headlineLarge, // DIUBAH
            color = MaterialTheme.colorScheme.primary, // DIUBAH
            modifier = Modifier.padding(bottom = 8.dp)
        )

        repeat(3) { index ->
            VoucherCard(voucherId = "voucher_${index + 1}", onClaimClick = onClaimVoucherClick)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun VoucherCard(voucherId: String, onClaimClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(206.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Special Offer!",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = "Get 20% discount on your next laundry service",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            Button(
                onClick = { onClaimClick(voucherId) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer, // DIUBAH
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer // DIUBAH
                ),
                shape = MaterialTheme.shapes.small // DIUBAH
            ) {
                Text("Claim", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

// Data classes
sealed class IconType {
    data class ImageVectorIcon(val imageVector: ImageVector) : IconType()
    data class DrawableResource(val id: Int) : IconType()
}
data class FeatureItem(val name: String, val icon: IconType, val id: String)


@Preview(showBackground = true, name = "Dashboard Normal State")
@Composable
fun DashboardScreenPreview() {
    LaundryGoTheme {
        DashboardScreenContent(
            userName = "Saoirse",
            userBalance = "1.500.000",
            error = null,
            onTopUpClick = {},
            onSearchClick = {},
            onFeatureClick = {},
            onClaimVoucherClick = {},
            onNavigateToServiceType = {},
            onNavigateToLocation = {},
            onNavigateToCart = {},
            onNavigateToVoucher = {},
            onNavigateToTopUp = {},
            onNavigateToProfile = {}
        )
    }
}

@Preview(showBackground = true, name = "Dashboard Error State")
@Composable
fun DashboardScreenErrorPreview() {
    LaundryGoTheme {
        // Preview untuk kondisi saat ada error
        DashboardScreenContent(
            userName = "User",
            userBalance = "0",
            error = "Gagal memuat data pengguna. Silakan coba lagi.",
            onTopUpClick = {},
            onSearchClick = {},
            onFeatureClick = {},
            onClaimVoucherClick = {},
            onNavigateToServiceType = {},
            onNavigateToLocation = {},
            onNavigateToCart = {},
            onNavigateToVoucher = {},
            onNavigateToTopUp = {},
            onNavigateToProfile = {}
        )
    }
}