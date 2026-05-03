package com.coffee.order.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = CoffeePrimary,                    // Deep Cocoa Brown
    secondary = CoffeeSecondary,                // Muted Matcha Green
    tertiary = CoffeeTertiary,                  // Roasted Bean Brown
    background = LightSurface,                  // Almond Cream White
    surface = Color.White,                      // Pure White
    onPrimary = Color.White,                    // White text on Brown
    onBackground = DarkGray,                    // Espresso Black on cream
    onSurface = DarkGray,                       // Espresso Black on white
    error = ErrorRed,                           // Deep Red
    onError = Color.White,                      // White on error
    primaryContainer = CoffeePrimary.copy(alpha = 0.1f), 
    secondaryContainer = CoffeeSecondary.copy(alpha = 0.1f),
    errorContainer = ErrorRed.copy(alpha = 0.1f)
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    secondary = SecondaryDark,
    background = BackgroundDark,
    surface = SurfaceDark,
    onPrimary = DarkGray,
    onBackground = LightSurface,
    onSurface = LightSurface,
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun CoffeeAdminTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
