package com.android.laundrygo.model

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.*

class LocationService(private val context: Context) {
    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(callback: (Location?) -> Unit) {
        val req = LocationRequest.create().apply {
            priority = Priority.PRIORITY_HIGH_ACCURACY
            interval = 5000
        }
        fusedClient.requestLocationUpdates(req, object : LocationCallback() {
            override fun onLocationResult(res: LocationResult) {
                callback(res.lastLocation)
                fusedClient.removeLocationUpdates(this)
            }
        }, Looper.getMainLooper())
    }
}
