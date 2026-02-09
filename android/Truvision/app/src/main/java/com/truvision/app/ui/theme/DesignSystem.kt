package com.truvision.app.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object TruVisionColors {
    // Primary colors
    val Primary = Color(0xFF2196F3)
    val PrimaryVariant = Color(0xFF1976D2)
    val PrimaryLight = Color(0xFF64B5F6)
    
    // Secondary colors
    val Secondary = Color(0xFF4CAF50)
    val SecondaryVariant = Color(0xFF388E3C)
    val SecondaryLight = Color(0xFF81C784)
    
    // Status colors
    val Success = Color(0xFF4CAF50)
    val Error = Color(0xFFF44336)
    val Warning = Color(0xFFFFA726)
    val Info = Color(0xFF2196F3)
    
    // Connection status colors
    val Connected = Color(0xFF4CAF50)
    val Disconnected = Color(0xFFF44336)
    val Checking = Color(0xFFFFC107)
    
    // Risk level colors
    val RiskLow = Color(0xFF4CAF50)
    val RiskMedium = Color(0xFFFFA726)
    val RiskHigh = Color(0xFFF44336)
    
    // Background colors
    val BackgroundLight = Color(0xFFF5F5F5)
    val BackgroundCard = Color(0xFFFFFFFF)
    val BackgroundPreview = Color(0xFFE8F4F8)
    
    // Text colors
    val TextPrimary = Color(0xFF212121)
    val TextSecondary = Color(0xFF757575)
    val TextHint = Color(0xFF9E9E9E)
    
    // Border colors
    val BorderDefault = Color(0xFFE0E0E0)
    val BorderSelected = Color(0xFF2196F3)
}

object TruVisionSpacing {
    // Base spacing unit
    val Unit = 8.dp
    
    // Common spacing values
    val ExtraSmall = 4.dp
    val Small = 8.dp
    val Medium = 16.dp
    val Large = 24.dp
    val ExtraLarge = 32.dp
    val Huge = 48.dp
    
    // Card padding
    val CardPadding = 16.dp
    val CardSpacing = 8.dp
    
    // Screen padding
    val ScreenPadding = 16.dp
    
    // Icon sizes
    val IconSmall = 16.dp
    val IconMedium = 24.dp
    val IconLarge = 32.dp
    val IconHuge = 64.dp
    
    // Button heights
    val ButtonHeightSmall = 36.dp
    val ButtonHeightMedium = 48.dp
    val ButtonHeightLarge = 56.dp
    
    // Progress indicator sizes
    val ProgressSmall = 24.dp
    val ProgressMedium = 48.dp
    val ProgressLarge = 60.dp
}

object TruVisionElevation {
    val None = 0.dp
    val Small = 2.dp
    val Medium = 4.dp
    val Large = 8.dp
    val ExtraLarge = 16.dp
}

object TruVisionCornerRadius {
    val Small = 4.dp
    val Medium = 8.dp
    val Large = 12.dp
    val ExtraLarge = 16.dp
    val Round = 50.dp
}
