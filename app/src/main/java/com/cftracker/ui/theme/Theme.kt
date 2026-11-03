package com.cftracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFF8A3D),
    onPrimary = Color(0xFF2B1300),
    primaryContainer = Color(0xFF5C2A00),
    onPrimaryContainer = Color(0xFFFFDBC8),
    secondary = Color(0xFFE6BEA9),
    tertiary = Color(0xFF8BD39A),
    background = Color(0xFF1A1210),
    surface = Color(0xFF1A1210),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFFC24E00),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDBC8),
    onPrimaryContainer = Color(0xFF341100),
    tertiary = Color(0xFF2E7D3E),
)

@Composable
fun CfTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content,
    )
}
