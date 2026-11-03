package com.cftracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object Brand {
    val Orange = Color(0xFFFF8A2B)
    val Pink = Color(0xFFE8336B)

    /** Degradado de la marca, el mismo que el fondo del icono. */
    val gradient: Brush = Brush.linearGradient(listOf(Orange, Pink))
}

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFF8A2B),
    onPrimary = Color(0xFF2A1000),
    primaryContainer = Color(0xFF4A2210),
    onPrimaryContainer = Color(0xFFFFDBC8),
    secondary = Color(0xFFFF6B95),
    onSecondary = Color(0xFF3B0018),
    tertiary = Color(0xFF6EE7A0),
    background = Color(0xFF121014),
    onBackground = Color(0xFFEDE6EE),
    surface = Color(0xFF121014),
    onSurface = Color(0xFFEDE6EE),
    surfaceVariant = Color(0xFF2A2530),
    onSurfaceVariant = Color(0xFFB4ABB8),
    surfaceContainerLow = Color(0xFF18151A),
    surfaceContainer = Color(0xFF1C181F),
    surfaceContainerHigh = Color(0xFF221E25),
    surfaceContainerHighest = Color(0xFF29242C),
    outline = Color(0xFF5A5260),
    outlineVariant = Color(0xFF38323C),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFFD45A00),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDBC8),
    onPrimaryContainer = Color(0xFF341100),
    secondary = Color(0xFFC2185B),
    tertiary = Color(0xFF1E8E4F),
    background = Color(0xFFFFF8F5),
    surface = Color(0xFFFFF8F5),
    surfaceVariant = Color(0xFFF3E6E0),
    surfaceContainerLow = Color(0xFFFFF1EB),
    surfaceContainer = Color(0xFFFBEBE4),
    surfaceContainerHigh = Color(0xFFF6E5DE),
    surfaceContainerHighest = Color(0xFFF0DFD8),
)

private val BaseTypography = Typography()

private val AppTypography = Typography(
    headlineLarge = BaseTypography.headlineLarge.copy(fontWeight = FontWeight.Black, letterSpacing = (-0.5).sp),
    headlineMedium = BaseTypography.headlineMedium.copy(fontWeight = FontWeight.Black, letterSpacing = (-0.5).sp),
    headlineSmall = BaseTypography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.25).sp),
    titleLarge = BaseTypography.titleLarge.copy(fontWeight = FontWeight.Bold),
    titleMedium = BaseTypography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    titleSmall = BaseTypography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
    labelLarge = BaseTypography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
    labelMedium = BaseTypography.labelMedium.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.8.sp),
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun CfTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
