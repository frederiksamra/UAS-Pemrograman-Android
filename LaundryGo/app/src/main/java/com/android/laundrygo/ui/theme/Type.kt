package com.android.laundrygo.ui.theme // Sesuaikan dengan package Anda

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.android.laundrygo.R // Sesuaikan dengan package Anda

// Muat font kustom Anda
val LondrinaSolid = FontFamily(
    Font(R.font.londrina_solid, FontWeight.Normal),
    Font(R.font.londrina_solid, FontWeight.Bold)
)

// Definisikan skala tipografi
val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = LondrinaSolid,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = LondrinaSolid,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default, // Menggunakan font default (Roboto) untuk keterbacaan
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    )
    /* Anda bisa mendefinisikan gaya lain sesuai kebutuhan */
)