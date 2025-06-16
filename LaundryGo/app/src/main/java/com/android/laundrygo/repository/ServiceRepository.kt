package com.android.laundrygo.repository

import com.android.laundrygo.model.LaundryService
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

interface ServiceRepository {
    suspend fun getServices(category: String): Result<List<LaundryService>>
}

class ServiceRepositoryImpl : ServiceRepository {

    private val firestore = FirebaseFirestore.getInstance()

    override suspend fun getServices(category: String): Result<List<LaundryService>> {
        return try {
            val snapshot = firestore.collection("services")
                .whereEqualTo("category", category)
                .get()
                .await()

            val services = snapshot.toObjects(LaundryService::class.java)
            Result.success(services)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}