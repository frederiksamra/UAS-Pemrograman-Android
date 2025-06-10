package com.android.laundrygo.repository
import com.android.laundrygo.model.LaundryLocation

class LaundryRepository {
    // Fungsi ini mensimulasikan data dari API
    fun getLaundryLocations(): List<LaundryLocation> {
        return listOf(
            LaundryLocation(
                id = "1",
                name = "LaundryGo",
                latitude = -6.2088,
                longitude = 106.8456,
                address = "123 Elm Street",
                city = "Springfield",
                state = "IL",
                zipCode = "62701",
                country = "United States"
            ),
            LaundryLocation(
                id = "2",
                name = "LaundryGo",
                latitude = -6.2065,
                longitude = 106.8235,
                address = "456 Oak Avenue",
                city = "Springfield",
                state = "IL",
                zipCode = "62702",
                country = "United States"
            ),
            LaundryLocation(
                id = "3",
                name = "LaundryGo",
                latitude = -6.2100,
                longitude = 106.8350,
                address = "789 Maple Drive",
                city = "Springfield",
                state = "IL",
                zipCode = "62703",
                country = "United States"
            ),
            LaundryLocation(
                id = "4",
                name = "LaundryGo",
                latitude = -6.2000,
                longitude = 106.8300,
                address = "101 Pine Street",
                city = "Springfield",
                state = "IL",
                zipCode = "62704",
                country = "United States"
            ),
            LaundryLocation(
                id = "5",
                name = "LaundryGo",
                latitude = -6.2150,
                longitude = 106.8400,
                address = "202 Cedar Road",
                city = "Springfield",
                state = "IL",
                zipCode = "62705",
                country = "United States"
            ),
            LaundryLocation(
                id = "6",
                name = "LaundryGo",
                latitude = -6.2200,
                longitude = 106.8250,
                address = "303 Birch Lane",
                city = "Springfield",
                state = "IL",
                zipCode = "62706",
                country = "United States"
            ),
            LaundryLocation(
                id = "7",
                name = "LaundryGo",
                latitude = -6.2050,
                longitude = 106.8150,
                address = "404 Willow Way",
                city = "Springfield",
                state = "IL",
                zipCode = "62707",
                country = "United States"
            ),
            LaundryLocation(
                id = "8",
                name = "LaundryGo",
                latitude = -6.2175,
                longitude = 106.8475,
                address = "505 Aspen Court",
                city = "Springfield",
                state = "IL",
                zipCode = "62708",
                country = "United States"
            )
        )
    }
}