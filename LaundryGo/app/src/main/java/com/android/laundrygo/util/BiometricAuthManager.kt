package com.android.laundrygo.util // Ganti dengan package Anda

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class BiometricAuthManager(
    private val activity: FragmentActivity,
    private val onAuthSuccess: () -> Unit,
    private val onAuthError: (Int, CharSequence) -> Unit,
    private val onAuthFailed: () -> Unit
) {

    private val executor = ContextCompat.getMainExecutor(activity)
    private val biometricPrompt: BiometricPrompt

    init {
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onAuthSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                // Kita tidak ingin menampilkan error jika pengguna menekan tombol cancel
                if (errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON && errorCode != BiometricPrompt.ERROR_USER_CANCELED) {
                    onAuthError(errorCode, errString)
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onAuthFailed()
            }
        }
        biometricPrompt = BiometricPrompt(activity, executor, callback)
    }

    fun canAuthenticate(): Boolean {
        val biometricManager = BiometricManager.from(activity)
        return biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun showBiometricPrompt(
        title: String,
        subtitle: String,
        description: String
    ) {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription(description)
            .setNegativeButtonText("Batalkan") // Atau gunakan metode lain
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}