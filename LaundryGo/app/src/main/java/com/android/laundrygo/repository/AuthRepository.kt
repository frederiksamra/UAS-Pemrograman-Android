package com.android.laundrygo.repository

import com.android.laundrygo.model.User
import com.android.laundrygo.model.Voucher
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

// --- INTERFACE (Kontrak Lengkap) ---
interface AuthRepository {
    suspend fun registerUser(email: String, password: String, userProfile: User): Result<Unit>
    suspend fun loginUser(email: String, password: String): Result<FirebaseUser>
    suspend fun loginWithGoogle(idToken: String): Result<FirebaseUser>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun getUserProfile(): Result<User?> // Diubah agar bisa null jika dokumen tidak ada
    fun getCurrentUserProfile(): Flow<Result<User?>>
    suspend fun updateUserProfile(user: User): Result<Unit>
    suspend fun performTopUp(amount: Double): Result<Unit>
    fun logout()

    // --- FUNGSI BARU UNTUK VOUCHER ---
    fun getMyVouchers(): Flow<Result<List<Voucher>>>
    suspend fun claimVoucher(voucher: Voucher): Result<Unit>

    // Fungsi baru yang menggabungkan semua proses pembayaran
    suspend fun processPayment(
        userId: String,
        transactionId: String,
        amountToDeduct: Double,
        voucherToUseId: String? // ID voucher yang digunakan, bisa null
    ): Result<Unit>

    fun getUserDocument(userId: String): Flow<Result<DocumentSnapshot>>
    fun getUserByUsername(username: String): Flow<Result<QuerySnapshot>> // Tambahkan ini
}

class AuthRepositoryImpl : AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val TAG = "AuthRepositoryImpl"

    override suspend fun registerUser(email: String, password: String, userProfile: User): Result<Unit> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw IllegalStateException("Firebase user not found.")
            val finalUserProfile = userProfile.copy(userId = firebaseUser.uid, createdAt = Timestamp.now())
            firestore.collection("users").document(firebaseUser.uid).set(finalUserProfile).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loginUser(email: String, password: String): Result<FirebaseUser> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(authResult.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Login dengan Google
    override suspend fun loginWithGoogle(idToken: String): Result<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user ?: throw IllegalStateException("Firebase user not found.")

            val userDocRef = firestore.collection("users").document(firebaseUser.uid)

            // Cek apakah dokumen user sudah ada
            if (!userDocRef.get().await().exists()) {
                val newUserProfile = User(
                    userId = firebaseUser.uid, // PERBAIKAN: Gunakan uid
                    name = firebaseUser.displayName ?: "Google User",
                    email = firebaseUser.email ?: "",
                    phone = firebaseUser.phoneNumber ?: "",
                    address = "",
                    username = firebaseUser.displayName?.replace(" ", "")?.lowercase() ?: "googleuser",
                    createdAt = Timestamp.now()
                )
                userDocRef.set(newUserProfile).await()
            }
            Result.success(firebaseUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Reset Password
    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Mendapatkan profil pengguna
    override suspend fun getUserProfile(): Result<User?> { // Diubah agar bisa null
        return try {
            val uid = auth.currentUser?.uid ?: return Result.success(null) // Kembalikan null jika user tidak login
            val document = firestore.collection("users").document(uid).get().await()
            Result.success(document.toObject<User>()) // Akan null jika dokumen tidak ada
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCurrentUserProfile(): Flow<Result<User?>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(Result.success(null))
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }

                val user = snapshot?.toObject(User::class.java)
                trySend(Result.success(user))
            }

        awaitClose { listener.remove() }
    }

    // Edit Profil User
    override suspend fun updateUserProfile(user: User): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: throw Exception("User not logged in.")
            // Gunakan SetOptions.merge() agar hanya field yang berubah yang diupdate
            firestore.collection("users").document(uid).set(user, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Fungsi untuk Top Up
    override suspend fun performTopUp(amount: Double): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: throw Exception("User not logged in.")
            if (amount <= 0) {
                throw IllegalArgumentException("Top up amount must be positive.")
            }
            // Dapatkan referensi dokumen user
            val userDocRef = firestore.collection("users").document(uid)

            // Gunakan FieldValue.increment untuk menambah saldo secara aman di server
            userDocRef.update("balance", FieldValue.increment(amount)).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Buat Logout
    override fun logout() {
        auth.signOut()
    }

    override fun getMyVouchers(): Flow<Result<List<Voucher>>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId.isNullOrEmpty()) {
            trySend(Result.failure(Exception("User tidak login."))); close(); return@callbackFlow
        }
        val listener = firestore.collection("users").document(userId).collection("my_vouchers")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error)); return@addSnapshotListener
                }
                if (snapshot != null) {
                    val vouchers = snapshot.toObjects(Voucher::class.java)
                    trySend(Result.success(vouchers))
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun claimVoucher(voucher: Voucher): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User tidak login.")
            val userVoucherRef = firestore.collection("users").document(userId)
                .collection("my_vouchers").document(voucher.voucherDocumentId)

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(userVoucherRef)
                if (snapshot.exists()) {
                    throw Exception("Voucher ini sudah pernah Anda klaim.")
                }

                // --- PERBAIKAN EKSPLISIT DI SINI ---
                // Buat objek data baru tanpa menyertakan 'documentId' dan 'is_active'
                val claimedVoucherData = mapOf(
                    "voucher_code" to voucher.voucher_code,
                    "discount_type" to voucher.discount_type,
                    "discount_value" to voucher.discount_value,
                    "valid_from" to voucher.valid_from,
                    "valid_until" to voucher.valid_until,
                    "is_used" to false // Status awal saat di-klaim
                )

                // Simpan data map ini, bukan seluruh objek voucher
                transaction.set(userVoucherRef, claimedVoucherData)

            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun processPayment(
        userId: String,
        transactionId: String,
        amountToDeduct: Double,
        voucherToUseId: String?
    ): Result<Unit> {
        return try {
            val userRef = firestore.collection("users").document(userId)
            val transactionRef = firestore.collection("transactions").document(transactionId)
            val voucherRef = voucherToUseId?.let {
                firestore.collection("users").document(userId).collection("my_vouchers").document(it)
            }

            firestore.runTransaction { transaction ->
                // 1. Ambil data saldo user saat ini
                val userSnapshot = transaction.get(userRef)
                val currentBalance = userSnapshot.getDouble("balance") ?: 0.0

                if (currentBalance < amountToDeduct) {
                    throw Exception("Saldo tidak mencukupi.")
                }

                // 2. Kurangi saldo user
                val newBalance = currentBalance - amountToDeduct
                transaction.update(userRef, "balance", newBalance)

                // 3. Update status transaksi menjadi "Lunas"
                transaction.update(transactionRef, "status", "Lunas")

                // 4. Jika ada voucher yang digunakan, tandai sebagai terpakai
                voucherRef?.let {
                    transaction.update(it, "is_used", true)
                }
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getUserDocument(userId: String): Flow<Result<DocumentSnapshot>> = flow {
        try {
            val snapshot = firestore.collection("users").document(userId).get().await()
            emit(Result.success(snapshot))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun getUserByUsername(username: String): Flow<Result<QuerySnapshot>> = flow {
        try {
            val querySnapshot = firestore.collection("users")
                .whereEqualTo("username", username)
                .get()
                .await()
            emit(Result.success(querySnapshot))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}