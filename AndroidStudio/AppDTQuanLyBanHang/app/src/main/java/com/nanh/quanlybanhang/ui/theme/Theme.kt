package com.nanh.quanlybanhang.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = GoldAccent,
    onPrimary = NavyDeep,
    primaryContainer = NavyElevated,
    onPrimaryContainer = GoldAccent,
    secondary = ChartTeal,
    onSecondary = Color.White,
    tertiary = ChartBlue,
    background = NavyDeep,
    onBackground = TextPrimaryLight,
    surface = NavyCard,
    onSurface = TextPrimaryLight,
    surfaceVariant = NavyElevated,
    onSurfaceVariant = TextSecondary,
    outline = TextSecondary.copy(alpha = 0.35f),
)

@Composable
fun TinhTuShopTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = AppTypography,
        content = content,
    )
}
