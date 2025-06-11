package com.android.laundrygo.ui.screens

import android.Manifest
import android.view.LayoutInflater
import androidx.compose.runtime.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.laundrygo.R
import com.android.laundrygo.model.LaundryLocation
import com.android.laundrygo.viewmodel.LocationViewModel
import com.android.laundrygo.viewmodel.LocationViewModelFactory
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.android.laundrygo.ui.theme.DarkBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationScreen(
    onBack: () -> Unit = {},
    viewModel: LocationViewModel = viewModel(factory = LocationViewModelFactory(LocalContext.current))
) {
    val context = LocalContext.current
    val userLocation by viewModel.userLocation.observeAsState()
    val laundryLocations by viewModel.laundryLocations.observeAsState(emptyList())
    var selectedLaundry by remember { mutableStateOf<LaundryLocation?>(null) }

    // Permission request launcher
    var permissionGranted by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        permissionGranted = granted
        if (granted) viewModel.fetchUserLocation()
    }

    LaunchedEffect(Unit) {
        launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nearest LaundryGo") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_black), contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBlue)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            if (permissionGranted && userLocation != null) {
                AndroidView(
                    factory = { inflaterContext ->
                        LayoutInflater.from(inflaterContext).inflate(R.layout.map_layout, null).apply {
                            val map = findViewById<MapView>(R.id.mapView)
                            map.setTileSource(TileSourceFactory.MAPNIK)
                            map.setMultiTouchControls(true)
                            map.controller.setZoom(15.0)

                            val uLoc = userLocation!!
                            map.controller.setCenter(GeoPoint(uLoc.latitude, uLoc.longitude))

                            map.overlays.clear()
                            // Marker user
                            map.overlays.add(Marker(map).apply {
                                position = GeoPoint(uLoc.latitude, uLoc.longitude)
                                icon = ContextCompat.getDrawable(context, R.drawable.ic_my_location)
                                title = "Lokasi Anda"
                            })

                            laundryLocations.forEach { laundry ->
                                map.overlays.add(Marker(map).apply {
                                    position = GeoPoint(laundry.latitude, laundry.longitude)
                                    icon = ContextCompat.getDrawable(context, R.drawable.ic_laundry_location)
                                    title = laundry.name
                                    setOnMarkerClickListener { _, _ ->
                                        selectedLaundry = laundry
                                        true
                                    }
                                })
                            }

                            map.invalidate()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
            } else {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(if (!permissionGranted) "Meminta izin lokasi..." else "Mengambil lokasi...")
                }
            }

            // Detail card
            selectedLaundry?.let { laundry ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(laundry.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(laundry.address)
                            Text("${laundry.city}, ${laundry.state} ${laundry.zipCode}")
                            Text(laundry.country)
                        }
                        Image(
                            painter = painterResource(R.drawable.laundry_image),
                            contentDescription = null,
                            modifier = Modifier.size(72.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LocationScreenPreview() {
    LocationScreen()
}
