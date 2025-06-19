package com.android.laundrygo.repository

import com.android.laundrygo.model.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

interface AuthRepository {
    suspend fun registerUser(email: String, password: String, userProfile: User): Result<Unit>
    suspend fun loginUser(email: String, password: String): Result<Unit>
    suspend fun loginWithGoogle(idToken: String): Result<Unit>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun getUserProfile(): Result<User>
    suspend fun updateUserProfile(user: User): Result<Unit>
    suspend fun performTopUp(amount: Double): Result<Unit>
    fun logout()
}

class AuthRepositoryImpl : AuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Daftar user baru -> Disimpan ke database
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

            // Cek apakah ini pengguna baru (yang login via Google untuk pertama kali)
            val isNewUser = authResult.additionalUserInfo?.isNewUser ?: false
            if (isNewUser) {
                // Jika user baru, buatkan profil dasar untuknya di Firestore
                val newUserProfile = User(
                    userId = firebaseUser.uid,
                    name = firebaseUser.displayName ?: "Google User",
                    email = firebaseUser.email ?: "",
                    phone = firebaseUser.phoneNumber ?: "",
                    address = "",
                    username = firebaseUser.displayName?.replace(" ", "")?.lowercase() ?: "googleuser",
                    createdAt = Timestamp.now()
                )
                firestore.collection("users").document(firebaseUser.uid).set(newUserProfile).await()
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
    override suspend fun getUserProfile(): Result<User> {
        return try {
            val uid = auth.currentUser?.uid ?: throw Exception("User not logged in.")
            val document = firestore.collection("users").document(uid).get().await()
            val user = document.toObject(User::class.java)
                ?: throw Exception("User data not found in Firestore.")
            Result.success(user)
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
}