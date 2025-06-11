package com.android.laundrygo.ui.screens
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.android.laundrygo.R
import com.android.laundrygo.model.LaundryLocation
import com.android.laundrygo.navigation.Screen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.layout.ContentScale
import android.location.Location
import androidx.compose.foundation.Image
import androidx.compose.ui.platform.LocalInspectionMode
import com.android.laundrygo.ui.theme.LaundryGoTheme
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
import com.android.laundrygo.repository.LaundryRepository
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.laundrygo.viewmodel.LocationViewModel
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationScreen(
    onBack: () -> Unit = {},
    viewModel: LocationViewModel = viewModel()
) {
    val userLocation by viewModel.userLocation.observeAsState()
    val laundryLocations by viewModel.laundryLocations.observeAsState(emptyList())
    val nearestLaundry = viewModel.getNearestLaundry()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nearest LaundryGo") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(com.android.laundrygo.R.drawable.ic_black), contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF002480)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5DC))
                .padding(padding)
        ) {
            if (!LocalInspectionMode.current) {
                AndroidView(
                    factory = { context ->
                        MapView(context).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)
                            // Tambahkan marker lokasi user dan laundry jika perlu
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
            } else {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text("[MapView Preview]", color = Color.DarkGray)
                }
            }

            // Legend
            Column(
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .background(Color(0x80FFFFFF))
                        .padding(8.dp)
                ) {
                    Icon(painterResource(com.android.laundrygo.R.drawable.ic_my_location), contentDescription = null, tint = Color.Blue)
                    Spacer(Modifier.width(8.dp))
                    Text("Your Location", color = Color.Blue)
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .background(Color(0x80FFFFFF))
                        .padding(8.dp)
                ) {
                    Icon(painterResource(com.android.laundrygo.R.drawable.ic_laundry_location), contentDescription = null, tint = Color.Red)
                    Spacer(Modifier.width(8.dp))
                    Text("LaundryGo Location", color = Color.Red)
                }
            }

            // Detail Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 12.dp
                )
            ) {
                Row(
                    modifier = Modifier
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = nearestLaundry?.name ?: "-",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = nearestLaundry?.address ?: "-",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = nearestLaundry?.let { "${it.city}, ${it.state} ${it.zipCode}" } ?: "-",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = nearestLaundry?.country ?: "-",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(Modifier.width(20.dp))
                    Image(
                        painter = painterResource(com.android.laundrygo.R.drawable.laundry_image),
                        contentDescription = null,
                        modifier = Modifier.size(100.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LocationScreenPreview() {
    val dummyLaundry = LaundryRepository().getLaundryLocations().first()
    val dummyLocation = Location("").apply {
        latitude = dummyLaundry.latitude
        longitude = dummyLaundry.longitude
    }
    LocationScreen(onBack = {})
}

@Composable
fun LocationScreenWithPermission(
    locationPermissionGranted: Boolean,
    onRequestPermission: () -> Unit
) {
    if (locationPermissionGranted) {
        // Tampilkan halaman lokasi (akses lokasi user, dsb)
        LocationScreen()
    } else {
        // Tampilkan UI meminta permission
        PermissionRequestScreen(onRequestPermission)
    }
}

@Composable
fun PermissionRequestScreen(onRequestPermission: () -> Unit) {
    Scaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Aplikasi membutuhkan akses lokasi untuk menampilkan laundry terdekat.")
            Spacer(Modifier.height(16.dp))
            Button(onClick = onRequestPermission) {
                Text("Izinkan Lokasi")
            }
        }
    }
}


