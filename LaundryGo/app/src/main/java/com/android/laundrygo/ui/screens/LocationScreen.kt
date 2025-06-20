package com.android.laundrygo.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.laundrygo.R
import com.android.laundrygo.model.LaundryLocation
import com.android.laundrygo.ui.theme.DarkBlue
import com.android.laundrygo.viewmodel.LocationViewModel
import com.android.laundrygo.viewmodel.LocationViewModelFactory
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationScreen(
    onBack: () -> Unit = {},
    viewModel: LocationViewModel = viewModel(factory = LocationViewModelFactory(LocalContext.current))
) {
    // State dan permission launcher, sudah benar
    val userLocation by viewModel.userLocation.observeAsState()
    val laundryLocations by viewModel.laundryLocations.observeAsState(emptyList())
    var selectedLaundry by remember { mutableStateOf<LaundryLocation?>(null) }

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
            Box(modifier = Modifier
                .weight(1f)
                .fillMaxWidth()) {

                // Jika lokasi user sudah didapatkan
                if (permissionGranted && userLocation != null) {
                    // Gunakan komponen MapView
                    LaundryMapView(
                        modifier = Modifier.fillMaxSize(),
                        userLocation = userLocation,
                        laundryLocations = laundryLocations,
                        onLaundrySelected = { laundry ->
                            selectedLaundry = laundry
                        }
                    )
                } else {
                    // Tampilkan pesan loading atau permintaan izin
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(if (!permissionGranted) "Meminta izin lokasi..." else "Mengambil lokasi Anda...")
                    }
                }
            }

            // Detail card
            selectedLaundry?.let { laundry ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(8.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkBlue)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(laundry.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(laundry.address)
                            Text("${laundry.city}, ${laundry.state} ${laundry.zipCode}")
                        }
                        Image(
                            painter = painterResource(id = R.drawable.laundry_image),
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

// Composable terpisah untuk MapView
@Composable
fun LaundryMapView(
    modifier: Modifier = Modifier,
    userLocation: android.location.Location?,
    laundryLocations: List<LaundryLocation>,
    onLaundrySelected: (LaundryLocation) -> Unit
) {
    val context = LocalContext.current
    val mapView = rememberMapViewWithLifecycle()

    // Ingat posisi tengah peta agar tidak terus-menerus di-reset saat user menggeser peta
    val isMapInitialized = remember { mutableStateOf(false) }

    AndroidView(
        modifier = modifier,
        factory = {
            // Blok ini hanya dijalankan sekali
            mapView.apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
            }
        },
        update = { view ->
            // Blok ini dijalankan setiap kali state (lokasi) berubah

            // Atur zoom & posisi tengah HANYA saat pertama kali lokasi user didapat
            if (userLocation != null && !isMapInitialized.value) {
                view.controller.setZoom(15.5)
                view.controller.setCenter(GeoPoint(userLocation.latitude, userLocation.longitude))
                isMapInitialized.value = true
            }

            // Hapus semua marker lama sebelum menambahkan yang baru
            view.overlays.clear()

            // Tambahkan marker user
            userLocation?.let {
                val userMarker = Marker(view).apply {
                    position = GeoPoint(it.latitude, it.longitude)
                    icon = ContextCompat.getDrawable(context, R.drawable.ic_my_location)
                    title = "Lokasi Anda"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }
                view.overlays.add(userMarker)
            }

            // Tambahkan marker untuk setiap laundry
            laundryLocations.forEach { laundry ->
                val laundryMarker = Marker(view).apply {
                    position = GeoPoint(laundry.latitude, laundry.longitude)
                    icon = ContextCompat.getDrawable(context, R.drawable.ic_laundry_location)
                    title = laundry.name
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    setOnMarkerClickListener { _, _ ->
                        onLaundrySelected(laundry)
                        true
                    }
                }
                view.overlays.add(laundryMarker)
            }

            // PENTING: Refresh peta untuk menampilkan semua perubahan
            view.invalidate()
        }
    )
}

// Helper untuk membuat MapView dan mengelola lifecycle-nya secara otomatis
@Composable
fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember { MapView(context) } // 'remember' agar tidak dibuat ulang terus-menerus

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> {}
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            // Optional: Hapus MapView dari parent saat Composable dihancurkan
            // untuk menghindari memory leak di beberapa kasus
            // (mapView.parent as? ViewGroup)?.removeView(mapView)
            // mapView.onDetach()
        }
    }
    return mapView
}


@Preview(showBackground = true)
@Composable
fun LocationScreenPreview() {

}