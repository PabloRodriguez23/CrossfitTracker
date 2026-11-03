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
    val Cyan = Color(0xFF00C2D8)
    val DeepBlue = Color(0xFF0369A1)

    /** Degradado de la marca, el mismo que el fondo del icono. */
    val gradient: Brush = Brush.linearGradient(listOf(Cyan, DeepBlue))
}

private val DarkColors = darkColorScheme(
    primary = Color(0xFF4DDCEE),
    onPrimary = Color(0xFF00363D),
    primaryContainer = Color(0xFF004F58),
    onPrimaryContainer = Color(0xFFA6EEFF),
    secondary = Color(0xFF7CC4FF),
    onSecondary = Color(0xFF00325A),
    tertiary = Color(0xFF6EE7A0),
    background = Color(0xFF0E1316),
    onBackground = Color(0xFFE2EBEF),
    surface = Color(0xFF0E1316),
    onSurface = Color(0xFFE2EBEF),
    surfaceVariant = Color(0xFF24303A),
    onSurfaceVariant = Color(0xFFA8B6BF),
    surfaceContainerLow = Color(0xFF13191D),
    surfaceContainer = Color(0xFF171E23),
    surfaceContainerHigh = Color(0xFF1C242A),
    surfaceContainerHighest = Color(0xFF222B32),
    outline = Color(0xFF4E5C66),
    outlineVariant = Color(0xFF2E3A43),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF00838F),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB2EBF2),
    onPrimaryContainer = Color(0xFF002022),
    secondary = Color(0xFF0277BD),
    tertiary = Color(0xFF1E8E4F),
    background = Color(0xFFF4FBFC),
    surface = Color(0xFFF4FBFC),
    surfaceVariant = Color(0xFFDCEBEE),
    surfaceContainerLow = Color(0xFFEDF7F9),
    surfaceContainer = Color(0xFFE7F2F5),
    surfaceContainerHigh = Color(0xFFE1EEF1),
    surfaceContainerHighest = Color(0xFFDAE9ED),
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
