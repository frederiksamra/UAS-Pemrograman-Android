package com.android.laundrygo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp

@Composable
fun InitialsProfilePicture(
    modifier: Modifier = Modifier,
    name: String,
    size: Dp,
    textStyle: TextStyle
) {
    val initials = name.firstOrNull()?.uppercase() ?: "?"

    val backgroundColor = MaterialTheme.colorScheme.primaryContainer

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = textStyle,
            color = MaterialTheme.colorScheme.primary
        )
    }
}