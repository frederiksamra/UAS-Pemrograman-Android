package com.android.laundrygo.repository

import android.util.Log
import com.android.laundrygo.model.CartItem
import com.android.laundrygo.model.LaundryService
import com.android.laundrygo.model.Transaction
import com.android.laundrygo.model.User
import com.android.laundrygo.model.Voucher
import com.android.laundrygo.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

// --- INTERFACE (Kontrak Lengkap) ---
interface ServiceRepository {
    // Layanan
    suspend fun getServices(category: String): Result<List<LaundryService>>
    // Keranjang
    fun getCartItems(userId: String): Flow<Result<List<CartItem>>>
    fun addItemToCart(userId: String, service: LaundryService): Flow<Result<Unit>>
    fun removeItemFromCart(userId: String, itemId: String): Flow<Result<Unit>>
    fun updateItemQuantity(userId: String, itemId: String, change: Int): Flow<Result<Unit>>
    suspend fun clearCart(userId: String): Result<Unit>
    // Transaksi
    suspend fun createTransaction(transaction: Transaction): Result<String>
    suspend fun getTransactionById(transactionId: String): Flow<Result<Transaction?>>
    suspend fun updateTransactionStatus(transactionId: String, newStatus: String): Result<Unit>
    suspend fun updateTransactionPaymentMethod(transactionId: String, paymentMethod: String): Result<Unit>
    suspend fun updateTransactionVoucherId(transactionId: String, voucherId: String): Result<Unit>
    suspend fun getVoucherById(voucherId: String): Flow<Result<Voucher?>> // Add this line
    // User & Pembayaran
    fun getCurrentUserProfile(): Flow<Result<User?>>
    suspend fun processBalancePayment(userId: String, amountToDeduct: Double): Result<Unit>
    // --- FUNGSI BARU YANG DIBUTUHKAN ---
    fun getTransactionsForUser(userId: String): Flow<Result<List<Transaction>>>
    suspend fun deleteTransaction(transactionId: String): Result<Unit>
}

// --- IMPLEMENTASI ---
class ServiceRepositoryImpl : ServiceRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val TAG = "ServiceRepositoryImpl"

    override suspend fun getServices(category: String): Result<List<LaundryService>> {
        return try {
            val snapshot = firestore.collection("services")
                .whereEqualTo("category", category)
                .get()
                .await()
            val services = snapshot.toObjects(LaundryService::class.java)
            Log.d(TAG, "Successfully fetched ${services.size} services for category '$category'.")
            Result.success(services)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching services for category '$category'", e)
            Result.failure(e)
        }
    }

    override fun getCartItems(userId: String): Flow<Result<List<CartItem>>> = callbackFlow {
        if (userId.isEmpty()) {
            trySend(Result.failure(IllegalArgumentException("User ID tidak boleh kosong.")))
            return@callbackFlow
        }
        val listener = firestore.collection("carts").document(userId).collection("items")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error)); return@addSnapshotListener
                }
                if (snapshot != null) {
                    val items = snapshot.toObjects(CartItem::class.java)
                    trySend(Result.success(items))
                }
            }
        awaitClose { listener.remove() }
    }

    override fun addItemToCart(userId: String, service: LaundryService): Flow<Result<Unit>> = flow {
        try {
            val itemRef = firestore.collection("carts").document(userId).collection("items").document(service.id)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(itemRef)
                if (snapshot.exists()) {
                    transaction.update(itemRef, "quantity", FieldValue.increment(1))
                } else {
                    val newItem = CartItem(
                        id = service.id,
                        name = service.title,
                        description = service.description,
                        price = service.price, // <-- PERBAIKAN: Tidak perlu .toDouble() lagi
                        quantity = 1,
                        unit = service.unit,
                        category = service.category
                    )
                    transaction.set(itemRef, newItem)
                }
                null
            }.await()
            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun removeItemFromCart(userId: String, itemId: String): Flow<Result<Unit>> = flow {
        try {
            firestore.collection("carts").document(userId).collection("items").document(itemId).delete().await()
            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun updateItemQuantity(userId: String, itemId: String, change: Int): Flow<Result<Unit>> = flow {
        try {
            val itemRef = firestore.collection("carts").document(userId).collection("items").document(itemId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(itemRef)
                val currentQuantity = snapshot.getLong("quantity") ?: 0L
                val newQuantity = currentQuantity + change
                if (newQuantity > 0) {
                    transaction.update(itemRef, "quantity", newQuantity)
                } else {
                    transaction.delete(itemRef)
                }
                null
            }.await()
            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override suspend fun createTransaction(transaction: com.android.laundrygo.model.Transaction): Result<String> {
        return try {
            val documentRef = firestore.collection("transactions").add(transaction).await()
            Log.d(TAG, "Successfully created transaction with ID: ${documentRef.id}")
            Result.success(documentRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating transaction", e)
            Result.failure(e)
        }
    }

    override suspend fun clearCart(userId: String): Result<Unit> {
        return try {
            val cartItemsRef = firestore.collection("carts").document(userId).collection("items")
            val snapshot = cartItemsRef.get().await()

            // Hapus semua dokumen di dalam sub-koleksi 'items' satu per satu
            val batch = firestore.batch()
            for (document in snapshot.documents) {
                batch.delete(document.reference)
            }
            batch.commit().await()

            Log.d(TAG, "Successfully cleared cart for user: $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing cart for user: $userId", e)
            Result.failure(e)
        }
    }

    override suspend fun getTransactionById(transactionId: String): Flow<Result<Transaction?>> = flow { // Return a Flow
        try {
            val snapshot = firestore.collection("transactions").document(transactionId).get().await()
            val transaction = snapshot.toObject(Transaction::class.java)
            emit(Result.success(transaction))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override suspend fun getVoucherById(voucherId: String): Flow<Result<Voucher?>> = flow { // Implement this function
        try {
            val snapshot = firestore.collection("vouchers").document(voucherId).get().await()
            val voucher = snapshot.toObject(Voucher::class.java)
            emit(Result.success(voucher))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override suspend fun updateTransactionStatus(transactionId: String, newStatus: String): Result<Unit> {
        return try {
            firestore.collection("transactions").document(transactionId)
                .update("status", newStatus).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTransactionPaymentMethod(transactionId: String, paymentMethod: String): Result<Unit> {
        return try {
            firestore.collection("transactions").document(transactionId)
                .update("paymentMethod", paymentMethod).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTransactionVoucherId(transactionId: String, voucherId: String): Result<Unit> {
        return try {
            firestore.collection("transactions").document(transactionId)
                .update("voucherId", voucherId).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCurrentUserProfile(): Flow<Result<User?>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId.isNullOrEmpty()) {
            trySend(Result.failure(Exception("User tidak login.")))
            return@callbackFlow
        }
        val listener = firestore.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error)); return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    trySend(Result.success(snapshot.toObject<User>()))
                } else {
                    trySend(Result.success(null))
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun processBalancePayment(userId: String, amountToDeduct: Double): Result<Unit> {
        return try {
            val userRef = firestore.collection("users").document(userId)
            firestore.runTransaction { transaction ->
                val userSnapshot = transaction.get(userRef)
                val currentBalance = userSnapshot.getDouble("balance") ?: 0.0

                if (currentBalance < amountToDeduct) {
                    throw Exception("Saldo tidak mencukupi.") // Ini akan ditangkap oleh blok catch
                }

                val newBalance = currentBalance - amountToDeduct
                transaction.update(userRef, "balance", newBalance)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteTransaction(transactionId: String): Result<Unit> {
        return try {
            firestore.collection("transactions").document(transactionId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getTransactionsForUser(userId: String): Flow<Result<List<Transaction>>> = callbackFlow {
        val listener = firestore.collection("transactions")
            .whereEqualTo("userId", userId) // Filter berdasarkan ID pengguna
            .orderBy("createdAt", Query.Direction.DESCENDING) // Urutkan dari yang terbaru
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    // Gunakan toObjects karena Transaction model sudah siap
                    val transactions = snapshot.toObjects(Transaction::class.java)
                    trySend(Result.success(transactions))
                } else {
                    // Kirim list kosong jika tidak ada data
                    trySend(Result.success(emptyList()))
                }
            }

        // Hapus listener saat tidak digunakan lagi
        awaitClose { listener.remove() }
    }
}
