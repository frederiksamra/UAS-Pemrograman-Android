package com.android.laundrygo.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.lifecycle.*
import com.android.laundrygo.model.LocationService
import com.android.laundrygo.model.LaundryLocation
import com.android.laundrygo.repository.LaundryRepository

@SuppressLint("StaticFieldLeak")
class LocationViewModel(private val context: Context) : ViewModel() {
    private val repo = LaundryRepository()
    private val service = LocationService(context)

    private val _userLocation = MutableLiveData<Location?>()
    val userLocation: LiveData<Location?> = _userLocation

    val laundryLocations: LiveData<List<LaundryLocation>> = liveData {
        emit(repo.getLaundryLocations())
    }

    fun fetchUserLocation() {
        service.getCurrentLocation { loc ->
            _userLocation.postValue(loc)
        }
    }
}

class LocationViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LocationViewModel::class.java)) {
            return LocationViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}