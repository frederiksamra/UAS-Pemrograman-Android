package com.android.laundrygo.repository
import com.android.laundrygo.model.LaundryLocation

class LaundryRepository {
    // Fungsi ini mensimulasikan data dari API
    fun getLaundryLocations(): List<LaundryLocation> {
        return listOf(
            LaundryLocation(
                id = "1",
                name = "LaundryGo",
                latitude = -7.752539068536223,
                longitude = 110.38432203045912,
                address = "Jl. Kaliurang KM 7 No.24",
                city = "Yogyakarta",
                state = "DI Yogyakarta",
                zipCode = "55283",
                country = "Indonesia"
            ),
            LaundryLocation(
                id = "2",
                name = "LaundryGo",
                latitude = -7.794095203836681,
                longitude = 110.36521665266386,
                address = "Jl. Malioboro No. 80",
                city = "Yogyakarta",
                state = "DI Yogyakarta",
                zipCode = "55271",
                country = "Indonesia"
            ),
            LaundryLocation(
                id = "3",
                name = "LaundryGo",
                latitude = -7.769927757342413,
                longitude = 110.39004495815378,
                address = "Jl. Gejayan No. 22",
                city = "Yogyakarta",
                state = "DI Yogyakarta",
                zipCode = "55281",
                country = "Indonesia"
            ),
            LaundryLocation(
                id = "4",
                name = "LaundryGo",
                latitude = -7.768773686366266,
                longitude = 110.40984200346078,
                address = "Jl. Seturan Raya No. 8",
                city = "Yogyakarta",
                state = "DI Yogyakarta",
                zipCode = "55281",
                country = "Indonesia"
            ),
            LaundryLocation(
                id = "5",
                name = "LaundryGo",
                latitude = -7.793152505498934,
                longitude = 110.40122394455625,
                address = "Jl. Pura Jl. Sorowajan No.194 C",
                city = "Yogyakarta",
                state = "DI Yogyakarta",
                zipCode = "55143",
                country = "Indonesia"
            ),
            LaundryLocation(
                id = "6",
                name = "LaundryGo",
                latitude = -7.781735034583433,
                longitude = 110.41427029057644,
                address = "Jl. Babarsari No. 10",
                city = "Yogyakarta",
                state = "DI Yogyakarta",
                zipCode = "55281",
                country = "Indonesia"
            ),
            LaundryLocation(
                id = "7",
                name = "LaundryGo",
                latitude = -7.782567856145689,
                longitude = 110.39636953659837,
                address = "Jl. Laksda Adisucipto No. 90",
                city = "Yogyakarta",
                state = "DI Yogyakarta",
                zipCode = "55281",
                country = "Indonesia"
            ),
            LaundryLocation(
                id = "8",
                name = "LaundryGo",
                latitude = -7.785157067514852,
                longitude = 110.37939376093905,
                address = "Jl. Klitren Lor GK 3 No.418",
                city = "Yogyakarta",
                state = "DI Yogyakarta",
                zipCode = "55222",
                country = "Indonesia"
            )
        )
    }

}