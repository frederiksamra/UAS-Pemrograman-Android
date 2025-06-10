package com.android.laundrygo.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.laundrygo.R
import com.android.laundrygo.viewmodel.StartNavigation
import com.android.laundrygo.viewmodel.StartViewModel

// Define custom color constants
private val BackgroundColor = Color(0xFF344970)
private val TextColor = Color(0xFFFFF2D7)
private val ButtonColor = Color(0xFFFFF2D7)
private val ButtonTextColor = Color(0xFF000000)

// Define custom font family (you'll need to add the font file to res/font/)
private val LondrinaSolidFontFamily = FontFamily(
    Font(R.font.londrina_solid, FontWeight.Normal)
)

@Composable
fun StartScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: StartViewModel = StartViewModel()
) {
    // Collect navigation events
    val navigationEvent by viewModel.navigationEvent.collectAsStateWithLifecycle(
        initialValue = null
    )

    // Handle navigation events
    LaunchedEffect(navigationEvent) {
        when (navigationEvent) {
            is StartNavigation.ToLogin -> onNavigateToLogin()
            is StartNavigation.ToRegister -> onNavigateToRegister()
            null -> { /* No action needed */ }
        }
    }

    StartScreenContent(
        onLoginClick = viewModel::onLoginClicked,
        onRegisterClick = viewModel::onRegisterClicked
    )
}

@Composable
private fun StartScreenContent(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo/Image
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "LaundryGo Logo",
            modifier = Modifier
                .size(450.dp)
                .padding(top = 100.dp),
            contentScale = ContentScale.Fit
        )

        // Welcome Text
        Text(
            text = "Welcome to\nLaundryGo!",
            color = TextColor,
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = LondrinaSolidFontFamily,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 40.dp)
        )

        // Auth Container
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Tombol Login
            Button(
                onClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ButtonColor,
                    contentColor = ButtonTextColor
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(text = "Log in")
            }

            // Tombol Register
            Button(
                onClick = onRegisterClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ButtonColor,
                    contentColor = ButtonTextColor
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(text = "Register")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StartScreenPreview() {
    StartScreenContent(
        onLoginClick = {}, // We provide an empty action for the preview
        onRegisterClick = {} // We provide an empty action for the preview
    )
}
