package com.radioold.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val RadiooldColors = lightColorScheme(
    primary = Sepia,
    onPrimary = TextOnAccent,
    secondary = SealRed,
    onSecondary = TextOnAccent,
    tertiary = Brass,
    background = Parchment,
    onBackground = Ink,
    surface = ParchmentDeep,
    onSurface = Ink,
    surfaceVariant = ParchmentDeep,
    onSurfaceVariant = InkSoft,
    error = SealRed,
)

@Composable
fun RadiooldTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = RadiooldColors,
        typography = Typography,
        content = content,
    )
}
