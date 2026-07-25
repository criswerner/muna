package com.tiendamuna.stock.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = GiraffePrimaryDark,
    secondary = GiraffeSecondaryDark,
    tertiary = GiraffeTertiary,
    background = GiraffeBackgroundDark,
    surface = GiraffeSurfaceDark
)

private val LightColorScheme = lightColorScheme(
    primary = GiraffePrimary,
    secondary = GiraffeSecondary,
    tertiary = GiraffeTertiary,
    background = GiraffeBackground,
    surface = GiraffeSurface,
    onBackground = GiraffeOnBackground,
    onSurface = GiraffeOnBackground
)

@Composable
fun StockTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color disabled by default to prefer the brand colors from the logo
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
