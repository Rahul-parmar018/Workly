package com.example.workly.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val ProfessionalBlue = Color(0xFF1565C0)
val ProfessionalBlueDark = Color(0xFF0D47A1)
val EnergyOrange = Color(0xFFFF9800)
val ElectricTeal = Color(0xFF009688)
val BackgroundGray = Color(0xFFF8F9FA)

val TextPrimary = Color(0xFF1C1C1E) // Near black
val TextSecondary = Color(0xFF555555) // Darker gray for better contrast

val PrimaryGradient = Brush.linearGradient(
    colors = listOf(ProfessionalBlue, ElectricTeal)
)

val SecondaryGradient = Brush.linearGradient(
    colors = listOf(EnergyOrange, Color(0xFFFFC107))
)

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Premium Dark Mode Palette
val DarkBackground = Color(0xFF0F172A)
val DarkSurface = Color(0xFF111827)
val DarkCard = Color(0xFF1E293B)
val DarkBorder = Color(0xFF334155)
val DarkTextPrimary = Color(0xFFF8FAFC)
val DarkTextSecondary = Color(0xFF94A3B8)
