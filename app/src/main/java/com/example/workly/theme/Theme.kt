package com.example.workly.theme

import android.app.Activity
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PremiumSilver,
    secondary = PremiumSilverDark,
    tertiary = PremiumWhite,
    background = PremiumBlack,
    surface = PremiumBlackSurface,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    outline = DarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = ProfessionalBlue,
    secondary = ElectricTeal,
    tertiary = EnergyOrange,
    background = BackgroundGray, // Switched to BackgroundGray instead of pure White for better light mode look
    surface = Color.White,
    surfaceVariant = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextPrimary,
    outline = Color(0xFFE2E8F0) // Light border color for light mode
)

@Composable
fun WorklyTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    // Set dynamicColor to false by default to maintain your brand colors
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = when(themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> systemDark
    }

    val context = LocalContext.current
    
    val targetColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> DarkColorScheme
        else -> LightColorScheme
    }

    // Performance-first animation scopes
    // We use a flag to skip animation on the very first composition to prevent "flashing"
    var isInitialComposition by remember { mutableStateOf(true) }
    
    val animationDuration = if (isInitialComposition) 0 else 300
    
    val animatedBackground by animateColorAsState(
        targetColorScheme.background, 
        tween(animationDuration), 
        label = "background"
    )
    val animatedSurface by animateColorAsState(
        targetColorScheme.surface, 
        tween(animationDuration), 
        label = "surface"
    )
    val animatedPrimaryContainer by animateColorAsState(
        targetColorScheme.primaryContainer, 
        tween(animationDuration), 
        label = "primaryContainer"
    )

    val finalColorScheme = targetColorScheme.copy(
        background = animatedBackground,
        surface = animatedSurface,
        primaryContainer = animatedPrimaryContainer
    )
    
    LaunchedEffect(Unit) {
        isInitialComposition = false
    }

    // Edge-to-Edge and Status Bar Sync
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !isDark
            insetsController.isAppearanceLightNavigationBars = !isDark
        }
    }

    MaterialTheme(
        colorScheme = finalColorScheme,
        typography = Typography,
        content = content
    )
}
