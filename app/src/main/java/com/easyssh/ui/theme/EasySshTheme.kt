package com.easyssh.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EasySshColors = darkColorScheme(
    primary = Color(0xFF22C55E),
    onPrimary = Color(0xFF03110A),
    secondary = Color(0xFF38BDF8),
    onSecondary = Color(0xFF02131D),
    tertiary = Color(0xFFF59E0B),
    background = Color(0xFF050505),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF0B0F14),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF111827),
    onSurfaceVariant = Color(0xFFCBD5E1),
    error = Color(0xFFF87171),
    onError = Color(0xFF230707)
)

@Composable
fun EasySshTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EasySshColors,
        typography = MaterialTheme.typography,
        content = content
    )
}

