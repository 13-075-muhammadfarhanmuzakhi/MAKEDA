package com.farhanrr.makeda.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val MakedaPrimary = Color(0xFF3D5AFE)
val MakedaPrimaryDark = Color(0xFF7C9BFF)
val MakedaSecondary = Color(0xFF00BFA5)
val MakedaIncome = Color(0xFF2E7D32)
val MakedaExpense = Color(0xFFD32F2F)

private val LightColors: ColorScheme = lightColorScheme(
    primary = MakedaPrimary,
    secondary = MakedaSecondary,
    background = Color(0xFFF7F8FC),
    surface = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE3E8FF),
    secondaryContainer = Color(0xFFD9F7F1)
)

private val DarkColors: ColorScheme = darkColorScheme(
    primary = MakedaPrimaryDark,
    secondary = MakedaSecondary,
    background = Color(0xFF12141C),
    surface = Color(0xFF1B1E29),
    primaryContainer = Color(0xFF2A335C),
    secondaryContainer = Color(0xFF1C3A36)
)

@Composable
fun MakedaTheme(darkMode: Boolean, content: @Composable () -> Unit) {
    val colors = if (darkMode) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        shapes = androidx.compose.material3.Shapes(
            small = androidx.compose.foundation.shape.RoundedCornerShape(androidx.compose.ui.unit.Dp(12f)),
            medium = androidx.compose.foundation.shape.RoundedCornerShape(androidx.compose.ui.unit.Dp(16f)),
            large = androidx.compose.foundation.shape.RoundedCornerShape(androidx.compose.ui.unit.Dp(24f))
        ),
        content = content
    )
}