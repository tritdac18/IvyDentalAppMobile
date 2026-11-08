package com.example.ivydentalapp.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Primary Brand Colors (Blue/Teal/Mint)
val Primary = Color(0xFF0EA5E9) // Sky Blue 500
val PrimaryDark = Color(0xFF0284C7) // Sky Blue 600
val PrimaryLight = Color(0xFFBAE6FD) // Sky Blue 200

// Secondary Brand Colors (Mint)
val Mint = Color(0xFF14B8A6) // Teal 500
val MintDark = Color(0xFF0F766E) // Teal 700
val MintLight = Color(0xFFCCFBF1) // Teal 100

// Gradients
val BlueGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF0EA5E9), Color(0xFF2DD4BF)) // Sky Blue to Teal
)
val MintGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF14B8A6), Color(0xFF34D399)) // Teal to Emerald
)

// Neutral Colors
val BackgroundLight = Color(0xFFF8FAFC) // Slate 50
val SurfaceWhite = Color(0xFFFFFFFF)
val TextDark = Color(0xFF0F172A) // Slate 900
val TextGray = Color(0xFF64748B) // Slate 500
val BorderColor = Color(0xFFE2E8F0) // Slate 200

// Status Colors
val Success = Color(0xFF10B981) // Emerald 500
val Pending = Color(0xFFF59E0B) // Amber 500
val Error = Color(0xFFEF4444) // Red 500

// Leaving original colors to avoid breaking other potential usages
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)
val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)