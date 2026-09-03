package com.ilmek.bordro.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val EmeraldPrimary = androidx.compose.ui.graphics.Color(0xFF047857)
private val SlatePrimary = androidx.compose.ui.graphics.Color(0xFF0F172A)

private val LightColors = lightColorScheme(
    primary = EmeraldPrimary,
    secondary = SlatePrimary,
    surface = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
    background = androidx.compose.ui.graphics.Color(0xFFF1F5F9),
)

private val DarkColors = darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF34D399),
    secondary = androidx.compose.ui.graphics.Color(0xFF94A3B8),
)

@Composable
fun BordroTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, content = content)
}
