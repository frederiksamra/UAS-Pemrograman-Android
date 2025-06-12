package com.android.laundrygo.ui.theme // Sesuaikan dengan package Anda

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Definisikan skema warna untuk mode terang (Light Mode)
private val LightColorScheme = lightColorScheme(
    primary = DarkBlue,
    onPrimary = White,
    primaryContainer = Cream,
    onPrimaryContainer = BlackText,

    // --- BAGIAN INI DIUBAH ---
    secondary = LightSteelBlue, // Latar belakang profil sekarang biru muda
    onSecondary = DarkBlue,     // Teks & ikon di atasnya tetap Biru Tua

    background = White,
    onBackground = DarkBlueText,
    surface = DarkBlue,
    onSurface = White,
    surfaceVariant = Grey,
    onSurfaceVariant = DarkBlueText,
    error = RedError,
    onError = White
)

// Definisikan skema warna untuk mode gelap (Dark Mode)
private val DarkColorScheme = darkColorScheme(
    primary = Cream, // Warna utama di mode gelap adalah krem
    onPrimary = BlackText,
    primaryContainer = DarkBlue,
    onPrimaryContainer = White,
    background = Color(0xFF121212), // Background gelap standar
    onBackground = White,
    surface = Color(0xFF1E1E1E), // Warna card/permukaan sedikit lebih terang
    onSurface = White,
    surfaceVariant = Color(0xFF424242),
    onSurfaceVariant = Color(0xFFBDBDBD),
    error = RedError,
    onError = White
)

@Composable
fun LaundryGoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}