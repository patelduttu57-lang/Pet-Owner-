package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = ForestGreen,
    onPrimary = CrispWhite,
    primaryContainer = SoftMintContainer,
    onPrimaryContainer = ForestGreenDark,
    secondary = WarmOrange,
    onSecondary = CrispWhite,
    secondaryContainer = SoftApricotContainer,
    onSecondaryContainer = OnSoftApricot,
    tertiary = ForestGreenDark,
    onTertiary = CrispWhite,
    background = CrispWhite,
    onBackground = InkCharcoal,
    surface = CrispWhite,
    onSurface = InkCharcoal,
    surfaceVariant = SoftMintSurface,
    onSurfaceVariant = SageSlate,
    surfaceContainerLowest = CrispWhite,
    surfaceContainerLow = WarmIvoryBackground,
    surfaceContainer = SoftMintSurface,
    surfaceContainerHigh = SoftMintContainer,
    outline = SoftOutline,
    outlineVariant = MintBorder,
    error = ErrorRed,
    onError = CrispWhite,
    errorContainer = ErrorContainerLight
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkMintPrimary,
    onPrimary = DarkForestContainer,
    primaryContainer = DarkForestContainer,
    onPrimaryContainer = DarkMintPrimary,
    secondary = DarkOrangeAccent,
    onSecondary = DarkApricotContainer,
    secondaryContainer = DarkApricotContainer,
    onSecondaryContainer = DarkOrangeAccent,
    background = DarkSurface,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurface,
    surfaceContainerLowest = DarkSurface,
    surfaceContainerLow = DarkSurfaceVariant,
    surfaceContainer = DarkSurfaceVariant,
    outline = DarkOutline
)

private val PawCareShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun PawCareTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = PawCareShapes,
        content = content
    )
}
