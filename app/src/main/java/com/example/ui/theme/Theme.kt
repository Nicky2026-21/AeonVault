package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color(0xFF04101A),
    primaryContainer = Color(0xFF003642),
    onPrimaryContainer = Color(0xFF97F0FF),
    secondary = ElectricViolet,
    onSecondary = Color(0xFF28004F),
    secondaryContainer = Color(0xFF450A75),
    onSecondaryContainer = Color(0xFFF3DAFF),
    tertiary = EmeraldGlow,
    onTertiary = Color(0xFF002B1B),
    background = DarkSpaceBg,
    onBackground = DarkTextPrimary,
    surface = DarkSpaceSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSpaceCard,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkGlassBorder,
    error = CoralNeon,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = QuantumIndigo,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDEE5FF),
    onPrimaryContainer = Color(0xFF001550),
    secondary = ElectricViolet,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF3DAFF),
    onSecondaryContainer = Color(0xFF28004F),
    tertiary = EmeraldGlow,
    onTertiary = Color.White,
    background = LightBg,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightCard,
    onSurfaceVariant = LightTextSecondary,
    outline = LightGlassBorder,
    error = CoralNeon,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Default false to preserve ÆonVault's distinctive futuristic theme
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
