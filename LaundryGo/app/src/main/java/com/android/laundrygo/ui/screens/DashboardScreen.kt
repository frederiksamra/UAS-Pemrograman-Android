

package com.android.laundrygo.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.laundrygo.R
import com.android.laundrygo.model.User
import com.android.laundrygo.model.Voucher
import com.android.laundrygo.ui.InitialsProfilePicture
import com.android.laundrygo.ui.theme.*
import com.android.laundrygo.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToServiceType: () -> Unit,
    onNavigateToLocation: () -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToVoucher: () -> Unit,
    onNavigateToTopUp: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToInProcess: () -> Unit
) {
    // Ambil satu UiState object dari ViewModel
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                // verticalScroll REMOVED here as discussed before
            ) {
                HeaderSection(
                    user = uiState.user,
                    userName = uiState.userName,
                    userBalance = uiState.userBalance,
                    onSearchClick = { /* TODO: Implement search */ },
                    onTopUpClick = onNavigateToTopUp,
                    onNavigateToProfile = onNavigateToProfile
                )

                uiState.error?.let {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
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
                        "top_up" -> onNavigateToTopUp()
                        "history" -> onNavigateToHistory()
                        "in_process" -> onNavigateToInProcess()
                    }
                })

                // "Promo" title now here
                Text(
                    text = "Promo",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )

                // Promo Section now only contains the LazyColumn of vouchers
                PromoSection(
                    vouchers = uiState.vouchers,
                    onClaimVoucherClick = { voucherId ->
                        viewModel.claimVoucher(voucherId)
                    }
                )
            }
        }
    }
}

// --- Semua Composable private di bawah ini tidak perlu diubah, mereka sudah bagus ---

@Composable
private fun HeaderSection(
    user: User?,
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
                    color = MaterialTheme.colorScheme.primary,
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
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "Search",
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Box(modifier = Modifier.clickable { onNavigateToProfile() }) {
                    InitialsProfilePicture(
                        name = user?.name ?: "",
                        size = 62.dp,
                        textStyle = MaterialTheme.typography.headlineSmall.copy(color = Color.White)
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Hello, $userName",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.AccountBalanceWallet, "Wallet",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = userBalance, // Menggunakan userBalance yang sudah diformat
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(40.dp)
                                .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f))
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Row(
                            modifier = Modifier.clickable { onTopUpClick() },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Add, "Top Up",
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Top Up",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onBackground
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
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
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
fun PromoSection(
    vouchers: List<Voucher>,
    onClaimVoucherClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight() // You might want to adjust this based on your layout needs
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (vouchers.isEmpty()) {
            item {
                Text(
                    text = "Tidak ada voucher tersedia saat ini.",
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        } else {
            items(vouchers, key = { it.voucherDocumentId }) { voucher ->
                M3VoucherCard(
                    voucher = voucher,
                    onUseClick = { onClaimVoucherClick(voucher.voucherDocumentId) }
                )
            }
        }
    }
}


// DESAIN M3 VOUCHER CARD BARU
@Composable
private fun M3VoucherCard(voucher: Voucher, onUseClick: () -> Unit) {
    Card(
        modifier = Modifier.width(300.dp), // Beri lebar agar konsisten di LazyRow
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min) // Tinggi fleksibel sesuai konten
        ) {
            // Bagian Kiri (Ikon dan Aksen)
            Column(
                modifier = Modifier
                    .background(DarkBlue)
                    .fillMaxHeight()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ConfirmationNumber,
                    contentDescription = "Voucher Icon",
                    tint = White,
                    modifier = Modifier.size(48.dp)
                )
            }

            // Garis putus-putus sebagai pemisah
            DashedDivider()

            // Bagian Kanan (Detail dan Tombol)
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .weight(1f)
            ) {
                Text(
                    text = voucher.voucher_code,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DarkBlueText
                )
                Text(
                    text = "Diskon senilai Rp ${voucher.discount_value}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BlackText,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onUseClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkBlueText,
                        contentColor = White
                    )
                ) {
                    Text("Claim", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

// Composable helper untuk membuat garis putus-putus
@Composable
fun DashedDivider() {
    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
    Canvas(
        modifier = Modifier
            .fillMaxHeight()
            .width(1.dp)
    ) {
        drawLine(
            color = Grey,
            start = Offset(0f, 0f),
            end = Offset(0f, size.height),
            pathEffect = pathEffect
        )
    }
}


// Data classes
sealed class IconType {
    data class ImageVectorIcon(val imageVector: ImageVector) : IconType()
    data class DrawableResource(val id: Int) : IconType()
}
data class FeatureItem(val name: String, val icon: IconType, val id: String)

//
//@Preview(showBackground = true, name = "Dashboard Normal State")
//@Composable
//fun DashboardScreenPreview() {
//    LaundryGoTheme {
//        DashboardScreenContent(
//            user = User(userId = "preview_id", name = "Saoirse"),
//            userName = "Saoirse",
//            userBalance = "1.500.000",
//            error = null,
//            onTopUpClick = {},
//            onSearchClick = {},
//            onFeatureClick = {},
//            onClaimVoucherClick = {},
//            onNavigateToServiceType = {},
//            onNavigateToLocation = {},
//            onNavigateToCart = {},
//            onNavigateToVoucher = {},
//            onNavigateToTopUp = {},
//            onNavigateToProfile = {},
//            onNavigateToHistory = {},
//            vouchers =listOf(
//                Voucher(
//                    voucher_code = "DISKON10",
//                    discount_type = "fixed",
//                    discount_value = 10000,
//                    is_active = true,
//                    valid_from = null,
//                    valid_until = null,
//                    documentId = "voucher_001"
//                )
//            )
//        )
//    }
//}
//
//@Preview(showBackground = true, name = "Dashboard Error State")
//@Composable
//fun DashboardScreenErrorPreview() {
//    LaundryGoTheme {
//        // Preview untuk kondisi saat ada error
//        DashboardScreenContent(
//            user = null,
//            userName = "User",
//            userBalance = "0",
//            error = "Gagal memuat data pengguna. Silakan coba lagi.",
//            onTopUpClick = {},
//            onSearchClick = {},
//            onFeatureClick = {},
//            onClaimVoucherClick = {},
//            onNavigateToServiceType = {},
//            onNavigateToLocation = {},
//            onNavigateToCart = {},
//            onNavigateToVoucher = {},
//            onNavigateToTopUp = {},
//            onNavigateToProfile = {},
//            onNavigateToHistory = {},
//            vouchers =listOf(
//                Voucher(
//                    voucher_code = "DISKON10",
//                    discount_type = "fixed",
//                    discount_value = 10000,
//                    is_active = true,
//                    valid_from = null,
//                    valid_until = null,
//                    documentId = "voucher_001"
//                )
//            )
//        )
//    }
//}