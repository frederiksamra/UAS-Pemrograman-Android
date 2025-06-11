package com.android.laundrygo.ui.screens // Sesuaikan dengan package Anda

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.laundrygo.R
import com.android.laundrygo.ui.theme.LaundryGoTheme
import com.android.laundrygo.viewmodel.StartNavigation
import com.android.laundrygo.viewmodel.StartViewModel

@Composable
fun StartScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: StartViewModel = viewModel()
) {
    LaunchedEffect(key1 = true) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is StartNavigation.ToLogin -> onNavigateToLogin()
                is StartNavigation.ToRegister -> onNavigateToRegister()
            }
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
            .background(MaterialTheme.colorScheme.primary)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "LaundryGo Logo",
            modifier = Modifier
                .size(450.dp)
                .padding(top = 100.dp),
            contentScale = ContentScale.Fit
        )

        Text(
            text = "Welcome to\nLaundryGo!",
            color = MaterialTheme.colorScheme.primaryContainer,
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 40.dp)
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                shape = MaterialTheme.shapes.large
            ) {
                Text(text = "Log in", style = MaterialTheme.typography.titleLarge)
            }

            Button(
                onClick = onRegisterClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                shape = MaterialTheme.shapes.large
            ) {
                Text(text = "Register", style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StartScreenPreview() {
    LaundryGoTheme {
        StartScreenContent(onLoginClick = {}, onRegisterClick = {})
    }
}