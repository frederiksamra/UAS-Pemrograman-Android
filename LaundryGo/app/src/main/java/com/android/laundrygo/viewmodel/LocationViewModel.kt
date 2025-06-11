package com.android.laundrygo.viewmodel

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.laundrygo.model.LaundryLocation
import com.android.laundrygo.repository.LaundryRepository

class LocationViewModel : ViewModel() {
    private val repository = LaundryRepository()

    private val _laundryLocations = MutableLiveData<List<LaundryLocation>>()
    val laundryLocations: LiveData<List<LaundryLocation>> get() = _laundryLocations

    private val _selectedLaundry = MutableLiveData<LaundryLocation>()
    val selectedLaundry: LiveData<LaundryLocation> get() = _selectedLaundry

    private val _userLocation = MutableLiveData<Location>()
    val userLocation: LiveData<Location> get() = _userLocation

    init {
        fetchLaundryLocations()
    }

    private fun fetchLaundryLocations() {
        val locations = repository.getLaundryLocations()
        _laundryLocations.value = locations
    }

    fun setUserLocation(location: Location) {
        _userLocation.value = location
    }

    fun selectLaundry(laundry: LaundryLocation) {
        _selectedLaundry.value = laundry
    }

    fun getNearestLaundry(): LaundryLocation? {
        val userLoc = _userLocation.value ?: return null
        val laundries = _laundryLocations.value ?: return null

        return laundries.minByOrNull { laundry ->
            val laundryLoc = Location("").apply {
                latitude = laundry.latitude
                longitude = laundry.longitude
            }
            userLoc.distanceTo(laundryLoc)
        }
    }
}