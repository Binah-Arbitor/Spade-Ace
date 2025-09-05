package com.binah.spadeace.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColors(
    primary = Color(0xFFD0BCFF),
    primaryVariant = Color(0xFF4F378B),
    secondary = Color(0xFFCCC2DC),
    secondaryVariant = Color(0xFF4A4458),
    background = Color(0xFF10131D),
    surface = Color(0xFF10131D),
    error = Color(0xFFF2B8B5),
    onPrimary = Color(0xFF371E73),
    onSecondary = Color(0xFF332D41),
    onBackground = Color(0xFFE6E0E9),
    onSurface = Color(0xFFE6E0E9),
    onError = Color(0xFF601410)
)

private val LightColorPalette = lightColors(
    primary = Color(0xFF6750A4),
    primaryVariant = Color(0xFFEADDFF),
    secondary = Color(0xFF625B71),
    secondaryVariant = Color(0xFFE8DEF8),
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    error = Color(0xFFBA1A1A),
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    onError = Color(0xFFFFFFFF)
)

@Composable
fun SpadeAceTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        content = content
    )
}