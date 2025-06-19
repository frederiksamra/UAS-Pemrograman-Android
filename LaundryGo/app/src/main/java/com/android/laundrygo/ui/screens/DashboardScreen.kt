

package com.android.laundrygo.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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

// --- PERBAIKAN UTAMA: FUNGSI INTI ---
@Composable
fun DashboardScreen(
    // 1. Terima ViewModel dari luar (NavGraph)
    viewModel: DashboardViewModel,

    // 2. Navigasi tetap sebagai parameter
    onNavigateToServiceType: () -> Unit,
    onNavigateToLocation: () -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToVoucher: () -> Unit,
    onNavigateToTopUp: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToInProcess: () -> Unit
) {
    // Ambil semua state dari ViewModel yang sudah diberikan
    val user by viewModel.user.observeAsState(null)
    val userName by viewModel.userName.observeAsState("User")
    val userBalance by viewModel.userBalance.observeAsState("Rp 0")
    val error by viewModel.error.observeAsState()
    val vouchers by viewModel.vouchers.observeAsState(emptyList())

    // Panggil konten UI dengan state yang sudah didapat
    DashboardScreenContent(
        user = user,
        userName = userName,
        userBalance = userBalance,
        error = error,
        vouchers = vouchers,
        onTopUpClick = onNavigateToTopUp,
        onSearchClick = { /* TODO: Implement search */ },
        onFeatureClick = { featureId ->
            when (featureId) {
                "service_type" -> onNavigateToServiceType()
                "nearest_location" -> onNavigateToLocation()
                "cart" -> onNavigateToCart()
                "voucher" -> onNavigateToVoucher()
                "top_up" -> onNavigateToTopUp()
                "history" -> onNavigateToHistory()
                "in_process" -> onNavigateToInProcess() // Tambahkan handler untuk "In Process"
            }
        },
        onClaimVoucherClick = { voucherId -> viewModel.claimVoucher(voucherId) },
        onNavigateToProfile = onNavigateToProfile
    )
}

// Composable ini bersifat 'stateless' dan hanya menampilkan UI
@Composable
private fun DashboardScreenContent(
    user: User?,
    userName: String,
    userBalance: String,
    error: String?,
    vouchers: List<Voucher>,
    onTopUpClick: () -> Unit,
    onSearchClick: () -> Unit,
    onFeatureClick: (String) -> Unit,
    onClaimVoucherClick: (String) -> Unit,
    onNavigateToProfile: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        HeaderSection(
            user = user,
            userName = userName,
            userBalance = userBalance,
            onSearchClick = onSearchClick,
            onTopUpClick = onTopUpClick,
            onNavigateToProfile = onNavigateToProfile
        )

        error?.let {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        FeaturesSection(onFeatureClick = onFeatureClick)

        PromoSection(vouchers = vouchers, onClaimVoucherClick = onClaimVoucherClick)
    }
}


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
        // Latar belakang biru di bagian atas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(165.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                )
        )

        // Konten utama header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Baris untuk Search Bar dan Foto Profil
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

                // --- PERUBAHAN UTAMA DI SINI ---
                // Mengganti Icon statis dengan komponen InitialsProfilePicture
                Box(modifier = Modifier.clickable { onNavigateToProfile() }) {
                    InitialsProfilePicture(
                        name = user?.name ?: "", // Mengambil nama dari objek User
                        size = 62.dp,
                        textStyle = MaterialTheme.typography.headlineSmall.copy(color = Color.White)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Kartu untuk "Hello, User" dan Saldo
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
                        // Menampilkan saldo yang sudah diformat dari ViewModel
                        Text(
                            text = "Rp $userBalance",
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
fun PromoSection(
    vouchers: List<Voucher>,
    onClaimVoucherClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(16.dp)) {
        Text(
            text = "Promo",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (vouchers.isEmpty()) {
            Text(
                "Tidak ada voucher tersedia saat ini.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                vouchers.forEach { voucher ->
                    M3VoucherCard(
                        voucher = voucher,
                        onUseClick = { onClaimVoucherClick(voucher.documentId) }
                    )
                }
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