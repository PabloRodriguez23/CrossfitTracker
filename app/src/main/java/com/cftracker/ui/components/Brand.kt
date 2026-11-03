package com.cftracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cftracker.R
import com.cftracker.ui.theme.Brand

/** Logo: pesa rusa blanca sobre el degradado de la marca. */
@Composable
fun AppLogo(size: Dp = 44.dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.3f))
            .background(Brand.gradient),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_kettlebell),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(size * 0.62f),
        )
    }
}

/** Círculo con las iniciales del ejercicio ("Back Squat" → "BS"). */
@Composable
fun InitialsAvatar(name: String, size: Dp = 44.dp) {
    val initials = name.split(' ', '-')
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.take(1).uppercase() }
        .ifEmpty { "?" }
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Brand.gradient),
        contentAlignment = Alignment.Center,
    ) {
        Text(initials, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
    }
}
