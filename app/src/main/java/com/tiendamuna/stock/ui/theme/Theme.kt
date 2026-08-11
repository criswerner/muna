package com.tiendamuna.stock.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AppPrimary,
    onPrimary = AppBackground,
    secondary = AppSecondary,
    onSecondary = AppOnSurface,
    tertiary = AppTertiary,
    background = AppBackground,
    onBackground = AppOnBackground,
    surface = AppSurface,
    onSurface = AppOnSurface,
    surfaceVariant = AppSurfaceVariant,
    onSurfaceVariant = AppSecondary,
    error = AppError
)

// We define a similar scheme for Light for now, 
// or could force Dark if the user wants that specific "Premium" look.
private val LightColorScheme = lightColorScheme(
    primary = AppPrimary,
    onPrimary = AppBackground,
    secondary = GiraffeSecondary,
    background = GiraffeBackground,
    surface = Color.White
)

@Composable
fun StockTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // For this specific design, we'll favor the Dark scheme 
    // as requested for the "Organic/Modern" look.
    val colorScheme = if (darkTheme) DarkColorScheme else DarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
