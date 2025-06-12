package com.android.laundrygo.data.repository

import com.android.laundrygo.data.model.OrderItem
import kotlinx.coroutines.delay

// Interface mendefinisikan "kontrak" apa yang bisa dilakukan repository
interface PaymentRepository {
    suspend fun getOrderDetails(): List<OrderItem>
}

// Implementasi konkret dari interface. Di sini kita gunakan data palsu.
// Di aplikasi nyata, ini bisa mengambil data dari Retrofit (API) atau Room (Database).
class FakePaymentRepositoryImpl : PaymentRepository {
    override suspend fun getOrderDetails(): List<OrderItem> {
        // Simulasi pengambilan data dari jaringan
        delay(500)
        return listOf(
            OrderItem("Fast Cleaning Shoes", 20000, "1", 20000),
            OrderItem("Iron only", 9000, "1,2Kg", 10000)
        )
    }
}