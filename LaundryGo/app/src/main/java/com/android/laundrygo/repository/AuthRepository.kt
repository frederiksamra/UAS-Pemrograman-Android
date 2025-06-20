package com.android.laundrygo.repository

import com.android.laundrygo.model.User
import com.android.laundrygo.model.Voucher // <-- Pastikan import ini ada
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

// --- INTERFACE (Kontrak Lengkap) ---
interface AuthRepository {
    suspend fun registerUser(email: String, password: String, userProfile: User): Result<Unit>
    suspend fun loginUser(email: String, password: String): Result<Unit>
    suspend fun loginWithGoogle(idToken: String): Result<Unit>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun getUserProfile(): Result<User?> // Diubah agar bisa null jika dokumen tidak ada
    suspend fun updateUserProfile(user: User): Result<Unit>
    suspend fun performTopUp(amount: Double): Result<Unit>
    fun logout()

    // --- FUNGSI BARU UNTUK VOUCHER ---
    fun getMyVouchers(): Flow<Result<List<Voucher>>>
    suspend fun claimVoucher(voucher: Voucher): Result<Unit>
}

// --- IMPLEMENTASI ---
class AuthRepositoryImpl : AuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    override suspend fun registerUser(email: String, password: String, userProfile: User): Result<Unit> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw IllegalStateException("Firebase user not found.")
            // PERBAIKAN: Menggunakan .copy(uid = ...) agar cocok dengan model User yang baru
            val finalUserProfile = userProfile.copy(userId = firebaseUser.uid, createdAt = Timestamp.now())
            firestore.collection("users").document(firebaseUser.uid).set(finalUserProfile).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Login dengan Email & Password
    override suspend fun loginUser(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Login dengan Google
    override suspend fun loginWithGoogle(idToken: String): Result<Unit> {
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
            Result.success(Unit)
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
                .collection("my_vouchers").document(voucher.documentId)

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(userVoucherRef)
                if (snapshot.exists()) {
                    throw Exception("Voucher ini sudah pernah Anda klaim.")
                }
                // Salin data voucher ke sub-koleksi milik user
                transaction.set(userVoucherRef, voucher)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}