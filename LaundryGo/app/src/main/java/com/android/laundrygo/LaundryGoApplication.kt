package com.android.laundrygo

import android.app.Application
import com.google.firebase.FirebaseApp

// Kelas ini akan menjadi titik masuk utama aplikasi Anda
class LaundryGoApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Inisialisasi Firebase di tempat yang paling tepat dan aman
        FirebaseApp.initializeApp(this)
    }
}