package com.android.laundrygo

import android.app.Application
import com.google.firebase.FirebaseApp
// 1. TAMBAHKAN IMPORT INI
import org.osmdroid.config.Configuration

class LaundryGoApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Inisialisasi Firebase
        FirebaseApp.initializeApp(this)

        // 2. TAMBAHKAN KONFIGURASI OSMDROID (SANGAT PENTING)
        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = "com.android.laundrygo" // Gunakan nama paket aplikasi Anda
    }
}