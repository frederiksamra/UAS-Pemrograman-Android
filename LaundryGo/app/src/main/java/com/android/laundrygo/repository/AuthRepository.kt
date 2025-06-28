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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

interface AuthRepository {
    suspend fun registerUser(email: String, password: String, userProfile: User): Result<Unit>
    suspend fun loginUser(email: String, password: String): Result<FirebaseUser>
    suspend fun loginWithGoogle(idToken: String): Result<FirebaseUser>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun getUserProfile(): Result<User?>
    fun getCurrentUserProfile(): Flow<Result<User?>>
    suspend fun updateUserProfile(user: User): Result<Unit>
    suspend fun performTopUp(amount: Double): Result<Unit>
    fun logout()
    fun getMyVouchers(): Flow<Result<List<Voucher>>>
    suspend fun claimVoucher(voucher: Voucher): Result<Unit>
    suspend fun processPayment(
        userId: String,
        transactionId: String,
        amountToDeduct: Double,
        voucherToUseId: String?
    ): Result<Unit>

    fun getUserDocument(userId: String): Flow<Result<DocumentSnapshot>>
    fun getUserByUsername(username: String): Flow<Result<QuerySnapshot>>
    fun getAllUsers(): Flow<Result<List<User>>>
    suspend fun deleteUserById(userId: String): Result<Unit>
    fun getAllVouchers(): Flow<Result<List<Voucher>>>

    suspend fun addVoucher(
        voucherCode: String,
        description: String,
        discountValue: Double,
        discountType: String,
        expiryDate: Timestamp?
    ): Result<Unit>
}

class AuthRepositoryImpl : AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val TAG = "AuthRepositoryImpl"

    override suspend fun addVoucher(
        voucherCode: String,
        description: String,
        discountValue: Double,
        discountType: String,
        expiryDate: Timestamp?
    ): Result<Unit> {
        return try {
            // Menggunakan Map untuk memastikan semua field yang diperlukan, terutama `is_active`, tersimpan.
            val voucherData = mapOf(
                "voucher_code" to voucherCode,
                "description" to description,
                "discount_type" to discountType,
                "discount_value" to discountValue,
                "valid_until" to expiryDate,
                "valid_from" to null,
                "is_used" to false,
                "is_active" to true // <-- KUNCI PERBAIKAN
            )

            // Menyimpan map ke Firestore. Dokumen akan dibuat dengan ID yang sama dengan kode voucher.
            firestore.collection("vouchers").document(voucherCode).set(voucherData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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

    override suspend fun loginWithGoogle(idToken: String): Result<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user ?: throw IllegalStateException("Firebase user not found.")
            val userDocRef = firestore.collection("users").document(firebaseUser.uid)
            if (!userDocRef.get().await().exists()) {
                val newUserProfile = User(
                    userId = firebaseUser.uid, name = firebaseUser.displayName ?: "Google User",
                    email = firebaseUser.email ?: "", phone = firebaseUser.phoneNumber ?: "",
                    address = "", username = firebaseUser.displayName?.replace(" ", "")?.lowercase() ?: "googleuser",
                    createdAt = Timestamp.now()
                )
                userDocRef.set(newUserProfile).await()
            }
            Result.success(firebaseUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserProfile(): Result<User?> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.success(null)
            val document = firestore.collection("users").document(uid).get().await()
            Result.success(document.toObject<User>())
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
                if (error != null) { trySend(Result.failure(error)); return@addSnapshotListener }
                trySend(Result.success(snapshot?.toObject(User::class.java)))
            }
        awaitClose { listener.remove() }
    }

    override suspend fun updateUserProfile(user: User): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: throw Exception("User not logged in.")
            firestore.collection("users").document(uid).set(user, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun performTopUp(amount: Double): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: throw Exception("User not logged in.")
            if (amount <= 0) { throw IllegalArgumentException("Top up amount must be positive.") }
            val userDocRef = firestore.collection("users").document(uid)
            userDocRef.update("balance", FieldValue.increment(amount)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun logout() {
        auth.signOut()
    }

    override fun getMyVouchers(): Flow<Result<List<Voucher>>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId.isNullOrEmpty()) { trySend(Result.failure(Exception("User tidak login."))); close(); return@callbackFlow }
        val listener = firestore.collection("users").document(userId).collection("my_vouchers")
            .addSnapshotListener { snapshot, error ->
                if (error != null) { trySend(Result.failure(error)); return@addSnapshotListener }
                if (snapshot != null) {
                    trySend(Result.success(snapshot.toObjects(Voucher::class.java)))
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun claimVoucher(voucher: Voucher): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User tidak login.")
            val userVoucherRef = firestore.collection("users").document(userId).collection("my_vouchers").document(voucher.voucherDocumentId)
            firestore.runTransaction { transaction ->
                if (transaction.get(userVoucherRef).exists()) { throw Exception("Voucher ini sudah pernah Anda klaim.") }
                val claimedVoucherData = mapOf(
                    "voucher_code" to voucher.voucher_code, "discount_type" to voucher.discount_type,
                    "discount_value" to voucher.discount_value, "valid_from" to voucher.valid_from,
                    "valid_until" to voucher.valid_until, "is_used" to false
                )
                transaction.set(userVoucherRef, claimedVoucherData)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun processPayment(
        userId: String, transactionId: String, amountToDeduct: Double, voucherToUseId: String?
    ): Result<Unit> {
        return try {
            val userRef = firestore.collection("users").document(userId)
            val transactionRef = firestore.collection("transactions").document(transactionId)
            val voucherRef = voucherToUseId?.let { firestore.collection("users").document(userId).collection("my_vouchers").document(it) }
            firestore.runTransaction { transaction ->
                val userSnapshot = transaction.get(userRef)
                val currentBalance = userSnapshot.getDouble("balance") ?: 0.0
                if (currentBalance < amountToDeduct) { throw Exception("Saldo tidak mencukupi.") }
                transaction.update(userRef, "balance", currentBalance - amountToDeduct)
                transaction.update(transactionRef, "status", 1)
                voucherRef?.let { transaction.update(it, "is_used", true) }
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getUserDocument(userId: String): Flow<Result<DocumentSnapshot>> = flow {
        try {
            emit(Result.success(firestore.collection("users").document(userId).get().await()))
        } catch (e: CancellationException) { throw e } catch (e: Exception) { emit(Result.failure(e)) }
    }

    override fun getUserByUsername(username: String): Flow<Result<QuerySnapshot>> = flow {
        try {
            emit(Result.success(firestore.collection("users").whereEqualTo("username", username).get().await()))
        } catch (e: CancellationException) { throw e } catch (e: Exception) { emit(Result.failure(e)) }
    }

    override fun getAllUsers(): Flow<Result<List<User>>> = flow {
        try {
            emit(Result.success(firestore.collection("users").get().await().toObjects(User::class.java)))
        } catch (e: CancellationException) { throw e } catch (e: Exception) { emit(Result.failure(e)) }
    }

    override suspend fun deleteUserById(userId: String): Result<Unit> {
        return try {
            firestore.collection("users").document(userId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getAllVouchers(): Flow<Result<List<Voucher>>> = flow {
        try {
            emit(Result.success(firestore.collection("vouchers").get().await().toObjects(Voucher::class.java)))
        } catch (e: CancellationException) { throw e } catch (e: Exception) { emit(Result.failure(e)) }
    }
}
