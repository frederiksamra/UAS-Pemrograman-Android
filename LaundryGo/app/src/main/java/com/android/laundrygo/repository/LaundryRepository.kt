package com.android.laundrygo.repository
import com.android.laundrygo.model.LaundryLocation

class LaundryRepository {
    // Fungsi ini mensimulasikan data dari API
    fun getLaundryLocations(): List<LaundryLocation> {
        return listOf(
            LaundryLocation(
                id = "1",
                name = "LaundryGo",
                latitude = -7.7828,
                longitude = 110.3671,
                address = "Jl. Kaliurang KM 5",
                city = "Yogyakarta",
                state = "DI Yogyakarta",
                zipCode = "55281",
                country = "Indonesia"
            ),
            LaundryLocation(
                id = "2",
                name = "LaundryGo",
                latitude = -7.8014,
                longitude = 110.3647,
                address = "Jl. Malioboro No. 15",
                city = "Yogyakarta",
                state = "DI Yogyakarta",
                zipCode = "55213",
                country = "Indonesia"
            ),
            LaundryLocation(
                id = "3",
                name = "LaundryGo",
                latitude = -7.7783,
                longitude = 110.4155,
                address = "Jl. Gejayan No. 22",
                city = "Yogyakarta",
                state = "DI Yogyakarta",
                zipCode = "55281",
                country = "Indonesia"
            ),
            LaundryLocation(
                id = "4",
                name = "LaundryGo",
                latitude = -7.7682,
                longitude = 110.3774,
                address = "Jl. Seturan Raya No. 8",
                city = "Yogyakarta",
                state = "DI Yogyakarta",
                zipCode = "55281",
                country = "Indonesia"
            ),
            LaundryLocation(
                id = "5",
                name = "LaundryGo",
                latitude = -7.8149,
                longitude = 110.3515,
                address = "Jl. Parangtritis KM 3",
                city = "Yogyakarta",
                state = "DI Yogyakarta",
                zipCode = "55143",
                country = "Indonesia"
            ),
            LaundryLocation(
                id = "6",
                name = "LaundryGo",
                latitude = -7.7844,
                longitude = 110.4086,
                address = "Jl. Babarsari No. 10",
                city = "Yogyakarta",
                state = "DI Yogyakarta",
                zipCode = "55281",
                country = "Indonesia"
            ),
            LaundryLocation(
                id = "7",
                name = "LaundryGo",
                latitude = -7.8025,
                longitude = 110.4192,
                address = "Jl. Laksda Adisucipto No. 90",
                city = "Yogyakarta",
                state = "DI Yogyakarta",
                zipCode = "55281",
                country = "Indonesia"
            ),
            LaundryLocation(
                id = "8",
                name = "LaundryGo",
                latitude = -7.7646,
                longitude = 110.3615,
                address = "Jl. Affandi No. 25",
                city = "Yogyakarta",
                state = "DI Yogyakarta",
                zipCode = "55283",
                country = "Indonesia"
            )
        )
    }

}