package com.truvision.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Simple light color scheme; you can refine colors later.
private val LightColorScheme = lightColorScheme()

@Composable
fun TruVisionTheme(
    content: @Composable () -> Unit
) {
    // Force light mode only, ignore system dark theme.
    val colorScheme = LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
