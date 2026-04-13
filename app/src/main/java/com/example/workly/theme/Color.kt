package com.example.workly.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Premium Black Palette (80-15-5 Design Pattern)
val PremiumBlack = Color(0xFF000000)
val PremiumBlackSurface = Color(0xFF0A0A0A)
val PremiumSilver = Color(0xFFCFD8DC) // Light metallic silver
val PremiumSilverDark = Color(0xFF78909C) // Slate silver
val PremiumWhite = Color(0xFFF5F5F7) // Pure clean white

// Semantic Colors for the Luxury Look
val DarkBorder = Color(0xFF1A1A1A)
val SilverAccent = Color(0xFFE0E0E0)
val PlatinumShadow = Color(0x66000000)

val ProfessionalBlue = Color(0xFF1565C0)
val ElectricTeal = Color(0xFF009688)
val EnergyOrange = Color(0xFFFF9800)
val BackgroundGray = Color(0xFFF8F9FA)
val TextPrimary = Color(0xFF1C1C1E) 
val TextSecondary = Color(0xFF555555) 

val PrimaryGradient = Brush.linearGradient(
    colors = listOf(PremiumBlack, Color(0xFF1A1A1A))
)

val SecondaryGradient = Brush.linearGradient(
    colors = listOf(PremiumSilver, PremiumSilverDark)
)
